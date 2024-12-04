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
    val imageUrl: String // Store image as URL or file path
)
