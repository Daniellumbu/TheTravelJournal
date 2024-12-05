package com.daniellumbu.thetraveljournal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MarkerEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Add this annotation
abstract class MarkerDatabase : RoomDatabase() {

    abstract fun markerDao(): MarkerDAO

    companion object {
        @Volatile
        private var INSTANCE: MarkerDatabase? = null

        fun getDatabase(context: Context): MarkerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarkerDatabase::class.java,
                    "marker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
