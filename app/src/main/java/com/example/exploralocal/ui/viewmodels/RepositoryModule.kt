package com.example.exploralocal.ui.viewmodels

import com.example.exploralocal.db.dao.PlaceDao
import com.example.exploralocal.repositories.PlaceRepository
import com.example.exploralocal.repositories.PlaceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun providePlaceRepository(placeDao: PlaceDao): PlaceRepository {
        return PlaceRepositoryImpl(placeDao)
    }
}