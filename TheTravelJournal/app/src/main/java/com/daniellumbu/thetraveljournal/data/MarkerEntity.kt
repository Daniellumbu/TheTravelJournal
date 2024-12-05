package com.daniellumbu.thetraveljournal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val snippet: String,
    val imageUrls: List<String> = mutableListOf() // Use List<String> for multiple images
)