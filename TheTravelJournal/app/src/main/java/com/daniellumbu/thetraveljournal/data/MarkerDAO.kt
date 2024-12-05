package com.daniellumbu.thetraveljournal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dagger.Provides

@Dao
interface MarkerDAO {

    @Insert
    suspend fun insertMarker(marker: MarkerEntity)

    @Query("SELECT * FROM markers")
    suspend fun getAllMarkers(): List<MarkerEntity>

    @Query("SELECT * FROM markers WHERE id = :markerId")
    suspend fun getMarkerById(markerId: Int): MarkerEntity?

    @Query("DELETE FROM markers")
    suspend fun deleteAllMarkers()

    @Query("SELECT * FROM markers WHERE latitude = :latitude AND longitude = :longitude")
    fun getMarkersByPosition(latitude: Double, longitude: Double): List<MarkerEntity>

    @Query("UPDATE markers SET imageUrls = :imageUrls WHERE id = :markerId")
    suspend fun updateImageUrls(markerId: Int, imageUrls: List<String>)

    @Update
    suspend fun updateMarker(marker: MarkerEntity)

}

