package com.daniellumbu.thetraveljournal.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.daniellumbu.thetraveljournal.R
import com.daniellumbu.thetraveljournal.data.TodoDAO
import com.daniellumbu.thetraveljournal.data.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    val todoDAO: TodoDAO
) : ViewModel() {
    val todoList: Flow<List<TodoItem>> = todoDAO.getAllTodos()

    // Method to get the total price
    suspend fun getTotalPrice():Double {
        return todoDAO.getTotalPrice()
    }

    var totalPrice by mutableStateOf(0.0)
        private set

    init {
        calculateTotalPrice()
    }

    fun calculateTotalPrice() {
        viewModelScope.launch {
            // Calculate the total price in a background thread
            val total = withContext(Dispatchers.IO) {
                todoDAO.getTotalPrice()
            }
            // Update totalPrice on the main thread
            totalPrice = total
        }
    }

    fun getAllToDoList(): Flow<List<TodoItem>> {
        return todoDAO.getAllTodos()
    }

    fun addTodoList(todoItem: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDAO.insert(todoItem)
            calculateTotalPrice()
        }
    }

    fun removeTodoItem(todoItem: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDAO.delete(todoItem)
        }
    }

    fun editTodoItem(editedTodo: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDAO.update(editedTodo)
            calculateTotalPrice()
        }
    }

    fun changeTodoState(todoItem: TodoItem, value: Boolean) {
        val updatedTodo = todoItem.copy()
        updatedTodo.isDone = value
        viewModelScope.launch(Dispatchers.IO) {
            todoDAO.update(updatedTodo)
        }
    }

    fun clearAllTodos() {
        viewModelScope.launch(Dispatchers.IO) {
            todoDAO.deleteAllTodos()
        }
    }

    suspend fun getAllTodoNum(): Int {
        return todoDAO.getTodosNum()
    }

    suspend fun getImportantTodoNum(): Int {
        return todoDAO.getImportantTodosNum()
    }
}

