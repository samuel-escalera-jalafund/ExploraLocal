package com.example.exploralocal.repositories

import com.example.exploralocal.db.Place
import com.example.exploralocal.db.dao.PlaceDao
import jakarta.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val placeDao: PlaceDao
) : PlaceRepository {
    override fun getPlacesByName() = placeDao.getPlacesByName()
    override fun getPlacesByRating() = placeDao.getPlacesByRating()
    override suspend fun getPlaceById(id: Int) = placeDao.getPlaceById(id)
    override suspend fun insertPlace(place: Place) = placeDao.insertPlace(place)
    override suspend fun updatePlace(place: Place) = placeDao.updatePlace(place)
    override suspend fun deletePlace(id: Int) = placeDao.deletePlace(id)
    override fun searchPlaces(query: String) = placeDao.searchPlaces(query)
}