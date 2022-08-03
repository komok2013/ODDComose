package ru.edinros.agitatoroffline.core.repositories

import ru.edinros.agitatoroffline.core.PreferencesDataStore
import ru.edinros.agitatoroffline.core.remote.RemoteApi
import javax.inject.Inject

class SessionRepository @Inject constructor(
    private val remoteApi: RemoteApi,
    private val preferencesDataStore: PreferencesDataStore
) {
    suspend fun updateSession(flag:Boolean) = preferencesDataStore.updateSession(isOpen = flag)
    fun watchSession() = preferencesDataStore.watchSession()

}