package com.example.exploralocal.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.exploralocal.db.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Insert
    suspend fun insertPlace(place: Place)
    @Update
    suspend fun updatePlace(place: Place)

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun deletePlace(id: Int)

    @Query("SELECT * FROM places ORDER BY name ASC")
    fun getPlacesByName(): Flow<List<Place>>

    @Query("SELECT * FROM places ORDER BY rating DESC")
    fun getPlacesByRating(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceById(id: Int): Place?

    @Query("SELECT * FROM places WHERE name LIKE '%' || :query || '%'")
    fun searchPlaces(query: String): Flow<List<Place>>
}