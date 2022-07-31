package ru.edinros.agitatoroffline.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.edinros.agitatoroffline.core.PrefDataKeyValueStore
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {
    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context) = PrefDataKeyValueStore(appContext)
}
