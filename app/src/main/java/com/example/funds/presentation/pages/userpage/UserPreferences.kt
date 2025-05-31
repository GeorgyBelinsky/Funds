package com.example.funds.presentation.pages.userpage

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_prefs"
private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

object UserPrefsKeys {
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_IMAGE_URI = stringPreferencesKey("user_image_uri")
}

class UserPreferences(private val context: Context) {
    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[UserPrefsKeys.USER_NAME]
    }

    val userImageUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[UserPrefsKeys.USER_IMAGE_URI]
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[UserPrefsKeys.USER_NAME] = name
        }
    }

    suspend fun saveUserImageUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[UserPrefsKeys.USER_IMAGE_URI] = uri
        }
    }
}