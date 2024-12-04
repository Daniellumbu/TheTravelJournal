package com.daniellumbu.thetraveljournal.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.daniellumbu.thetraveljournal.R
import java.io.Serializable


@Entity(tableName = "todotable")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title:String,
    @ColumnInfo(name = "price") var price:String,
    @ColumnInfo(name = "description") val description:String,
    @ColumnInfo(name = "createDate") val createDate:String,
    @ColumnInfo(name = "priority") var priority:TodoPriority,
    @ColumnInfo(name = "isDone") var isDone: Boolean
) : Serializable

enum class TodoPriority {
    FOOD, CLOTHES, TECH, BOOKS;

    fun getIcon(): Int {
        return when (this) {
            FOOD -> R.drawable.fastfood
            CLOTHES -> R.drawable.clothes
            TECH -> R.drawable.tech
            BOOKS -> R.drawable.book
        }
    }
}
