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

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun farmDocRef(farmId: String) =
        db.collection("farms").document(farmId)

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
                val mergedMembers = existingMembers + farm.members + (uid to roleString)
                docRef.update(
                    mapOf(
                        "name" to farm.name,
                        "location" to farm.location,
                        "capacity" to farm.capacity,
                        "assignedWorkerId" to farm.assignedWorkerId,
                        "currencyCode" to farm.currencyCode.ifBlank { "DZD" },
                        "members" to mergedMembers,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val initialMembers = farm.members + (uid to roleString)
                docRef.set(
                    mapOf(
                        "farmId" to farm.id,
                        "name" to farm.name,
                        "location" to farm.location,
                        "capacity" to farm.capacity,
                        "assignedWorkerId" to farm.assignedWorkerId,
                        "currencyCode" to farm.currencyCode.ifBlank { "DZD" },
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
     * Streams a shared farm document from `/farms/{farmId}`.
     */
    fun observeFarm(farmId: String): Flow<FarmModel?> {
        val docRef = farmDocRef(farmId)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) docToFarm(snapshot) else null
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, docRef.path)
                throw e
            }
    }

    /**
     * Streams the shared farm as a list for dashboard binding.
     */
    fun observeFarms(farmId: String = "farm_1"): Flow<List<FarmModel>> {
        val docRef = farmDocRef(farmId)
        return docRef.snapshots()
            .map { snapshot ->
                listOfNotNull(if (snapshot.exists()) docToFarm(snapshot) else null)
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, docRef.path)
                throw e
            }
    }

    /**
     * Assigns user's role and farmId in `/users/{uid}` AND registers the user in `/farms/{farmId}.members`.
     */
    suspend fun setUserRoleAndFarm(role: AppRole, farmId: String, displayName: String? = null): Result<Unit> {
        val uid = requireUserId()
        val normalizedRoleName = when (role) {
            AppRole.VETERINARIAN, AppRole.VET_CLINIC -> "VETERINARIAN"
            AppRole.WORKER -> "WORKER"
            else -> "OWNER"
        }
        val userDocRef = db.collection("users").document(uid)
        val fRef = farmDocRef(farmId)
        return try {
            val existingUser = userDocRef.get().await()
            if (existingUser.exists()) {
                val updatePayload = mutableMapOf<String, Any>(
                    "role" to normalizedRoleName,
                    "farmId" to farmId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!displayName.isNullOrBlank()) {
                    updatePayload["displayName"] = displayName
                }
                userDocRef.update(updatePayload).await()
            } else {
                val createPayload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "role" to normalizedRoleName,
                    "farmId" to farmId,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!displayName.isNullOrBlank()) {
                    createPayload["displayName"] = displayName
                }
                userDocRef.set(createPayload).await()
            }

            // Also register membership in the shared /farms/{farmId} document
            val farmSnap = fRef.get().await()
            if (farmSnap.exists()) {
                @Suppress("UNCHECKED_CAST")
                val currentMembers = (farmSnap.get("members") as? Map<String, String>).orEmpty()
                val updatedMembers = currentMembers + (uid to normalizedRoleName)
                fRef.update(
                    mapOf(
                        "members" to updatedMembers,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                fRef.set(
                    mapOf(
                        "farmId" to farmId,
                        "name" to "عنبر البركة النموذجي",
                        "location" to "المدية - سيدي نعمان",
                        "capacity" to 12000,
                        "assignedWorkerId" to if (role == AppRole.WORKER) uid else "worker_1",
                        "members" to mapOf(uid to normalizedRoleName),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
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
                        "totalBirdsPlaced" to cycle.totalBirdsPlaced,
                        "targetWeightKg" to cycle.targetWeight,
                        "cumulativeBirdsSold" to cumulativeBirdsSold,
                        "cumulativeSoldWeightKg" to cumulativeSoldWeightKg.coerceAtLeast(0.0),
                        "feedInventoryKg" to cycle.feedInventoryKg.coerceAtLeast(0.0),
                        "currencyCode" to cycle.currencyCode.ifBlank { "DZD" },
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "farmId" to cycle.farmId,
                        "totalBirdsPlaced" to cycle.totalBirdsPlaced,
                        "targetWeightKg" to cycle.targetWeight,
                        "cumulativeBirdsSold" to cumulativeBirdsSold,
                        "cumulativeSoldWeightKg" to cumulativeSoldWeightKg.coerceAtLeast(0.0),
                        "feedInventoryKg" to cycle.feedInventoryKg.coerceAtLeast(0.0),
                        "currencyCode" to cycle.currencyCode.ifBlank { "DZD" },
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
        val farmId = doc.getString("farmId") ?: doc.id
        val name = doc.getString("name") ?: return null
        val location = doc.getString("location") ?: ""
        val capacity = (doc.getLong("capacity") ?: 0L).toInt()
        val assignedWorkerId = doc.getString("assignedWorkerId") ?: ""
        val currencyCode = doc.getString("currencyCode") ?: "DZD"
        @Suppress("UNCHECKED_CAST")
        val members = (doc.get("members") as? Map<String, String>).orEmpty()
        return FarmModel(
            id = farmId,
            name = name,
            location = location,
            capacity = capacity,
            assignedWorkerId = assignedWorkerId,
            members = members,
            currencyCode = currencyCode
        )
    }

    private fun docToCycle(doc: DocumentSnapshot): CycleModel? {
        val farmId = doc.getString("farmId") ?: return null
        val totalBirdsPlaced = (doc.getLong("totalBirdsPlaced") ?: 0L).toInt()
        val targetWeight = doc.getDouble("targetWeightKg") ?: 2.3
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
        val currencyCode = doc.getString("currencyCode") ?: "DZD"
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
            currencyCode = currencyCode
        )
    }

    /**
     * Saves the Worker's DailyReport under `/farms/{farmId}/dailyReports/{reportId}` AND
     * atomically decrements `feedInventoryKg` in `/farms/{farmId}/cycles/{cycleId}`.
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

                val oldFeedKg = if (existing.exists()) {
                    existing.getDouble("feedIntakeKg") ?: 0.0
                } else {
                    0.0
                }

                if (existing.exists()) {
                    transaction.update(
                        docRef,
                        mapOf(
                            "dayNumber" to report.dayNumber,
                            "feedIntakeKg" to report.feedIntakeKg,
                            "waterIntakeLiters" to report.waterIntakeLiters,
                            "mortalityCount" to report.mortalityCount,
                            "temperature" to report.temperature,
                            "gasUnits" to report.gasUnits,
                            "ventilationStatus" to report.ventilationStatus,
                            "litterCondition" to report.generalStatus,
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
                            "litterCondition" to report.generalStatus,
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

                // Atomic adjustment of feedIntakeKg on active cycle's feedInventoryKg
                val feedDeltaToDeduct = report.feedIntakeKg - oldFeedKg
                if (cycleSnap.exists() && feedDeltaToDeduct != 0.0) {
                    val currentFeedKg = cycleSnap.getDouble("feedInventoryKg") ?: 0.0
                    val updatedFeedKg = (currentFeedKg - feedDeltaToDeduct).coerceAtLeast(0.0)
                    transaction.update(
                        cycleRef,
                        mapOf(
                            "feedInventoryKg" to updatedFeedKg,
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
                        "dayNumber" to note.dayNumber,
                        "medication" to note.medicineName,
                        "dosage" to note.dosage,
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
                        "userId" to uid,
                        "farmId" to note.farmId,
                        "cycleId" to note.cycleId,
                        "dayNumber" to note.dayNumber,
                        "medication" to note.medicineName,
                        "dosage" to note.dosage,
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
                val currentFeedKg = if (cycleSnap.exists()) {
                    cycleSnap.getDouble("feedInventoryKg") ?: 0.0
                } else {
                    0.0
                }

                // 1. Write financial expense to Treasury ledger
                val payload = buildTransactionPayload(uid, idempotencyKey, tx)
                transaction.set(txRef, payload)

                // 2. Atomically increment feedInventoryKg on the active Cycle
                if (cycleSnap.exists()) {
                    transaction.update(
                        cycleRef,
                        mapOf(
                            "feedInventoryKg" to (currentFeedKg + addedFeedKg),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    transaction.set(
                        cycleRef,
                        mapOf(
                            "userId" to uid,
                            "farmId" to tx.farmId,
                            "totalBirdsPlaced" to 12000,
                            "targetWeightKg" to 2.3,
                            "cumulativeBirdsSold" to 0,
                            "cumulativeSoldWeightKg" to 0.0,
                            "feedInventoryKg" to addedFeedKg,
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
                            "userId" to uid,
                            "farmId" to vetNote.farmId,
                            "cycleId" to vetNote.cycleId,
                            "dayNumber" to vetNote.dayNumber,
                            "medication" to vetNote.medicineName,
                            "dosage" to vetNote.dosage,
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
                // 1. Read Phase: Verify idempotency
                val existingTx = transaction.get(txRef)
                if (existingTx.exists()) {
                    return@runTransaction false
                }

                val cycleSnap = transaction.get(cycleRef)
                val currentSold = if (cycleSnap.exists()) {
                    (cycleSnap.getLong("cumulativeBirdsSold") ?: 0L).toInt()
                } else {
                    0
                }
                val currentSoldWeightKg = if (cycleSnap.exists()) {
                    cycleSnap.getDouble("cumulativeSoldWeightKg") ?: 0.0
                } else {
                    0.0
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
                if (cycleSnap.exists()) {
                    transaction.update(
                        cycleRef,
                        mapOf(
                            "cumulativeBirdsSold" to (currentSold + invoice.totalBirdsSold),
                            "cumulativeSoldWeightKg" to (currentSoldWeightKg + invoice.totalNetKg),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    transaction.set(
                        cycleRef,
                        mapOf(
                            "userId" to uid,
                            "farmId" to farmId,
                            "totalBirdsPlaced" to 12000,
                            "targetWeightKg" to 2.3,
                            "cumulativeBirdsSold" to invoice.totalBirdsSold,
                            "cumulativeSoldWeightKg" to invoice.totalNetKg,
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
     * Calculates Net Balance strictly from a single source of truth:
     * Sum of APPROVED INCOME documents minus Sum of APPROVED EXPENSE documents.
     * PENDING transactions are excluded from totalExpenses and netBalance until approved by the Owner.
     */
    fun calculateNetBalanceFromTransactions(transactions: List<TransactionModel>): TreasurySummary =
        Companion.calculateNetBalanceFromTransactions(transactions)

    fun observeTransactions(farmId: String = "farm_1"): Flow<List<TransactionModel>> {
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
            generalStatus = doc.getString("litterCondition") ?: "طبيعية",
            isEdited = doc.getBoolean("isEdited") ?: false,
            previousSummary = doc.getString("previousSummary") ?: "",
            actualAvgWeightKg = doc.getDouble("actualAvgWeightKg") ?: 0.0,
            medicineGiven = doc.getBoolean("medicineGiven") ?: false,
            medicineSummary = doc.getString("medicineSummary") ?: "",
            withdrawalDays = (doc.getLong("withdrawalDays") ?: 0L).toInt(),
            updatedAtMillis = doc.getLong("updatedAtMillis") ?: doc.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
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
            medicineName = doc.getString("medication") ?: "",
            dosage = doc.getString("dosage") ?: "",
            note = doc.getString("diagnosis") ?: "",
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
        val invoice = if (!invoiceNumber.isNullOrBlank()) {
            SalesInvoiceModel(
                invoiceNumber = invoiceNumber,
                farmName = "عنبر الخيرات والبركة 1",
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
     * Observes real-time Cloud Firestore chat messages for a specific channel/cycle.
     */
    fun observeChatMessages(channelId: String): Flow<List<ChatMessageModel>> {
        val cleanChannel = channelId.ifBlank { "general_admin" }
        val colRef = db.collection("chatChannels").document(cleanChannel).collection("messages")
        return colRef.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val text = doc.getString("text") ?: return@mapNotNull null
                    ChatMessageModel(
                        id = doc.id,
                        cycleId = doc.getString("cycleId") ?: cleanChannel,
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "",
                        senderRole = doc.getString("senderRole") ?: "",
                        text = text,
                        timestamp = doc.getString("timestamp") ?: "الآن",
                        linkedTransactionId = doc.getString("linkedTransactionId") ?: "",
                        requestCategory = doc.getString("requestCategory") ?: "",
                        requestAmount = doc.getDouble("requestAmount") ?: 0.0,
                        requestCurrency = doc.getString("requestCurrency") ?: "دج",
                        status = doc.getString("status") ?: "PENDING"
                    )
                }
            }
            .catch { emit(emptyList()) }
    }

    /**
     * Sends a new team chat message for the specified cycle to both reactive local state and Cloud Firestore.
     */
    fun sendChatMessage(message: ChatMessageModel) {
        AgritechMockDb.sendChatMessage(message)
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val cleanChannel = message.cycleId.ifBlank { "general_admin" }
                val msgId = message.id.ifBlank { "msg_${System.currentTimeMillis()}" }
                db.collection("chatChannels")
                    .document(cleanChannel)
                    .collection("messages")
                    .document(msgId)
                    .set(
                        mapOf(
                            "id" to msgId,
                            "userId" to uid,
                            "cycleId" to cleanChannel,
                            "senderId" to message.senderId,
                            "senderName" to message.senderName,
                            "senderRole" to message.senderRole,
                            "text" to message.text,
                            "timestamp" to message.timestamp,
                            "linkedTransactionId" to message.linkedTransactionId,
                            "requestCategory" to message.requestCategory,
                            "requestAmount" to message.requestAmount,
                            "requestCurrency" to message.requestCurrency,
                            "status" to message.status,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
            }
        } catch (_: Exception) {
        }
    }
}
