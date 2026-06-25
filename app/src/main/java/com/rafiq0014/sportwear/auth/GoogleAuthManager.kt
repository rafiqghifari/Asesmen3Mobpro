package com.rafiq0014.sportwear.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.rafiq0014.sportwear.BuildConfig
import com.rafiq0014.sportwear.data.model.User
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthManager {

    suspend fun signIn(context: Context): User? {
        val credentialManager = CredentialManager.create(context)
        
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    User(
                        id = googleIdTokenCredential.id,
                        email = googleIdTokenCredential.id, // Using id as email for now if email is not directly available, but usually it's in the payload. Actually, id is unique.
                        displayName = googleIdTokenCredential.displayName,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                    )
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("Auth", "Parsing exception", e)
                    null
                }
            } else {
                Log.w("Auth", "Unexpected type of credential")
                null
            }
        } catch (e: Exception) {
            Log.e("Auth", "GetCredentialException", e)
            null
        }
    }

    suspend fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        try {
            credentialManager.clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest()
            )
        } catch (e: Exception) {
            Log.e("Auth", "Clear credential state exception", e)
        }
    }
}
