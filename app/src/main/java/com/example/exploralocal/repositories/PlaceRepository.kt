package com.example.exploralocal.repositories

import com.example.exploralocal.db.Place
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {
    fun getPlacesByName(): Flow<List<Place>>
    fun getPlacesByRating(): Flow<List<Place>>
    suspend fun getPlaceById(id: Int): Place?
    suspend fun insertPlace(place: Place)
    suspend fun updatePlace(place: Place)
    suspend fun deletePlace(id: Int)
    fun searchPlaces(query: String): Flow<List<Place>>
}