package ru.edinros.agitatoroffline.core.repositories

import ru.edinros.agitatoroffline.core.PreferencesDataStore
import ru.edinros.agitatoroffline.core.remote.RemoteApi
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val remoteApi: RemoteApi,
    private val preferencesDataStore: PreferencesDataStore
) {
    suspend fun updateAuthorized(flag: Boolean) =
        preferencesDataStore.updateAuthorized(isAuthorized = flag)

    fun watchAuthorized() = preferencesDataStore.watchAuthorized()

}