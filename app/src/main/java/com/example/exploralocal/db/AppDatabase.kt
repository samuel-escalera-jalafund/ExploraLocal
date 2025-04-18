package com.example.exploralocal.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.exploralocal.db.dao.PlaceDao

@Database(
    entities = [Place::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao

    companion object {
        fun getDatabase(context: Context) : AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase :: class.java,
                "exploralocal_db"
            ).build()
        }
    }
}