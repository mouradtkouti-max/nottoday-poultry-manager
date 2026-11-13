package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.utils.AppLanguage
import com.example.utils.Translations
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

enum class AuthSessionMode {
    AUTHENTICATED,
    LOCAL_GUEST
}

enum class AuthProviderTab {
    GOOGLE,
    EMAIL,
    PHONE,
    SOCIAL
}

object AgritechAuthHelper {

    @Volatile
    var lastAuthProvider: String = "local_guest"
        private set

    @Volatile
    var lastAuthIdentifier: String = ""
        private set

    fun isSignedIn(): Boolean {
        return try {
            FirebaseAuth.getInstance().currentUser != null
        } catch (_: Exception) {
            false
        }
    }

    fun currentUser(): FirebaseUser? {
        return try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }
    }

    fun currentUserUid(): String? {
        return try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Returns true when a signed-in user uses Email/Password ("password" provider) and has not yet
     * verified their email link (`!user.isEmailVerified`). Google, Phone OTP, Facebook, and X
     * providers are already verified by their upstream identity/OTP flow.
     */
    fun isEmailVerificationPending(user: FirebaseUser? = currentUser()): Boolean {
        if (user == null) return false
        val usesPasswordProvider = user.providerData.any { it.providerId == "password" }
        val hasVerifiedOAuthOrPhone = user.providerData.any {
            it.providerId in setOf("google.com", "phone", "facebook.com", "twitter.com")
        }
        return usesPasswordProvider && !hasVerifiedOAuthOrPhone && !user.isEmailVerified
    }

    fun isUserFullyVerified(): Boolean {
        val user = currentUser() ?: return false
        return !isEmailVerificationPending(user)
    }

    fun syncMetadataFromFirebaseUser(user: FirebaseUser? = currentUser()) {
        if (user == null) return
        val providerId = user.providerData
            .firstOrNull { it.providerId != "firebase" }
            ?.providerId
            ?: "firebase"
        lastAuthProvider = when (providerId) {
            "google.com" -> "google"
            "phone" -> "phone"
            "facebook.com" -> "facebook"
            "twitter.com" -> "x"
            "password" -> "email"
            else -> providerId
        }
        lastAuthIdentifier = user.email?.takeIf { it.isNotBlank() }
            ?: user.phoneNumber?.takeIf { it.isNotBlank() }
            ?: user.displayName?.takeIf { it.isNotBlank() }
            ?: user.uid
    }

    fun currentSessionMode(): AuthSessionMode {
        return if (isSignedIn()) AuthSessionMode.AUTHENTICATED else AuthSessionMode.LOCAL_GUEST
    }

    /**
     * Resolves a deterministic, isolated local storage namespace key so authenticated accounts
     * (`auth_uid_<firebaseUid>`) and local guest sessions (`local_guest_default` or `local_user_<id>`)
     * never share or leak local state.
     */
    fun resolveActiveStorageIdentityKey(explicitSessionUid: String? = null): String {
        val cleanedExplicit = explicitSessionUid?.trim().orEmpty()
        if (cleanedExplicit.isNotBlank() && cleanedExplicit != "local_guest_default") {
            val sanitized = cleanedExplicit.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            return if (sanitized.startsWith("auth_uid_") || sanitized.startsWith("local_")) {
                sanitized
            } else {
                "local_user_$sanitized"
            }
        }
        val firebaseUid = currentUserUid()
        if (!firebaseUid.isNullOrBlank()) {
            val sanitized = firebaseUid.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            return "auth_uid_$sanitized"
        }
        return "local_guest_default"
    }

    fun isLikelyPhoneNumber(rawInput: String): Boolean {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty() || trimmed.contains("@")) return false
        return trimmed.all { it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' }
    }

    fun formatNormalizedIdentifier(rawInput: String, countryDialCode: String = "+213"): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return "${countryDialCode}550123456"
        return if (isLikelyPhoneNumber(trimmed)) {
            val cleanedDial = if (countryDialCode.startsWith("+")) countryDialCode else "+$countryDialCode"
            if (trimmed.startsWith("+")) {
                "+" + trimmed.filter { it.isDigit() }
            } else {
                val digitsOnly = trimmed.filter { it.isDigit() }.removePrefix("0")
                "$cleanedDial$digitsOnly"
            }
        } else {
            trimmed
        }
    }

    fun isValidE164PhoneNumber(e164Phone: String): Boolean {
        if (!e164Phone.startsWith("+")) return false
        val digits = e164Phone.drop(1)
        return digits.length in 8..15 && digits.all { it.isDigit() }
    }

    fun isValidEmailAddress(email: String): Boolean {
        val trimmed = email.trim()
        val atIdx = trimmed.indexOf('@')
        val dotIdx = trimmed.lastIndexOf('.')
        return trimmed.length >= 5 && atIdx > 0 && dotIdx > atIdx + 1 && dotIdx < trimmed.length - 1 && !trimmed.contains(" ")
    }

    fun resolveLocalizedAuthError(error: Throwable?, lang: AppLanguage): String {
        if (error is FirebaseNetworkException) {
            return Translations.tr("auth_err_network", lang)
        }
        val code = (error as? FirebaseAuthException)?.errorCode.orEmpty()
        return when (code) {
            "ERROR_OPERATION_NOT_ALLOWED" -> Translations.tr("auth_err_provider_disabled", lang)
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_NOT_FOUND" -> Translations.tr("auth_err_invalid_credentials", lang)
            "ERROR_EMAIL_ALREADY_IN_USE" -> Translations.tr("auth_err_email_in_use", lang)
            "ERROR_WEAK_PASSWORD" -> Translations.tr("auth_err_password_short", lang)
            "ERROR_INVALID_EMAIL" -> Translations.tr("auth_err_invalid_email", lang)
            "ERROR_INVALID_PHONE_NUMBER" -> Translations.tr("auth_err_invalid_phone", lang)
            "ERROR_INVALID_VERIFICATION_CODE",
            "ERROR_SESSION_EXPIRED" -> Translations.tr("auth_err_invalid_otp", lang)
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> Translations.tr("auth_err_account_exists_diff_provider", lang)
            "ERROR_TOO_MANY_REQUESTS" -> Translations.tr("auth_err_too_many_requests", lang)
            else -> {
                val rawMsg = error?.localizedMessage?.trim().orEmpty()
                when {
                    rawMsg.contains("cancelled", ignoreCase = true) ||
                        rawMsg.contains("canceled", ignoreCase = true) ->
                        Translations.tr("auth_err_cancelled", lang)
                    rawMsg.contains("operation is not allowed", ignoreCase = true) ->
                        Translations.tr("auth_err_provider_disabled", lang)
                    rawMsg.isNotBlank() -> rawMsg
                    else -> Translations.tr("auth_err_generic", lang)
                }
            }
        }
    }

    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        if (isSignedIn() && isUserFullyVerified()) {
            syncMetadataFromFirebaseUser()
            onAuthSuccess()
            return
        }
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                    if (isSignedIn()) {
                        syncMetadataFromFirebaseUser()
                        onAuthSuccess()
                    } else {
                        onUnauthenticated()
                    }
                } else {
                    onUnauthenticated()
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Silent auto sign-in cancelled: ${e.message}", e)
                onUnauthenticated()
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    fun onGoogleSignInClicked(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit,
        scope: CoroutineScope,
        onAuthCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onAuthError("Google Sign-In configuration (default_web_client_id) is missing.")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val activityContext = context as? Activity ?: context
                val result = credentialManager.getCredential(activityContext, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                    if (isSignedIn()) {
                        syncMetadataFromFirebaseUser()
                        onAuthSuccess()
                    } else {
                        onAuthError("Firebase Google Sign-In completed without an active user session.")
                    }
                } else {
                    onAuthError("Unsupported credential type returned by Google Sign-In.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.w("Auth", "Google Sign-In failed: ${e.message}", e)
                onAuthError(e.localizedMessage ?: "Google Sign-In failed or is unavailable on this device.")
            }
        }
    }

    fun signInWithEmailPassword(
        email: String,
        password: String,
        isSignUp: Boolean,
        lang: AppLanguage,
        scope: CoroutineScope,
        onVerifiedSuccess: () -> Unit,
        onVerificationRequired: (emailAddress: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedEmail = email.trim()
        scope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val result = if (isSignUp) {
                    auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
                } else {
                    auth.signInWithEmailAndPassword(trimmedEmail, password).await()
                }
                val user = result.user
                if (user == null) {
                    onError(Translations.tr("auth_err_generic", lang))
                    return@launch
                }
                if (isEmailVerificationPending(user)) {
                    try {
                        user.sendEmailVerification().await()
                    } catch (mailEx: Exception) {
                        Log.w("Auth", "Verification email dispatch warning: ${mailEx.message}")
                    }
                    syncMetadataFromFirebaseUser(user)
                    onVerificationRequired(user.email ?: trimmedEmail)
                } else {
                    syncMetadataFromFirebaseUser(user)
                    onVerifiedSuccess()
                }
            } catch (e: Exception) {
                Log.w("Auth", "Email auth failed: ${e.message}", e)
                onError(resolveLocalizedAuthError(e, lang))
            }
        }
    }

    fun resendVerificationEmail(
        lang: AppLanguage,
        scope: CoroutineScope,
        onSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = currentUser()
        if (user == null) {
            onError(Translations.tr("auth_err_generic", lang))
            return
        }
        scope.launch {
            try {
                user.sendEmailVerification().await()
                onSent()
            } catch (e: Exception) {
                onError(resolveLocalizedAuthError(e, lang))
            }
        }
    }

    fun checkEmailVerificationStatus(
        lang: AppLanguage,
        scope: CoroutineScope,
        onVerified: () -> Unit,
        onStillPending: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = currentUser()
        if (user == null) {
            onError(Translations.tr("auth_err_generic", lang))
            return
        }
        scope.launch {
            try {
                user.reload().await()
                val refreshed = currentUser()
                if (refreshed != null && !isEmailVerificationPending(refreshed)) {
                    syncMetadataFromFirebaseUser(refreshed)
                    onVerified()
                } else {
                    onStillPending()
                }
            } catch (e: Exception) {
                onError(resolveLocalizedAuthError(e, lang))
            }
        }
    }

    fun sendPasswordResetEmail(
        email: String,
        lang: AppLanguage,
        scope: CoroutineScope,
        onSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmedEmail = email.trim()
        scope.launch {
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(trimmedEmail).await()
                onSent()
            } catch (e: Exception) {
                onError(resolveLocalizedAuthError(e, lang))
            }
        }
    }

    fun sendPhoneVerificationCode(
        activity: Activity,
        e164Phone: String,
        resendToken: PhoneAuthProvider.ForceResendingToken?,
        lang: AppLanguage,
        scope: CoroutineScope,
        onCodeSent: (verificationId: String, token: PhoneAuthProvider.ForceResendingToken?) -> Unit,
        onAutoVerified: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val auth = FirebaseAuth.getInstance()
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    scope.launch {
                        try {
                            auth.signInWithCredential(credential).await()
                            if (isSignedIn()) {
                                syncMetadataFromFirebaseUser()
                                onAutoVerified()
                            } else {
                                onError(Translations.tr("auth_err_generic", lang))
                            }
                        } catch (e: Exception) {
                            onError(resolveLocalizedAuthError(e, lang))
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.w("Auth", "Phone verification failed: ${e.message}", e)
                    onError(resolveLocalizedAuthError(e, lang))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId, token)
                }
            }
            val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(e164Phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
            if (resendToken != null) {
                optionsBuilder.setForceResendingToken(resendToken)
            }
            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        } catch (e: Exception) {
            onError(resolveLocalizedAuthError(e, lang))
        }
    }

    fun verifyPhoneOtpCode(
        verificationId: String,
        smsCode: String,
        lang: AppLanguage,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, smsCode.trim())
                FirebaseAuth.getInstance().signInWithCredential(credential).await()
                if (isSignedIn()) {
                    syncMetadataFromFirebaseUser()
                    onSuccess()
                } else {
                    onError(Translations.tr("auth_err_generic", lang))
                }
            } catch (e: Exception) {
                Log.w("Auth", "OTP code verification failed: ${e.message}", e)
                onError(resolveLocalizedAuthError(e, lang))
            }
        }
    }

    /**
     * Launches federated OAuth via Firebase Hosting handler for `"facebook.com"` or `"twitter.com"` (X).
     * Automatically checks any `pendingAuthResult` left over from a process-killed browser redirect.
     */
    fun signInWithOAuthProvider(
        activity: Activity,
        providerId: String,
        lang: AppLanguage,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val auth = FirebaseAuth.getInstance()
            val pendingTask = auth.pendingAuthResult
            if (pendingTask != null) {
                pendingTask
                    .addOnSuccessListener { result ->
                        syncMetadataFromFirebaseUser(result.user)
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(resolveLocalizedAuthError(e, lang))
                    }
                return
            }

            val builder = OAuthProvider.newBuilder(providerId)
            builder.addCustomParameter("lang", lang.code)
            if (providerId == "facebook.com") {
                builder.setScopes(listOf("email", "public_profile"))
            }

            auth.startActivityForSignInWithProvider(activity, builder.build())
                .addOnSuccessListener { result ->
                    syncMetadataFromFirebaseUser(result.user)
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w("Auth", "OAuth ($providerId) sign-in failed: ${e.message}", e)
                    onError(resolveLocalizedAuthError(e, lang))
                }
        } catch (e: Exception) {
            Log.w("Auth", "OAuth ($providerId) launch failed: ${e.message}", e)
            onError(resolveLocalizedAuthError(e, lang))
        }
    }

    fun consumePendingOAuthResult(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val pending = FirebaseAuth.getInstance().pendingAuthResult ?: return
            pending
                .addOnSuccessListener { result ->
                    syncMetadataFromFirebaseUser(result.user)
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
        } catch (_: Exception) {}
    }

    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        onSignOutComplete: () -> Unit,
        scope: CoroutineScope
    ) {
        FirebaseAuth.getInstance().signOut()
        lastAuthProvider = "local_guest"
        lastAuthIdentifier = ""
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed to clear credential state", e)
            } finally {
                onSignOutComplete()
            }
        }
    }
}
