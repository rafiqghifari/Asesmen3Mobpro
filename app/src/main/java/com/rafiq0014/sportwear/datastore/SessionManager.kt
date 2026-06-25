package com.rafiq0014.sportwear.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rafiq0014.sportwear.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_PHOTO_KEY = stringPreferencesKey("user_photo")
    }

    val userFlow: Flow<User?> = context.dataStore.data.map { preferences ->
        val id = preferences[USER_ID_KEY]
        val email = preferences[USER_EMAIL_KEY]
        if (id != null && email != null) {
            User(
                id = id,
                email = email,
                displayName = preferences[USER_NAME_KEY],
                photoUrl = preferences[USER_PHOTO_KEY]
            )
        } else {
            null
        }
    }

    suspend fun saveSession(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_EMAIL_KEY] = user.email
            if (user.displayName != null) preferences[USER_NAME_KEY] = user.displayName
            if (user.photoUrl != null) preferences[USER_PHOTO_KEY] = user.photoUrl
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
