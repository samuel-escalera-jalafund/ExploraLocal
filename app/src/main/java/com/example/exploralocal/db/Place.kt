package com.example.exploralocal.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class Place(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val rating: Float,
    val latitude: Double,
    val longitude: Double,
    val photoPath: String?,
    val createdAt: Long = System.currentTimeMillis()
)