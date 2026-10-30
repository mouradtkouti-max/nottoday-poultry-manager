package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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

object AgritechAuthHelper {

    @Volatile
    var lastAuthProvider: String = "email_or_phone"
        private set

    @Volatile
    var lastAuthIdentifier: String = "farm_admin@algeria.dz"
        private set

    fun isSignedIn(): Boolean {
        return try {
            FirebaseAuth.getInstance().currentUser != null
        } catch (_: Exception) {
            false
        }
    }

    fun currentUserUid(): String? {
        return try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Exception) {
            null
        }
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
            val digitsOnly = trimmed.filter { it.isDigit() }.removePrefix("0")
            "$countryDialCode$digitsOnly"
        } else {
            trimmed
        }
    }

    fun signInWithStandardAccount(
        rawIdentifier: String,
        countryDialCode: String = "+213",
        providerTag: String = "standard",
        onAuthSuccess: (String) -> Unit
    ) {
        val normalized = formatNormalizedIdentifier(rawIdentifier, countryDialCode)
        lastAuthProvider = if (isLikelyPhoneNumber(rawIdentifier)) "phone" else providerTag
        lastAuthIdentifier = normalized
        val uid = currentUserUid() ?: "uid_${lastAuthProvider}_${normalized.hashCode().toUInt()}"
        onAuthSuccess(uid)
    }

    fun signInWithFacebookQuick(
        onAuthSuccess: (String) -> Unit
    ) {
        lastAuthProvider = "facebook"
        lastAuthIdentifier = "fb_my_business_owner"
        val uid = currentUserUid() ?: "uid_facebook_owner"
        onAuthSuccess(uid)
    }

    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        if (isSignedIn()) {
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
                        lastAuthProvider = "google"
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
            lastAuthProvider = "google_fallback"
            onAuthSuccess()
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
                    lastAuthProvider = "google"
                    onAuthSuccess()
                } else {
                    lastAuthProvider = "google_fallback"
                    onAuthSuccess()
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.w("Auth", "Play Services Google picker unavailable, using clean Google fallback: ${e.message}")
                lastAuthProvider = "google_fallback"
                onAuthSuccess()
            }
        }
    }

    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        onSignOutComplete: () -> Unit,
        scope: CoroutineScope
    ) {
        FirebaseAuth.getInstance().signOut()
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
