package ru.edinros.agitatoroffline.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("AuthPref")

class PreferencesDataStore (private val context: Context) {
    private object PreferencesKeys {
        val IS_AUTHORIZED: Preferences.Key<Boolean> = booleanPreferencesKey("isAuthorized")
        val IS_SESSION_OPEN: Preferences.Key<Boolean> = booleanPreferencesKey("isSessionOpen")
    }
    suspend fun updateAuthorized(isAuthorized: Boolean) = PreferencesKeys.IS_AUTHORIZED.setValue(isAuthorized)
    suspend fun isAuthorized(): Boolean = PreferencesKeys.IS_AUTHORIZED.getValue(false)
    fun watchAuthorized() = PreferencesKeys.IS_AUTHORIZED.watchValue(false)

    suspend fun updateSession(isOpen: Boolean) = PreferencesKeys.IS_SESSION_OPEN.setValue(isOpen)
    suspend fun isSessionOpen(): Boolean = PreferencesKeys.IS_SESSION_OPEN.getValue(false)
    fun watchSession() = PreferencesKeys.IS_SESSION_OPEN.watchValue(false)



    /**
     *  Helper functions
     */
    private fun <T> Preferences.Key<T>.watchValue(defaultValue: T): Flow<T> {
        return context.dataStore.data
            .catchAndHandleError()
            .map { preferences -> preferences[this] ?: defaultValue }
    }

    private fun <T> Preferences.Key<T>.watchValue(): Flow<T?> {
        return context.dataStore.data
            .catchAndHandleError()
            .map { preferences -> preferences[this] }
    }

    private fun <T, M> Preferences.Key<T>.watchValue(convertToValueFunction: (T?) -> M?): Flow<M?> {
        return context.dataStore.data
            .catchAndHandleError()
            .map { preferences -> preferences[this] }
            .map(convertToValueFunction)
    }

    private suspend fun <T> Preferences.Key<T>.getValue(defaultValue: T): T {
        return context.dataStore.data
            .catchAndHandleError()
            .map { preferences -> preferences[this] }
            .firstOrNull() ?: defaultValue
    }

    private suspend fun <T> Preferences.Key<T>.getValue(): T? {
        return context.dataStore.data
            .catchAndHandleError()
            .map { preferences -> preferences[this] }
            .firstOrNull()
    }

    private suspend fun <T, M> Preferences.Key<T>.getValue(map: (T?) -> M?): M? {
        return context.dataStore.data
            .catchAndHandleError()
            .map { preferences -> map(preferences[this]) }
            .firstOrNull()
    }

    private suspend fun <T> Preferences.Key<T>.setValue(value: T) {
        context.dataStore.edit { preferences -> preferences[this] = value }
    }

    private fun Flow<Preferences>.catchAndHandleError(): Flow<Preferences> {
        this.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        return this@catchAndHandleError
    }
}
