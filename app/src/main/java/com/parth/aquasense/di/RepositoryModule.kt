package com.parth.aquasense.di

import com.parth.aquasense.data.remote.api.TankApi
import com.parth.aquasense.data.repository.TankRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Module - Provides repository dependencies
 *
 * Why a separate module from NetworkModule?
 * - Separation of concerns: Network layer vs Data layer
 * - Easier to mock repositories in tests
 * - Can add other data sources later (Room database, preferences, etc.)
 *
 * @Module: Tells Hilt this class provides dependencies
 * @InstallIn(SingletonComponent::class): These dependencies live as long as the app
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides TankRepository
     *
     * @Provides: Marks this function as a dependency provider
     * @Singleton: Only one instance will be created
     *
     * Hilt will automatically:
     * 1. See that TankRepository needs TankApi
     * 2. Get TankApi from NetworkModule
     * 3. Create TankRepository with the TankApi instance
     * 4. Cache the TankRepository instance (because of @Singleton)
     */
    @Provides
    @Singleton
    fun provideTankRepository(
        tankApi: TankApi
    ): TankRepository {
        return TankRepository(tankApi)
    }
}
