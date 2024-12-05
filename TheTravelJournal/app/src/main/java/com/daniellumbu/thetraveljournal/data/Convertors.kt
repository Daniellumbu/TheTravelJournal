package com.daniellumbu.thetraveljournal.data
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromImageUrlsList(imageUrls: List<String>?): String {
        return Gson().toJson(imageUrls)
    }

    @TypeConverter
    fun toImageUrlsList(imageUrlsString: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(imageUrlsString, listType) ?: emptyList()
    }
}
