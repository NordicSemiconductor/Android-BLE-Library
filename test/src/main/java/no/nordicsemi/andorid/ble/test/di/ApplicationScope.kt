package no.nordicsemi.andorid.ble.test.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
class ApplicationScope {

    @Provides
    fun getCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }
}