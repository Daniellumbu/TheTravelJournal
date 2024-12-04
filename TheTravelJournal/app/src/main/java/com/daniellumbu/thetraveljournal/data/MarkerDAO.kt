package com.daniellumbu.thetraveljournal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

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

}
