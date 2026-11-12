package com.example

import android.content.Context
import android.util.Log
import com.example.models.*
import com.example.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

data class TreasurySummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netBalance: Double,
    val pendingExpenses: Double = 0.0
)

class AgritechFirestoreRepository(private val db: FirebaseFirestore) {

    companion object {
        /**
         * OWASP MASVS Input Sanitization:
         * Guards numeric values against NaN, Infinity, negative numbers, and overflow.
         */
        fun sanitizeNonNegativeDouble(value: Double, maxAllowed: Double = 1_000_000_000.0): Double {
            if (value.isNaN() || value.isInfinite() || value < 0.0) return 0.0
            return value.coerceIn(0.0, maxAllowed)
        }

        fun sanitizePositiveDouble(value: Double, fallback: Double = 0.01, maxAllowed: Double = 1_000_000_000.0): Double {
            if (value.isNaN() || value.isInfinite() || value <= 0.0) return fallback
            return value.coerceIn(fallback, maxAllowed)
        }

        fun sanitizeNonNegativeInt(value: Int, maxAllowed: Int = 10_000_000): Int {
            if (value < 0) return 0
            return value.coerceIn(0, maxAllowed)
        }

        /**
         * Role-Based Access Control (RBAC):
         * WORKER and VETERINARIAN / VET_CLINIC sessions joined via QR code cannot delete approved
         * treasury records or modify finalized sale invoices (`INV-...`).
         */
        fun canDeleteOrModifyApprovedTreasuryRecord(role: AppRole): Boolean {
            return role == AppRole.OWNER || role == AppRole.OWNER_BARN
        }

        fun canApprovePendingTransaction(role: AppRole): Boolean {
            return role == AppRole.OWNER || role == AppRole.OWNER_BARN
        }

        const val UNASSIGNED_FARM_ID = "unassigned_farm"

        fun isUnassignedFarm(farmId: String?): Boolean {
            return farmId.isNullOrBlank() || farmId == UNASSIGNED_FARM_ID || farmId in AgritechMockDb.deletedFarmIds
        }

        fun isImmutableFinalizedSaleInvoice(tx: TransactionModel): Boolean {
            val invNo = tx.invoiceData?.invoiceNumber.orEmpty()
            return invNo.startsWith("INV-", ignoreCase = true) && tx.status.equals("APPROVED", ignoreCase = true)
        }

        /**
         * Ensures third-party requests/invoices (Worker, Vet, Trader, Supplier, Hatchery, Importer)
         * always enter the Owner's treasury as `PENDING` until approved by `OWNER` / `OWNER_BARN`.
         */
        fun enforceInitialStatusForRole(requestedStatus: String, actorRole: AppRole): String {
            return if (actorRole == AppRole.OWNER || actorRole == AppRole.OWNER_BARN) {
                requestedStatus.ifBlank { "APPROVED" }
            } else {
                "PENDING"
            }
        }

        fun calculateNetBalanceFromTransactions(transactions: List<TransactionModel>): TreasurySummary {
            val approvedTxs = transactions.filter { it.status == "APPROVED" }
            val totalIncome = approvedTxs
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            val totalExpenses = approvedTxs
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            val pendingExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE && it.status == "PENDING" }
                .sumOf { it.amount }
            return TreasurySummary(
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                netBalance = totalIncome - totalExpenses,
                pendingExpenses = pendingExpenses
            )
        }
    }

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    constructor() : this(FirebaseFirestore.getInstance())

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun farmDocRef(farmId: String) =
        db.collection("farms").document(farmId)

    private fun farmInvitesRef(farmId: String) =
        farmDocRef(farmId).collection("invites")

    private fun farmCyclesRef(farmId: String) =
        farmDocRef(farmId).collection("cycles")

    private fun farmReportsRef(farmId: String) =
        farmDocRef(farmId).collection("dailyReports")

    private fun farmVetNotesRef(farmId: String) =
        farmDocRef(farmId).collection("vetNotes")

    private fun farmTransactionsRef(farmId: String) =
        farmDocRef(farmId).collection("transactions")

    /**
     * Saves or updates a shared FarmModel in `/farms/{farmId}` including its `members` RBAC map
     * `{ uid: "OWNER" | "VETERINARIAN" | "WORKER" }`.
     */
    suspend fun saveFarmRecord(
        farm: FarmModel,
        role: AppRole = AppRole.OWNER
    ): Result<Unit> {
        val uid = requireUserId()
        val roleString = when (role) {
            AppRole.VETERINARIAN, AppRole.VET_CLINIC -> "VETERINARIAN"
            AppRole.WORKER -> "WORKER"
            else -> "OWNER"
        }
        val docRef = farmDocRef(farm.id)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                @Suppress("UNCHECKED_CAST")
                val existingMembers = (snap.get("members") as? Map<String, String>).orEmpty()
                val existingOwnerId = snap.getString("assignedOwnerId")?.takeIf { it.isNotBlank() } ?: uid
                val mergedMembers = existingMembers + farm.members + (uid to roleString) + (existingOwnerId to "OWNER")
                docRef.update(
                    mapOf(
                        "id" to farm.id,
                        "farmId" to farm.id,
                        "name" to farm.name,
                        "location" to farm.location,
                        "capacity" to farm.capacity,
                        "assignedOwnerId" to existingOwnerId,
                        "assignedWorkerId" to farm.assignedWorkerId,
                        "currencyCode" to farm.currencyCode.ifBlank { "DZD" },
                        "schemaVersion" to farm.schemaVersion,
                        "sectorId" to farm.sectorId.ifBlank { "AGRICULTURE" },
                        "branchId" to farm.branchId.ifBlank { "BROILER" },
                        "customMetrics" to farm.customMetrics,
                        "members" to mergedMembers,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val ownerId = if (roleString == "OWNER") uid else (farm.members.entries.firstOrNull { it.value == "OWNER" }?.key ?: uid)
                val initialMembers = farm.members + (uid to roleString) + (ownerId to "OWNER")
                docRef.set(
                    mapOf(
                        "id" to farm.id,
                        "farmId" to farm.id,
                        "name" to farm.name,
                        "location" to farm.location,
                        "capacity" to farm.capacity,
                        "assignedOwnerId" to ownerId,
                        "assignedWorkerId" to farm.assignedWorkerId,
                        "currencyCode" to farm.currencyCode.ifBlank { "DZD" },
                        "schemaVersion" to farm.schemaVersion,
                        "sectorId" to farm.sectorId.ifBlank { "AGRICULTURE" },
                        "branchId" to farm.branchId.ifBlank { "BROILER" },
                        "customMetrics" to farm.customMetrics,
                        "members" to initialMembers,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Creates a verified, time-bounded invitation document under `/farms/{farmId}/invites/{inviteId}`.
     * Only the farm OWNER is permitted by Firestore security rules to create invites.
     */
    suspend fun createFarmInvite(
        farmId: String,
        role: AppRole,
        ttlMillis: Long = 24L * 60L * 60L * 1000L,
        customInviteId: String? = null
    ): Result<FarmInviteModel> {
        val uid = requireUserId()
        require(!isUnassignedFarm(farmId)) { "Cannot create an invite for an unassigned farm." }
        val normalizedRole = when (role) {
            AppRole.VETERINARIAN, AppRole.VET_CLINIC -> "VETERINARIAN"
            AppRole.WORKER -> "WORKER"
            else -> throw IllegalArgumentException("Invites can only be issued for WORKER or VETERINARIAN roles.")
        }
        val inviteId = customInviteId?.takeIf { it.isNotBlank() }
            ?: "inv_${farmId}_${normalizedRole.lowercase()}_${System.currentTimeMillis()}"
        val expiresAtMillis = System.currentTimeMillis() + ttlMillis.coerceAtLeast(60_000L)
        val inviteRef = farmInvitesRef(farmId).document(inviteId)
        val model = FarmInviteModel(
            inviteId = inviteId,
            farmId = farmId,
            role = normalizedRole,
            createdBy = uid,
            status = "ACTIVE",
            expiresAtMillis = expiresAtMillis,
            consumedBy = "",
            consumedAtMillis = 0L
        )
        return try {
            inviteRef.set(
                mapOf(
                    "inviteId" to inviteId,
                    "farmId" to farmId,
                    "role" to normalizedRole,
                    "createdBy" to uid,
                    "status" to "ACTIVE",
                    "expiresAtMillis" to expiresAtMillis,
                    "consumedBy" to "",
                    "consumedAtMillis" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(model)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, inviteRef.path)
            Result.failure(e)
        }
    }

    /**
     * Atomically consumes a verified invitation `/farms/{farmId}/invites/{inviteId}` and adds
     * the authenticated user to `/farms/{farmId}.members` with the invited role (`WORKER` or `VETERINARIAN`).
     */
    suspend fun acceptFarmInvite(
        farmId: String,
        inviteId: String,
        expectedRole: AppRole,
        displayName: String? = null
    ): Result<Unit> {
        val uid = requireUserId()
        if (isUnassignedFarm(farmId) || inviteId.isBlank()) {
            return Result.failure(IllegalArgumentException("Valid farmId and inviteId are required to join a barn."))
        }
        val normalizedRole = when (expectedRole) {
            AppRole.VETERINARIAN, AppRole.VET_CLINIC -> "VETERINARIAN"
            AppRole.WORKER -> "WORKER"
            else -> return Result.failure(IllegalArgumentException("Only WORKER or VETERINARIAN roles can join via invite."))
        }
        val fRef = farmDocRef(farmId)
        val inviteRef = farmInvitesRef(farmId).document(inviteId)
        return try {
            db.runTransaction { transaction ->
                val inviteSnap = transaction.get(inviteRef)
                if (!inviteSnap.exists()) {
                    throw IllegalStateException("Invitation does not exist: $inviteId")
                }
                val status = inviteSnap.getString("status") ?: ""
                val expiresAt = inviteSnap.getLong("expiresAtMillis") ?: 0L
                val inviteFarmId = inviteSnap.getString("farmId") ?: ""
                val inviteRole = inviteSnap.getString("role") ?: ""
                val nowMillis = System.currentTimeMillis()
                if (status != "ACTIVE") {
                    throw IllegalStateException("Invitation is no longer active (status=$status)")
                }
                if (expiresAt <= nowMillis) {
                    throw IllegalStateException("Invitation has expired")
                }
                if (inviteFarmId != farmId || inviteRole != normalizedRole) {
                    throw IllegalStateException("Invitation role or farmId mismatch")
                }

                transaction.update(
                    inviteRef,
                    mapOf(
                        "status" to "CONSUMED",
                        "consumedBy" to uid,
                        "consumedAtMillis" to nowMillis,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                if (normalizedRole == "WORKER") {
                    transaction.update(
                        fRef,
                        com.google.firebase.firestore.FieldPath.of("members", uid),
                        normalizedRole,
                        "assignedWorkerId",
                        uid,
                        "lastConsumedInviteId",
                        inviteId,
                        "updatedAt",
                        FieldValue.serverTimestamp()
                    )
                } else {
                    transaction.update(
                        fRef,
                        com.google.firebase.firestore.FieldPath.of("members", uid),
                        normalizedRole,
                        "lastConsumedInviteId",
                        inviteId,
                        "updatedAt",
                        FieldValue.serverTimestamp()
                    )
                }
            }.await()

            setUserRoleAndFarm(role = expectedRole, farmId = farmId, displayName = displayName)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, inviteRef.path)
            Result.failure(e)
        }
    }

    /**
     * Streams a shared farm document from `/farms/{farmId}`.
     */
    fun observeFarm(farmId: String): Flow<FarmModel?> {
        if (isUnassignedFarm(farmId)) {
            return kotlinx.coroutines.flow.flowOf(null)
        }
        val docRef = farmDocRef(farmId)
        return docRef.snapshots()
            .map { snapshot ->
                val parsed = if (snapshot.exists()) docToFarm(snapshot) else null
                parsed?.takeUnless { isUnassignedFarm(it.id) }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, docRef.path)
                throw e
            }
    }

    /**
     * Discovers and streams all farms in `/farms` where the authenticated user (`uid`) is a registered member,
     * strictly excluding any unassigned farm or any farm present in `AgritechMockDb.deletedFarmIds`.
     * Enables a new device with empty SharedPreferences to recover all cloud barns owned by or shared with this UID.
     */
    fun observeAllUserFarms(uid: String, primaryFarmId: String = ""): Flow<List<FarmModel>> {
        val cleanUid = uid.trim()
        if (cleanUid.isEmpty()) {
            return if (!isUnassignedFarm(primaryFarmId)) observeFarms(primaryFarmId) else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        val query = db.collection("farms")
            .whereIn("members.$cleanUid", listOf("OWNER", "WORKER", "VETERINARIAN"))
        return query.snapshots()
            .map { querySnap ->
                querySnap.documents
                    .mapNotNull { doc -> if (doc.exists()) docToFarm(doc) else null }
                    .filterNot { isUnassignedFarm(it.id) || it.id in AgritechMockDb.deletedFarmIds }
            }
            .catch { e ->
                if (!isUnassignedFarm(primaryFarmId)) {
                    emitAll(observeFarms(primaryFarmId))
                } else {
                    if (e is Exception) handleFirestoreError(e, OperationType.LIST, "farms")
                    emit(emptyList())
                }
            }
    }

    /**
     * Streams the shared farm as a list for dashboard binding, excluding any unassigned or deleted farm.
     * If `farmId` is unassigned on a fresh device, falls back to `observeAllUserFarms` for the signed-in UID.
     */
    fun observeFarms(farmId: String = "farm_1"): Flow<List<FarmModel>> {
        if (isUnassignedFarm(farmId)) {
            val uid = auth.currentUser?.uid.orEmpty()
            return if (uid.isNotBlank()) {
                observeAllUserFarms(uid, "")
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        val docRef = farmDocRef(farmId)
        return docRef.snapshots()
            .map { snapshot ->
                listOfNotNull(if (snapshot.exists()) docToFarm(snapshot) else null)
                    .filterNot { isUnassignedFarm(it.id) || it.id in AgritechMockDb.deletedFarmIds }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, docRef.path)
                throw e
            }
    }

    /**
     * Assigns user's role and farmId in `/users/{uid}`.
     * Only OWNER roles initialize or update their owned farm directly; WORKER and VETERINARIAN roles
     * must join existing farms via `acceptFarmInvite` (verified `/farms/{farmId}/invites/{inviteId}`).
     */
    suspend fun setUserRoleAndFarm(role: AppRole, farmId: String, displayName: String? = null): Result<Unit> {
        val uid = requireUserId()
        val normalizedRoleName = when (role) {
            AppRole.VETERINARIAN, AppRole.VET_CLINIC -> "VETERINARIAN"
            AppRole.WORKER -> "WORKER"
            else -> "OWNER"
        }
        val isUnassigned = isUnassignedFarm(farmId)
        val profileFarmId = if (isUnassigned) UNASSIGNED_FARM_ID else farmId
        val userDocRef = db.collection("users").document(uid)
        return try {
            val existingUser = userDocRef.get().await()
            if (existingUser.exists()) {
                val updatePayload = mutableMapOf<String, Any>(
                    "uid" to uid,
                    "userId" to uid,
                    "role" to normalizedRoleName,
                    "farmId" to profileFarmId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!displayName.isNullOrBlank()) {
                    updatePayload["displayName"] = displayName
                }
                userDocRef.update(updatePayload).await()
            } else {
                val createPayload = mutableMapOf<String, Any>(
                    "uid" to uid,
                    "userId" to uid,
                    "role" to normalizedRoleName,
                    "farmId" to profileFarmId,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!displayName.isNullOrBlank()) {
                    createPayload["displayName"] = displayName
                }
                userDocRef.set(createPayload).await()
            }

            if (isUnassigned || profileFarmId == UNASSIGNED_FARM_ID || normalizedRoleName != "OWNER") {
                return Result.success(Unit)
            }

            val fRef = farmDocRef(profileFarmId)
            val farmSnap = fRef.get().await()
            if (farmSnap.exists()) {
                val assignedOwnerId = farmSnap.getString("assignedOwnerId") ?: ""
                @Suppress("UNCHECKED_CAST")
                val currentMembers = (farmSnap.get("members") as? Map<String, String>).orEmpty()
                if (assignedOwnerId == uid || currentMembers[uid] == "OWNER") {
                    val updatedMembers = currentMembers + (uid to "OWNER")
                    fRef.update(
                        mapOf(
                            "members" to updatedMembers,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            } else {
                val localFarm = AgritechMockDb.farms.firstOrNull {
                    it.id == profileFarmId && it.id !in AgritechMockDb.deletedFarmIds
                }
                if (localFarm != null) {
                    fRef.set(
                        mapOf(
                            "id" to localFarm.id,
                            "farmId" to localFarm.id,
                            "name" to localFarm.name,
                            "location" to localFarm.location,
                            "capacity" to localFarm.capacity,
                            "assignedOwnerId" to uid,
                            "assignedWorkerId" to localFarm.assignedWorkerId.ifBlank { "worker_1" },
                            "members" to (localFarm.members + (uid to "OWNER")),
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDocRef.path)
            Result.failure(e)
        }
    }

    fun observeUserRole(userId: String): Flow<AppRole> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snapshot ->
                val roleStr = snapshot.getString("role") ?: "OWNER"
                try {
                    AppRole.valueOf(roleStr)
                } catch (_: Exception) {
                    AppRole.OWNER
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, docRef.path)
                throw e
            }
    }

    suspend fun saveCycleRecord(
        cycle: CycleModel,
        cumulativeBirdsSold: Int = cycle.cumulativeBirdsSold,
        cumulativeSoldWeightKg: Double = cycle.cumulativeSoldWeightKg
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = farmCyclesRef(cycle.farmId).document(cycle.id)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "id" to cycle.id,
                        "totalBirdsPlaced" to cycle.totalBirdsPlaced,
                        "targetWeight" to cycle.targetWeight,
                        "targetWeightKg" to cycle.targetWeight,
                        "cumulativeBirdsSold" to cumulativeBirdsSold,
                        "cumulativeSoldWeightKg" to cumulativeSoldWeightKg.coerceAtLeast(0.0),
                        "cumulativeMortality" to cycle.cumulativeMortality.coerceAtLeast(0),
                        "feedInventoryKg" to cycle.feedInventoryKg.coerceAtLeast(0.0),
                        "status" to cycle.status.ifBlank { "active" },
                        "startDate" to cycle.startDate,
                        "chickPrice" to cycle.chickPrice.coerceAtLeast(0.0),
                        "currencyCode" to cycle.currencyCode.ifBlank { "DZD" },
                        "schemaVersion" to cycle.schemaVersion,
                        "sectorId" to cycle.sectorId.ifBlank { "AGRICULTURE" },
                        "branchId" to cycle.branchId.ifBlank { "BROILER" },
                        "customMetrics" to cycle.customMetrics,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "id" to cycle.id,
                        "userId" to uid,
                        "farmId" to cycle.farmId,
                        "totalBirdsPlaced" to cycle.totalBirdsPlaced,
                        "targetWeight" to cycle.targetWeight,
                        "targetWeightKg" to cycle.targetWeight,
                        "cumulativeBirdsSold" to cumulativeBirdsSold,
                        "cumulativeSoldWeightKg" to cumulativeSoldWeightKg.coerceAtLeast(0.0),
                        "cumulativeMortality" to cycle.cumulativeMortality.coerceAtLeast(0),
                        "feedInventoryKg" to cycle.feedInventoryKg.coerceAtLeast(0.0),
                        "status" to cycle.status.ifBlank { "active" },
                        "startDate" to cycle.startDate,
                        "chickPrice" to cycle.chickPrice.coerceAtLeast(0.0),
                        "currencyCode" to cycle.currencyCode.ifBlank { "DZD" },
                        "schemaVersion" to cycle.schemaVersion,
                        "sectorId" to cycle.sectorId.ifBlank { "AGRICULTURE" },
                        "branchId" to cycle.branchId.ifBlank { "BROILER" },
                        "customMetrics" to cycle.customMetrics,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeCumulativeBirdsSold(farmId: String, cycleId: String): Flow<Int> {
        val docRef = farmCyclesRef(farmId).document(cycleId)
        return docRef.snapshots()
            .map { snapshot ->
                (snapshot.getLong("cumulativeBirdsSold") ?: 0L).toInt()
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, docRef.path)
                throw e
            }
    }

    /**
     * Streams active cycles for a specific farm from Firestore (/farms/{farmId}/cycles).
     */
    fun streamCycles(farmId: String, status: String = "active"): Flow<List<CycleModel>> {
        if (isUnassignedFarm(farmId)) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        val colRef = farmCyclesRef(farmId)
        return colRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    docToCycle(doc)
                }.filter {
                    it.farmId == farmId && it.status.equals(status, ignoreCase = true)
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, colRef.path)
                throw e
            }
    }

    fun streamCycles(userId: String, farmId: String, status: String = "active"): Flow<List<CycleModel>> {
        return streamCycles(farmId = farmId, status = status)
    }

    fun observeCycles(farmId: String = "farm_1"): Flow<List<CycleModel>> {
        if (isUnassignedFarm(farmId)) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        val colRef = farmCyclesRef(farmId)
        return colRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    docToCycle(doc)
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, colRef.path)
                throw e
            }
    }

    private fun docToFarm(doc: DocumentSnapshot): FarmModel? {
        val farmId = doc.getString("id") ?: doc.getString("farmId") ?: doc.id
        val name = doc.getString("name") ?: return null
        val location = doc.getString("location") ?: ""
        val capacity = (doc.getLong("capacity") ?: 0L).toInt()
        val assignedWorkerId = doc.getString("assignedWorkerId") ?: ""
        val currencyCode = doc.getString("currencyCode") ?: "DZD"
        val schemaVersion = (doc.getLong("schemaVersion") ?: 2L).toInt()
        val sectorId = doc.getString("sectorId")?.ifBlank { "AGRICULTURE" } ?: "AGRICULTURE"
        val branchId = doc.getString("branchId")?.ifBlank { "BROILER" } ?: "BROILER"
        @Suppress("UNCHECKED_CAST")
        val members = (doc.get("members") as? Map<String, String>).orEmpty()
        @Suppress("UNCHECKED_CAST")
        val customMetrics = (doc.get("customMetrics") as? Map<String, Any?>)
            ?.mapValues { it.value?.toString() ?: "" }
            .orEmpty()
        return FarmModel(
            id = farmId,
            name = name,
            location = location,
            capacity = capacity,
            assignedWorkerId = assignedWorkerId,
            members = members,
            currencyCode = currencyCode,
            schemaVersion = schemaVersion,
            sectorId = sectorId,
            branchId = branchId,
            customMetrics = customMetrics
        )
    }

    private fun docToCycle(doc: DocumentSnapshot): CycleModel? {
        val farmId = doc.getString("farmId") ?: return null
        val totalBirdsPlaced = (doc.getLong("totalBirdsPlaced") ?: 0L).toInt()
        val targetWeight = doc.getDouble("targetWeight") ?: doc.getDouble("targetWeightKg") ?: 2.3
        val status = doc.getString("status") ?: "active"
        val createdAtTs = doc.getTimestamp("createdAt")
        val derivedDate = if (createdAtTs != null) {
            java.time.Instant.ofEpochSecond(createdAtTs.seconds)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString()
        } else {
            java.time.LocalDate.now().toString()
        }
        val startDate = doc.getString("startDate") ?: derivedDate
        val chickPrice = doc.getDouble("chickPrice") ?: 120.0
        val feedInventoryKg = doc.getDouble("feedInventoryKg") ?: 0.0
        val cumulativeBirdsSold = (doc.getLong("cumulativeBirdsSold") ?: 0L).toInt()
        val cumulativeSoldWeightKg = doc.getDouble("cumulativeSoldWeightKg") ?: 0.0
        val cumulativeMortality = (doc.getLong("cumulativeMortality") ?: 0L).toInt()
        val currencyCode = doc.getString("currencyCode") ?: "DZD"
        val schemaVersion = (doc.getLong("schemaVersion") ?: 2L).toInt()
        val sectorId = doc.getString("sectorId")?.ifBlank { "AGRICULTURE" } ?: "AGRICULTURE"
        val branchId = doc.getString("branchId")?.ifBlank { "BROILER" } ?: "BROILER"
        @Suppress("UNCHECKED_CAST")
        val customMetrics = (doc.get("customMetrics") as? Map<String, Any?>)
            ?.mapValues { it.value?.toString() ?: "" }
            .orEmpty()
        return CycleModel(
            id = doc.id,
            farmId = farmId,
            totalBirdsPlaced = totalBirdsPlaced,
            targetWeight = targetWeight,
            status = status,
            startDate = startDate,
            chickPrice = chickPrice,
            feedInventoryKg = feedInventoryKg,
            cumulativeBirdsSold = cumulativeBirdsSold,
            cumulativeSoldWeightKg = cumulativeSoldWeightKg,
            cumulativeMortality = cumulativeMortality,
            currencyCode = currencyCode,
            schemaVersion = schemaVersion,
            sectorId = sectorId,
            branchId = branchId,
            customMetrics = customMetrics
        )
    }

    /**
     * Saves the Worker's DailyReport under `/farms/{farmId}/dailyReports/{reportId}` AND
     * atomically adjusts `feedInventoryKg` and `cumulativeMortality` in `/farms/{farmId}/cycles/{cycleId}`.
     * Requires the target cycle to exist and be `active`.
     */
    suspend fun saveDailyReport(report: DailyReportModel): Result<String> {
        val uid = requireUserId()
        val reportId = report.id.ifBlank { "rep_${report.farmId}_day_${report.dayNumber}" }
        val docRef = farmReportsRef(report.farmId).document(reportId)
        val cycleRef = farmCyclesRef(report.farmId).document(report.cycleId)
        return try {
            db.runTransaction { transaction ->
                val existing = transaction.get(docRef)
                val cycleSnap = transaction.get(cycleRef)

                if (!cycleSnap.exists()) {
                    throw IllegalStateException("Active production cycle does not exist in Firestore: ${report.cycleId}")
                }
                val cycleStatus = cycleSnap.getString("status") ?: "active"
                if (!cycleStatus.equals("active", ignoreCase = true)) {
                    throw IllegalStateException("Cannot save daily report for non-active cycle (${report.cycleId}, status=$cycleStatus)")
                }

                val oldFeedKg = if (existing.exists()) {
                    existing.getDouble("feedIntakeKg") ?: 0.0
                } else {
                    0.0
                }
                val oldMortality = if (existing.exists()) {
                    (existing.getLong("mortalityCount") ?: 0L).toInt()
                } else {
                    0
                }

                val safeGeneralStatus = report.generalStatus.ifBlank { "طبيعية" }
                val safeLitterCondition = report.litterCondition.ifBlank { "طبيعية" }
                if (existing.exists()) {
                    transaction.update(
                        docRef,
                        mapOf(
                            "id" to reportId,
                            "dayNumber" to report.dayNumber,
                            "feedIntakeKg" to report.feedIntakeKg,
                            "waterIntakeLiters" to report.waterIntakeLiters,
                            "mortalityCount" to report.mortalityCount,
                            "temperature" to report.temperature,
                            "gasUnits" to report.gasUnits,
                            "ventilationStatus" to report.ventilationStatus,
                            "generalStatus" to safeGeneralStatus,
                            "litterCondition" to safeLitterCondition,
                            "isLocked" to report.isLocked,
                            "isEdited" to report.isEdited,
                            "previousSummary" to report.previousSummary,
                            "actualAvgWeightKg" to report.actualAvgWeightKg,
                            "medicineGiven" to report.medicineGiven,
                            "medicineSummary" to report.medicineSummary,
                            "withdrawalDays" to report.withdrawalDays,
                            "updatedAtMillis" to report.updatedAtMillis,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    transaction.set(
                        docRef,
                        mapOf(
                            "id" to reportId,
                            "userId" to uid,
                            "farmId" to report.farmId,
                            "cycleId" to report.cycleId,
                            "dayNumber" to report.dayNumber,
                            "feedIntakeKg" to report.feedIntakeKg,
                            "waterIntakeLiters" to report.waterIntakeLiters,
                            "mortalityCount" to report.mortalityCount,
                            "temperature" to report.temperature,
                            "gasUnits" to report.gasUnits,
                            "ventilationStatus" to report.ventilationStatus,
                            "generalStatus" to safeGeneralStatus,
                            "litterCondition" to safeLitterCondition,
                            "isLocked" to report.isLocked,
                            "isEdited" to report.isEdited,
                            "previousSummary" to report.previousSummary,
                            "actualAvgWeightKg" to report.actualAvgWeightKg,
                            "medicineGiven" to report.medicineGiven,
                            "medicineSummary" to report.medicineSummary,
                            "withdrawalDays" to report.withdrawalDays,
                            "updatedAtMillis" to report.updatedAtMillis,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                // Atomic adjustment of feedIntakeKg and cumulativeMortality on the active cycle
                val feedDeltaToDeduct = report.feedIntakeKg - oldFeedKg
                val mortalityDelta = report.mortalityCount - oldMortality
                if (feedDeltaToDeduct != 0.0 || mortalityDelta != 0) {
                    val currentFeedKg = cycleSnap.getDouble("feedInventoryKg") ?: 0.0
                    val updatedFeedKg = (currentFeedKg - feedDeltaToDeduct).coerceAtLeast(0.0)
                    val currentMortality = (cycleSnap.getLong("cumulativeMortality") ?: 0L).toInt()
                    val updatedMortality = (currentMortality + mortalityDelta).coerceAtLeast(0)
                    transaction.update(
                        cycleRef,
                        mapOf(
                            "feedInventoryKg" to updatedFeedKg,
                            "cumulativeMortality" to updatedMortality,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()
            Result.success(reportId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeDailyReports(farmId: String = "farm_1"): Flow<List<DailyReportModel>> {
        if (isUnassignedFarm(farmId)) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        val colRef = farmReportsRef(farmId)
        return colRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    docToDailyReport(doc)
                }.sortedBy { it.dayNumber }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, colRef.path)
                throw e
            }
    }

    suspend fun saveVetNote(note: VetNoteModel): Result<String> {
        val uid = requireUserId()
        val noteId = note.id.ifBlank { "vet_${System.currentTimeMillis()}" }
        val docRef = farmVetNotesRef(note.farmId).document(noteId)
        return try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "id" to noteId,
                        "dayNumber" to note.dayNumber,
                        "medicineName" to note.medicineName,
                        "medication" to note.medicineName,
                        "dosage" to note.dosage,
                        "note" to note.note,
                        "diagnosis" to note.note,
                        "isUrgent" to note.isUrgent,
                        "status" to note.status,
                        "linkedTransactionId" to note.linkedTransactionId,
                        "invoiceAmount" to note.invoiceAmount,
                        "currency" to note.currency,
                        "withdrawalDays" to note.withdrawalDays,
                        "updatedAtMillis" to note.updatedAtMillis,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "id" to noteId,
                        "userId" to uid,
                        "farmId" to note.farmId,
                        "cycleId" to note.cycleId,
                        "dayNumber" to note.dayNumber,
                        "medicineName" to note.medicineName,
                        "medication" to note.medicineName,
                        "dosage" to note.dosage,
                        "note" to note.note,
                        "diagnosis" to note.note,
                        "isUrgent" to note.isUrgent,
                        "status" to note.status,
                        "linkedTransactionId" to note.linkedTransactionId,
                        "invoiceAmount" to note.invoiceAmount,
                        "currency" to note.currency,
                        "withdrawalDays" to note.withdrawalDays,
                        "updatedAtMillis" to note.updatedAtMillis,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(noteId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeVetNotes(farmId: String = "farm_1"): Flow<List<VetNoteModel>> {
        if (isUnassignedFarm(farmId)) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        val colRef = farmVetNotesRef(farmId)
        return colRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    docToVetNote(doc)
                }.sortedByDescending { it.dayNumber }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, colRef.path)
                throw e
            }
    }

    /**
     * Idempotent financial transaction write under `/farms/{farmId}/transactions/{idempotencyKey}`.
     */
    suspend fun recordTransactionIdempotent(
        idempotencyKey: String,
        tx: TransactionModel
    ): Result<Boolean> {
        val uid = requireUserId()
        require(idempotencyKey.isNotBlank()) { "idempotencyKey must not be blank" }
        require(tx.amount > 0.0) { "Transaction amount must be strictly positive" }

        val txRef = farmTransactionsRef(tx.farmId).document(idempotencyKey)
        return try {
            val created = db.runTransaction { transaction ->
                val snapshot = transaction.get(txRef)
                if (snapshot.exists()) {
                    return@runTransaction false
                }
                val payload = buildTransactionPayload(uid, idempotencyKey, tx)
                transaction.set(txRef, payload)
                true
            }.await()
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, txRef.path)
            Result.failure(e)
        }
    }

    /**
     * Atomic Closed-Loop Feed Expense & Inventory Increment Transaction under `/farms/{farmId}/...`:
     * 1. Records the financial feed expense in `/farms/{farmId}/transactions/{idempotencyKey}`.
     * 2. Atomically increments `feedInventoryKg` in `/farms/{farmId}/cycles/{cycleId}`.
     * Fails explicitly if the target cycle does not exist in Firestore (never creates a phantom 12,000-bird cycle).
     */
    suspend fun recordFeedPurchaseAndExpenseIdempotent(
        idempotencyKey: String,
        tx: TransactionModel,
        addedFeedKg: Double
    ): Result<Boolean> {
        val uid = requireUserId()
        require(idempotencyKey.isNotBlank()) { "idempotencyKey must not be blank" }
        require(tx.amount > 0.0) { "Transaction amount must be strictly positive" }
        require(addedFeedKg > 0.0) { "addedFeedKg must be strictly positive" }

        val txRef = farmTransactionsRef(tx.farmId).document(idempotencyKey)
        val cycleRef = farmCyclesRef(tx.farmId).document(tx.cycleId)

        return try {
            val created = db.runTransaction { transaction ->
                val existingTx = transaction.get(txRef)
                if (existingTx.exists()) {
                    return@runTransaction false
                }
                val cycleSnap = transaction.get(cycleRef)
                if (!cycleSnap.exists()) {
                    throw IllegalStateException("Active production cycle does not exist in Firestore: ${tx.cycleId}")
                }
                val currentFeedKg = cycleSnap.getDouble("feedInventoryKg") ?: 0.0

                // 1. Write financial expense to Treasury ledger
                val payload = buildTransactionPayload(uid, idempotencyKey, tx)
                transaction.set(txRef, payload)

                // 2. Atomically increment feedInventoryKg on the existing active Cycle
                transaction.update(
                    cycleRef,
                    mapOf(
                        "feedInventoryKg" to (currentFeedKg + addedFeedKg),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                true
            }.await()
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txRef.path)
            Result.failure(e)
        }
    }

    /**
     * Atomic Vet Medicine Invoice & Prescription Transaction under `/farms/{farmId}/...`:
     * 1. Records the financial medicine expense (`type == "EXPENSE"`, `category == "MEDICATION"`)
     *    in `/farms/{farmId}/transactions/{idempotencyKey}`.
     * 2. Atomically logs the corresponding Vet Note prescription in `/farms/{farmId}/vetNotes/{noteId}`.
     */
    suspend fun recordVetInvoiceAndPrescriptionIdempotent(
        idempotencyKey: String,
        tx: TransactionModel,
        vetNote: VetNoteModel
    ): Result<Boolean> {
        val uid = requireUserId()
        require(idempotencyKey.isNotBlank()) { "idempotencyKey must not be blank" }
        require(tx.amount > 0.0) { "Invoice amount must be strictly positive" }

        val noteId = vetNote.id.ifBlank { "vet_inv_${idempotencyKey}" }
        val txRef = farmTransactionsRef(tx.farmId).document(idempotencyKey)
        val noteRef = farmVetNotesRef(vetNote.farmId).document(noteId)

        return try {
            val created = db.runTransaction { transaction ->
                val existingTx = transaction.get(txRef)
                if (existingTx.exists()) {
                    return@runTransaction false
                }
                val existingNote = transaction.get(noteRef)

                // 1. Write Medicine Invoice Expense to Treasury ledger with strict MEDICATION category and PENDING status
                val pendingStatus = tx.status.ifBlank { "PENDING" }.let { if (it == "APPROVED") "PENDING" else it }
                val medicationTx = tx.copy(
                    type = TransactionType.EXPENSE,
                    category = "MEDICATION",
                    status = pendingStatus
                )
                val txPayload = buildTransactionPayload(uid, idempotencyKey, medicationTx)
                transaction.set(txRef, txPayload)

                // 2. Write Vet Prescription Note to vetNotes subcollection with PENDING status
                if (!existingNote.exists()) {
                    transaction.set(
                        noteRef,
                        mapOf(
                            "id" to noteId,
                            "userId" to uid,
                            "farmId" to vetNote.farmId,
                            "cycleId" to vetNote.cycleId,
                            "dayNumber" to vetNote.dayNumber,
                            "medicineName" to vetNote.medicineName,
                            "medication" to vetNote.medicineName,
                            "dosage" to vetNote.dosage,
                            "note" to vetNote.note,
                            "diagnosis" to vetNote.note,
                            "isUrgent" to vetNote.isUrgent,
                            "status" to pendingStatus,
                            "linkedTransactionId" to idempotencyKey,
                            "invoiceAmount" to medicationTx.amount,
                            "currency" to medicationTx.currency,
                            "withdrawalDays" to vetNote.withdrawalDays,
                            "updatedAtMillis" to vetNote.updatedAtMillis,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                true
            }.await()
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txRef.path)
            Result.failure(e)
        }
    }

    /**
     * Owner confirmation action to approve a pending Vet Medicine Invoice and deduct its amount from the Treasury.
     */
    suspend fun approveVetInvoiceTransaction(
        farmId: String,
        transactionId: String,
        vetNoteId: String? = null,
        approvedBySignature: String = "Signed_By_Owner_${System.currentTimeMillis()}"
    ): Result<Unit> {
        requireUserId()
        val txRef = farmTransactionsRef(farmId).document(transactionId)
        return try {
            db.runTransaction { transaction ->
                val txSnap = transaction.get(txRef)
                val noteRef = if (!vetNoteId.isNullOrBlank()) farmVetNotesRef(farmId).document(vetNoteId) else null
                val noteSnap = if (noteRef != null) transaction.get(noteRef) else null

                if (txSnap.exists()) {
                    transaction.update(
                        txRef,
                        mapOf(
                            "status" to "APPROVED",
                            "approvedBySignature" to approvedBySignature,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                if (noteRef != null && noteSnap != null && noteSnap.exists()) {
                    transaction.update(
                        noteRef,
                        mapOf(
                            "status" to "APPROVED",
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, txRef.path)
            Result.failure(e)
        }
    }

    /**
     * Idempotent Sales Invoice & Treasury Migration executed inside a single atomic Firestore transaction
     * under `/farms/{farmId}/...`.
     * Fails explicitly if the target cycle does not exist or if `invoice.totalBirdsSold > availableBirds`.
     */
    suspend fun saveSalesInvoiceAndMigrateIdempotent(
        farmId: String,
        cycleId: String,
        idempotencyKey: String,
        dateText: String,
        invoice: SalesInvoiceModel
    ): Result<Boolean> {
        val uid = requireUserId()
        require(idempotencyKey.isNotBlank()) { "idempotencyKey must not be blank" }
        require(invoice.totalAmount > 0.0) { "Invoice totalAmount must be > 0" }
        require(invoice.totalNetKg > 0.0) { "Invoice totalNetKg must be > 0" }
        require(invoice.totalBirdsSold > 0) { "Invoice totalBirdsSold must be > 0" }

        val txRef = farmTransactionsRef(farmId).document(idempotencyKey)
        val cycleRef = farmCyclesRef(farmId).document(cycleId)

        return try {
            val created = db.runTransaction { transaction ->
                // 1. Read Phase: Verify idempotency first — if already recorded, return false immediately without re-deducting birds
                val existingTxSnap = transaction.get(txRef)
                if (existingTxSnap.exists() || existingTxSnap.getString("idempotencyKey") == idempotencyKey) {
                    return@runTransaction false
                }

                val cycleSnap = transaction.get(cycleRef)
                if (!cycleSnap.exists()) {
                    throw IllegalStateException("Active production cycle does not exist in Firestore: $cycleId")
                }

                val totalBirdsPlaced = (cycleSnap.getLong("totalBirdsPlaced") ?: 0L).toInt()
                val currentSold = (cycleSnap.getLong("cumulativeBirdsSold") ?: 0L).toInt()
                val currentMortality = (cycleSnap.getLong("cumulativeMortality") ?: 0L).toInt()
                val currentSoldWeightKg = cycleSnap.getDouble("cumulativeSoldWeightKg") ?: 0.0

                val availableBirds = (totalBirdsPlaced - currentMortality - currentSold).coerceAtLeast(0)
                if (!(invoice.totalBirdsSold > 0 && invoice.totalBirdsSold <= availableBirds)) {
                    throw IllegalStateException(
                        "Sold birds (${invoice.totalBirdsSold}) exceed available live birds ($availableBirds) in cycle $cycleId"
                    )
                }

                // 2. Write Phase: Record Treasury Income Transaction with embedded Invoice metadata
                val txModel = TransactionModel(
                    id = idempotencyKey,
                    farmId = farmId,
                    cycleId = cycleId,
                    type = TransactionType.INCOME,
                    category = "tx_chicken_sale",
                    amount = invoice.totalAmount,
                    currency = invoice.currency,
                    date = dateText,
                    invoiceData = invoice
                )
                val txPayload = buildTransactionPayload(uid, idempotencyKey, txModel)
                transaction.set(txRef, txPayload)

                // 3. Write Phase: Update Cycle live bird inventory and sold weight atomically
                transaction.update(
                    cycleRef,
                    mapOf(
                        "cumulativeBirdsSold" to (currentSold + invoice.totalBirdsSold),
                        "cumulativeSoldWeightKg" to (currentSoldWeightKg + invoice.totalNetKg),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                true
            }.await()
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txRef.path)
            Result.failure(e)
        }
    }

    /**
     * Calculates Net Balance strictly from a single source of truth:
     * Sum of APPROVED INCOME documents minus Sum of APPROVED EXPENSE documents.
     * PENDING transactions are excluded from totalExpenses and netBalance until approved by the Owner.
     */
    fun calculateNetBalanceFromTransactions(transactions: List<TransactionModel>): TreasurySummary =
        Companion.calculateNetBalanceFromTransactions(transactions)

    fun observeTransactions(farmId: String = "farm_1"): Flow<List<TransactionModel>> {
        if (isUnassignedFarm(farmId)) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        val colRef = farmTransactionsRef(farmId)
        return colRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    docToTransaction(doc)
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, colRef.path)
                throw e
            }
    }

    private fun buildTransactionPayload(
        uid: String,
        idempotencyKey: String,
        tx: TransactionModel
    ): Map<String, Any> {
        val base = mutableMapOf<String, Any?>(
            "id" to idempotencyKey,
            "userId" to uid,
            "farmId" to tx.farmId,
            "cycleId" to tx.cycleId,
            "type" to tx.type.name,
            "category" to tx.category,
            "amount" to tx.amount,
            "currency" to tx.currency,
            "date" to tx.date,
            "idempotencyKey" to idempotencyKey,
            "status" to tx.status.ifBlank { "APPROVED" },
            "notes" to tx.notes,
            "quantityQuintals" to tx.quantityQuintals,
            "approvedBySignature" to tx.approvedBySignature,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val inv = tx.invoiceData
        if (inv != null) {
            base["farmName"] = inv.farmName
            base["invoiceNumber"] = inv.invoiceNumber
            base["buyerName"] = inv.buyerName
            base["truckPlate"] = inv.truckPlate
            base["birdAgeDays"] = inv.birdAgeDays
            base["totalCrates"] = inv.totalCrates
            base["birdsPerCrate"] = inv.birdsPerCrate
            base["totalBirdsSold"] = inv.totalBirdsSold
            base["totalGrossKg"] = inv.totalGrossKg
            base["totalTareKg"] = inv.totalTareKg
            base["totalNetKg"] = inv.totalNetKg
            base["avgBirdWeightKg"] = inv.avgBirdWeightKg
            base["pricePerKg"] = inv.pricePerKg
            base["tareWeightsCount"] = inv.tareWeightsCount
            base["grossWeightsCount"] = inv.grossWeightsCount
        }
        @Suppress("UNCHECKED_CAST")
        return base.filterValues { it != null } as Map<String, Any>
    }

    private fun docToDailyReport(doc: DocumentSnapshot): DailyReportModel? {
        val farmId = doc.getString("farmId") ?: return null
        val cycleId = doc.getString("cycleId") ?: return null
        val parsedLitter = doc.getString("litterCondition") ?: ""
        val parsedGeneral = doc.getString("generalStatus") ?: parsedLitter.ifBlank { "طبيعية" }
        return DailyReportModel(
            id = doc.id,
            farmId = farmId,
            cycleId = cycleId,
            dayNumber = (doc.getLong("dayNumber") ?: 1L).toInt(),
            feedIntakeKg = doc.getDouble("feedIntakeKg") ?: 0.0,
            waterIntakeLiters = doc.getDouble("waterIntakeLiters") ?: 0.0,
            mortalityCount = (doc.getLong("mortalityCount") ?: 0L).toInt(),
            temperature = doc.getDouble("temperature") ?: 30.0,
            gasUnits = doc.getDouble("gasUnits") ?: 0.0,
            ventilationStatus = doc.getString("ventilationStatus") ?: "جيدة",
            generalStatus = parsedGeneral,
            isLocked = doc.getBoolean("isLocked") ?: true,
            isEdited = doc.getBoolean("isEdited") ?: false,
            previousSummary = doc.getString("previousSummary") ?: "",
            actualAvgWeightKg = doc.getDouble("actualAvgWeightKg") ?: 0.0,
            medicineGiven = doc.getBoolean("medicineGiven") ?: false,
            medicineSummary = doc.getString("medicineSummary") ?: "",
            withdrawalDays = (doc.getLong("withdrawalDays") ?: 0L).toInt(),
            updatedAtMillis = doc.getLong("updatedAtMillis") ?: doc.getTimestamp("updatedAt")?.toDate()?.time ?: 0L,
            litterCondition = parsedLitter
        )
    }

    private fun docToVetNote(doc: DocumentSnapshot): VetNoteModel? {
        val farmId = doc.getString("farmId") ?: return null
        val cycleId = doc.getString("cycleId") ?: return null
        return VetNoteModel(
            id = doc.id,
            farmId = farmId,
            cycleId = cycleId,
            dayNumber = (doc.getLong("dayNumber") ?: 1L).toInt(),
            medicineName = doc.getString("medicineName") ?: doc.getString("medication") ?: "",
            dosage = doc.getString("dosage") ?: "",
            note = doc.getString("note") ?: doc.getString("diagnosis") ?: "",
            isUrgent = doc.getBoolean("isUrgent") ?: false,
            status = doc.getString("status") ?: "APPROVED",
            linkedTransactionId = doc.getString("linkedTransactionId") ?: "",
            invoiceAmount = doc.getDouble("invoiceAmount") ?: 0.0,
            currency = doc.getString("currency") ?: "دج",
            withdrawalDays = (doc.getLong("withdrawalDays") ?: 0L).toInt(),
            updatedAtMillis = doc.getLong("updatedAtMillis") ?: doc.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
        )
    }

    private fun docToTransaction(doc: DocumentSnapshot): TransactionModel? {
        val farmId = doc.getString("farmId") ?: return null
        val cycleId = doc.getString("cycleId") ?: return null
        val typeStr = doc.getString("type") ?: "EXPENSE"
        val type = try {
            TransactionType.valueOf(typeStr)
        } catch (_: Exception) {
            TransactionType.EXPENSE
        }
        val amount = doc.getDouble("amount") ?: 0.0
        val currency = doc.getString("currency") ?: "دج"
        val date = doc.getString("date") ?: "اليوم 1"
        val category = doc.getString("category") ?: ""
        val status = doc.getString("status") ?: "APPROVED"

        val invoiceNumber = doc.getString("invoiceNumber")
        val savedFarmName = doc.getString("farmName")?.takeIf { it.isNotBlank() }
            ?: AgritechMockDb.farms.firstOrNull { it.id == farmId && it.id !in AgritechMockDb.deletedFarmIds }?.name
            ?: ""
        val invoice = if (!invoiceNumber.isNullOrBlank()) {
            SalesInvoiceModel(
                invoiceNumber = invoiceNumber,
                farmName = savedFarmName,
                cycleId = cycleId,
                dateText = date,
                birdAgeDays = (doc.getLong("birdAgeDays") ?: 1L).toInt(),
                buyerName = doc.getString("buyerName") ?: "",
                truckPlate = doc.getString("truckPlate") ?: "",
                totalCrates = (doc.getLong("totalCrates") ?: 0L).toInt(),
                birdsPerCrate = (doc.getLong("birdsPerCrate") ?: 10L).toInt(),
                totalBirdsSold = (doc.getLong("totalBirdsSold") ?: 0L).toInt(),
                totalGrossKg = doc.getDouble("totalGrossKg") ?: 0.0,
                totalTareKg = doc.getDouble("totalTareKg") ?: 0.0,
                totalNetKg = doc.getDouble("totalNetKg") ?: 0.0,
                avgBirdWeightKg = doc.getDouble("avgBirdWeightKg") ?: 0.0,
                pricePerKg = doc.getDouble("pricePerKg") ?: 380.0,
                totalAmount = amount,
                currency = currency,
                tareWeightsCount = (doc.getLong("tareWeightsCount") ?: 0L).toInt(),
                grossWeightsCount = (doc.getLong("grossWeightsCount") ?: 0L).toInt()
            )
        } else null

        return TransactionModel(
            id = doc.id,
            farmId = farmId,
            cycleId = cycleId,
            type = type,
            category = category,
            amount = amount,
            currency = currency,
            date = date,
            invoiceData = invoice,
            status = status,
            notes = doc.getString("notes") ?: "",
            quantityQuintals = doc.getDouble("quantityQuintals") ?: 0.0,
            approvedBySignature = doc.getString("approvedBySignature") ?: ""
        )
    }

    /**
     * Streams chat messages for a specific cycle (merges mock stream with cycle filter).
     */
    fun streamChatMessages(cycleId: String): List<ChatMessageModel> {
        return AgritechMockDb.streamChatMessages(cycleId)
    }

    /**
     * Observes real-time Cloud Firestore chat messages for a specific channel/cycle using the unified contract.
     */
    fun observeChatMessages(channelId: String): Flow<List<ChatMessageModel>> {
        val cleanChannel = channelId.ifBlank { "general_admin" }
        val colRef = db.collection("chatChannels").document(cleanChannel).collection("messages")
        return colRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val text = doc.getString("text") ?: return@mapNotNull null
                    val senderId = doc.getString("senderId") ?: doc.getString("userId") ?: ""
                    val userId = doc.getString("userId") ?: senderId
                    val ts = doc.getString("timestamp") ?: doc.getString("timestampText") ?: "الآن"
                    val statusVal = doc.getString("status") ?: doc.getString("requestStatus") ?: "PENDING"
                    val reqStatusVal = doc.getString("requestStatus") ?: statusVal
                    val linkedTxId = doc.getString("linkedTransactionId") ?: ""
                    val reqAmount = doc.getDouble("requestAmount") ?: 0.0
                    ChatMessageModel(
                        id = doc.id,
                        cycleId = doc.getString("cycleId") ?: cleanChannel,
                        senderId = senderId,
                        userId = userId,
                        senderName = doc.getString("senderName") ?: "",
                        senderRole = doc.getString("senderRole") ?: "",
                        text = text,
                        timestamp = ts,
                        timestampText = doc.getString("timestampText") ?: ts,
                        createdAtMillis = doc.getLong("createdAtMillis")
                            ?: doc.getTimestamp("createdAt")?.toDate()?.time
                            ?: 0L,
                        linkedTransactionId = linkedTxId,
                        linkedVetNoteId = doc.getString("linkedVetNoteId") ?: "",
                        isFinancialRequest = doc.getBoolean("isFinancialRequest")
                            ?: (linkedTxId.isNotBlank() || reqAmount > 0.0),
                        requestCategory = doc.getString("requestCategory") ?: "",
                        requestAmount = reqAmount,
                        requestCurrency = doc.getString("requestCurrency") ?: "دج",
                        status = statusVal,
                        requestStatus = reqStatusVal,
                        deliveryState = "SYNCED"
                    )
                }
            }
            .let { flow ->
                kotlinx.coroutines.flow.flow {
                    var attempt = 0
                    while (true) {
                        var completedNormally = false
                        try {
                            flow.collect { value ->
                                emit(value)
                            }
                            completedNormally = true
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            val hasFirestoreCause = generateSequence<Throwable>(e) { it.cause }
                                .any { it is com.google.firebase.firestore.FirebaseFirestoreException }
                            if (hasFirestoreCause && attempt < 10) {
                                emit(emptyList())
                                attempt++
                                kotlinx.coroutines.delay(150L)
                            } else {
                                throw e
                            }
                        } catch (_: Throwable) {
                            emit(emptyList())
                            if (attempt >= 10) {
                                break
                            }
                            attempt++
                            kotlinx.coroutines.delay(150L)
                        }
                        if (completedNormally) break
                    }
                }
            }
    }

    /**
     * Sends a new team chat message for the specified channel/cycle using the unified message contract.
     * In local guest mode, keeps `deliveryState = "LOCAL_ONLY"`.
     * When authenticated with Firebase, marks `deliveryState = "PENDING_SYNC"`, ensures channel metadata exists,
     * writes the unified message payload, and updates `deliveryState` to `"SYNCED"` or `"FAILED"`.
     */
    fun sendChatMessage(message: ChatMessageModel, farmIdHint: String? = null) {
        val uid = runCatching { auth.currentUser?.uid }.getOrNull()
        val msgId = message.id.ifBlank { "msg_${System.currentTimeMillis()}" }
        val cleanChannel = message.cycleId.ifBlank { "general_admin" }
        val isFinancial = message.isFinancialRequest || message.linkedTransactionId.isNotBlank() || message.requestAmount > 0.0
        val normalizedStatus = if (isFinancial) {
            "PENDING"
        } else {
            message.status.ifBlank { "SENT" }
        }
        val effectiveSenderId = uid ?: message.senderId
        val localMsg = message.copy(
            id = msgId,
            cycleId = cleanChannel,
            senderId = effectiveSenderId,
            userId = effectiveSenderId,
            timestampText = message.timestampText.ifBlank { message.timestamp },
            isFinancialRequest = isFinancial,
            status = normalizedStatus,
            requestStatus = normalizedStatus,
            deliveryState = if (uid != null) "PENDING_SYNC" else "LOCAL_ONLY"
        )
        AgritechMockDb.sendChatMessage(localMsg)

        if (uid == null) return

        try {
            val directExec = java.util.concurrent.Executor { it.run() }
            val channelRef = db.collection("chatChannels").document(cleanChannel)
            val msgRef = channelRef.collection("messages").document(msgId)
            val strippedCycleId = cleanChannel.removeSuffix("_worker").removeSuffix("_vet")
            val resolvedFarmId = farmIdHint?.takeIf { !isUnassignedFarm(it) }
                ?: AgritechMockDb.cycles.firstOrNull { it.id == cleanChannel || it.id == strippedCycleId }?.farmId
                ?: ""
            val farmOwnerUid = if (resolvedFarmId.isNotBlank()) {
                val localFarm = AgritechMockDb.farms.firstOrNull { it.id == resolvedFarmId }
                localFarm?.members?.entries?.firstOrNull { it.value.equals("OWNER", ignoreCase = true) }?.key
            } else null
            val participantsList = listOfNotNull(uid, farmOwnerUid).distinct()

            val channelMeta = mutableMapOf<String, Any>(
                "channelId" to cleanChannel,
                "participants" to participantsList,
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (resolvedFarmId.isNotBlank()) {
                channelMeta["farmId"] = resolvedFarmId
            }
            if (!farmOwnerUid.isNullOrBlank()) {
                channelMeta["ownerId"] = farmOwnerUid
            } else if (resolvedFarmId.isBlank()) {
                channelMeta["ownerId"] = uid
            }

            val messagePayload = mutableMapOf<String, Any>(
                "id" to msgId,
                "userId" to uid,
                "senderId" to uid,
                "cycleId" to cleanChannel,
                "senderName" to localMsg.senderName.ifBlank { "Member" },
                "senderRole" to localMsg.senderRole.ifBlank { "OWNER" },
                "text" to localMsg.text,
                "timestamp" to localMsg.timestamp,
                "timestampText" to localMsg.timestampText,
                "createdAtMillis" to localMsg.createdAtMillis,
                "linkedTransactionId" to localMsg.linkedTransactionId,
                "linkedVetNoteId" to localMsg.linkedVetNoteId,
                "isFinancialRequest" to localMsg.isFinancialRequest,
                "requestCategory" to localMsg.requestCategory,
                "requestAmount" to localMsg.requestAmount,
                "requestCurrency" to localMsg.requestCurrency,
                "status" to localMsg.status,
                "requestStatus" to localMsg.requestStatus,
                "deliveryState" to "SYNCED",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (resolvedFarmId.isNotBlank()) {
                messagePayload["farmId"] = resolvedFarmId
            }

            channelRef.get()
                .addOnSuccessListener(directExec) { channelSnap ->
                    if (!channelSnap.exists()) {
                        val batch = db.batch()
                        batch.set(channelRef, channelMeta)
                        batch.set(msgRef, messagePayload)
                        batch.commit()
                            .addOnSuccessListener(directExec) {
                                AgritechMockDb.updateChatMessageDeliveryState(msgId, "SYNCED")
                            }
                            .addOnFailureListener(directExec) {
                                AgritechMockDb.updateChatMessageDeliveryState(msgId, "FAILED")
                            }
                    } else {
                        msgRef.set(messagePayload)
                            .addOnSuccessListener(directExec) {
                                AgritechMockDb.updateChatMessageDeliveryState(msgId, "SYNCED")
                            }
                            .addOnFailureListener(directExec) {
                                AgritechMockDb.updateChatMessageDeliveryState(msgId, "FAILED")
                            }
                    }
                }
                .addOnFailureListener(directExec) {
                    val batch = db.batch()
                    batch.set(channelRef, channelMeta)
                    batch.set(msgRef, messagePayload)
                    batch.commit()
                        .addOnSuccessListener(directExec) {
                            AgritechMockDb.updateChatMessageDeliveryState(msgId, "SYNCED")
                        }
                        .addOnFailureListener(directExec) {
                            AgritechMockDb.updateChatMessageDeliveryState(msgId, "FAILED")
                        }
                }
        } catch (_: Exception) {
            AgritechMockDb.updateChatMessageDeliveryState(msgId, "FAILED")
        }
    }

    /**
     * Resets the specified cycle and clears all daily reports, financial transactions, and vet notes/withdrawal periods
     * for the barn in both local state and Cloud Firestore.
     */
    suspend fun resetCycleAndClearBarn(farmId: String, cycleId: String): Result<Unit> {
        if (farmId == "personal_worker_ledger") return Result.success(Unit)
        val resetCycle = AgritechMockDb.resetCycleAndClearBarn(farmId, cycleId)
        val uid = auth.currentUser?.uid ?: return Result.success(Unit)
        return try {
            val reportsSnap = farmReportsRef(farmId).get().await()
            for (doc in reportsSnap.documents) {
                val docCycleId = doc.getString("cycleId").orEmpty()
                if (docCycleId == cycleId || docCycleId.isBlank()) {
                    runCatching { doc.reference.delete().await() }
                }
            }

            val txsSnap = farmTransactionsRef(farmId).get().await()
            for (doc in txsSnap.documents) {
                val docCycleId = doc.getString("cycleId").orEmpty()
                if (docCycleId == cycleId || docCycleId.isBlank()) {
                    runCatching { doc.reference.delete().await() }
                }
            }

            val vetSnap = farmVetNotesRef(farmId).get().await()
            for (doc in vetSnap.documents) {
                val docCycleId = doc.getString("cycleId").orEmpty()
                if (docCycleId == cycleId || docCycleId.isBlank()) {
                    runCatching { doc.reference.delete().await() }
                }
            }

            if (resetCycle != null) {
                saveCycleRecord(resetCycle)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetCycleAndClearBarn(
        farmId: String,
        defaultBirds: Int = 0,
        defaultTargetWeight: Double = 2.5
    ): Result<Unit> {
        if (farmId.isBlank() || farmId == "personal_worker_ledger") return Result.success(Unit)
        val activeCycle = AgritechMockDb.cycles.firstOrNull {
            it.farmId == farmId && it.status.equals("active", ignoreCase = true)
        } ?: AgritechMockDb.cycles.firstOrNull { it.farmId == farmId }
        val cycleId = activeCycle?.id.orEmpty()
        return resetCycleAndClearBarn(farmId = farmId, cycleId = cycleId)
    }

    /**
     * Permanently deletes a farm and its associated subcollections (cycles, dailyReports, transactions, vetNotes)
     * from both local state (`AgritechMockDb`) and Cloud Firestore (`/farms/{farmId}`).
     */
    suspend fun deleteFarmRecord(farmId: String): Result<Unit> {
        if (farmId.isBlank() || farmId == "personal_worker_ledger") return Result.success(Unit)
        AgritechMockDb.deleteFarmCompletely(farmId)
        val uid = auth.currentUser?.uid ?: return Result.success(Unit)
        return try {
            val reportsSnap = farmReportsRef(farmId).get().await()
            for (doc in reportsSnap.documents) {
                runCatching { doc.reference.delete().await() }
            }
            val txsSnap = farmTransactionsRef(farmId).get().await()
            for (doc in txsSnap.documents) {
                runCatching { doc.reference.delete().await() }
            }
            val vetSnap = farmVetNotesRef(farmId).get().await()
            for (doc in vetSnap.documents) {
                runCatching { doc.reference.delete().await() }
            }
            val cyclesSnap = farmCyclesRef(farmId).get().await()
            for (doc in cyclesSnap.documents) {
                runCatching { doc.reference.delete().await() }
            }
            runCatching { farmDocRef(farmId).delete().await() }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


