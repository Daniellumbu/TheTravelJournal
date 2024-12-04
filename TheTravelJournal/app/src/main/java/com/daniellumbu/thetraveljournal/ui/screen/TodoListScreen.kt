package com.daniellumbu.thetraveljournal.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.daniellumbu.thetraveljournal.data.TodoItem
import com.daniellumbu.thetraveljournal.data.TodoPriority
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.exp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = hiltViewModel(),
    onNavigateToSummary: (Int, Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val todoLists by viewModel.getAllToDoList().collectAsState(initial = emptyList())

    // Use derived state to calculate the total price from the todoList
    val totals: Double = todoLists.sumOf { it.price.toDoubleOrNull() ?: 0.0 }

    val todoList by viewModel.getAllToDoList().collectAsState(emptyList())

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var todoToEdit: TodoItem? by rememberSaveable {
        mutableStateOf(null)
    }
    var important by remember { mutableStateOf(false) }
    val pricesOfItems = remember { mutableStateListOf<Int>() }
    val total = pricesOfItems.sum()


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Shopping List") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = {
                            showAddDialog = true
                        }
                    ) {
                        Icon(
                            Icons.Filled.AddCircle, contentDescription = "Add"
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.clearAllTodos()
                        }
                    ) {
                        Icon(
                            Icons.Filled.Delete, contentDescription = "Delete all"
                        )
                    }

                }
            )
        }
    ) { innerpadding ->
        Column(modifier = modifier
            .fillMaxSize()
            .padding(innerpadding)) {
            if (todoList.isEmpty()) {
                Text(
                    "Empty list", modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            } else {
                Text("Total: $totals", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(todoList) { todoItem ->
                        TodoCard(
                            todoItem = todoItem,
                            important = important, // Pass the `important` state to TodoCard
                            onTodoDelete = { item ->
                                viewModel.removeTodoItem(item)
                            },
                            onTodoChecked = { item, checked ->
                                important = checked // Update `important` state when checkbox is checked
                                viewModel.changeTodoState(item, checked)
                            },
                            onTodoEdit = { item ->
                                todoToEdit = item
                                showAddDialog = true
                                important = item.isDone // Initialize `important` with the state of the item being edited
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TodoDialog(
            viewModel,
            todoToEdit = todoToEdit,
            onCancel = {
                showAddDialog = false
                todoToEdit = null
            },
            onImportantChange = { isImportant ->
                important = isImportant // Update `important` state when it changes
            },onPriceChange = { price ->
                price.toIntOrNull()?.let { numericPrice ->
                    pricesOfItems.add(numericPrice) // Add numeric price to the list
                }
            }
        )
    }

}

@Composable
fun TodoDialog(
    viewModel: TodoViewModel,
    todoToEdit: TodoItem? = null,
    onCancel: () -> Unit,
    onImportantChange: (Boolean) -> Unit, // New parameter
    onPriceChange: (String) -> Unit
) {
    var buttonCategory by remember { mutableStateOf("Category") }
    var oExpanded by remember { mutableStateOf(false) }
    var todoTitle by remember { mutableStateOf(todoToEdit?.title ?: "") }
    var todoprice by remember { mutableStateOf(todoToEdit?.price ?: "") }
    var priorityTodo by remember { mutableStateOf(TodoPriority.FOOD) }
    var todoDesc by remember { mutableStateOf(todoToEdit?.description ?: "") }
    var important by remember { mutableStateOf(todoToEdit?.isDone ?: false) }

    // Notify when `important` changes
    LaunchedEffect(important) {
        onImportantChange(important)
    }

    Dialog(onDismissRequest = { onCancel() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(size = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    if (todoToEdit == null) "New Item" else "Edit Item",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Item title") },
                    value = todoTitle,
                    onValueChange = { todoTitle = it }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Price") },
                    value = todoprice,
                    onValueChange = { newValue ->
                        // Allow only digits and at most one period in the input
                        if (newValue.all { it.isDigit() || it == '.' } && newValue.count { it == '.' } <= 1) {
                            todoprice = newValue
                            onPriceChange(newValue) // Notify parent composable of price change
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Todo description") },
                    value = todoDesc,
                    onValueChange = { todoDesc = it }
                )
                Box {
                    Button(onClick = { oExpanded = true }) {
                        Text(buttonCategory)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Arrow Down")
                    }
                    DropdownMenu(
                        expanded = oExpanded,
                        onDismissRequest = { oExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Food") },
                            onClick = { oExpanded = false; buttonCategory = "Food"; priorityTodo = TodoPriority.FOOD }
                        )
                        DropdownMenuItem(
                            text = { Text("Clothes") },
                            onClick = { oExpanded = false; buttonCategory = "Clothes"; priorityTodo = TodoPriority.CLOTHES }
                        )
                        DropdownMenuItem(
                            text = { Text("Tech") },
                            onClick = { oExpanded = false; buttonCategory = "Tech"; priorityTodo = TodoPriority.TECH }
                        )
                        DropdownMenuItem(
                            text = { Text("Books") },
                            onClick = { oExpanded = false; buttonCategory = "Books"; priorityTodo = TodoPriority.BOOKS }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = important,
                        onCheckedChange = { isChecked ->
                            important = isChecked
                        }
                    )
                    Text("Bought")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {

                        if (todoToEdit == null) {
                            val newTodo = TodoItem(
                                title = todoTitle,
                                price = todoprice,
                                description = todoDesc,
                                createDate = Date(System.currentTimeMillis()).toString(),
                                priority = priorityTodo,
                                isDone = important
                            )
                            viewModel.addTodoList(newTodo)
                        } else {
                            val editedTodo = todoToEdit.copy(
                                title = todoTitle,
                                price = todoprice,
                                description = todoDesc,
                                priority = priorityTodo,
                                isDone = important
                            )
                            viewModel.editTodoItem(editedTodo)
                        }

                        onCancel()
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}


@Composable
fun TodoCard(
    todoItem: TodoItem,
    important: Boolean, // New parameter
    onTodoDelete: (TodoItem) -> Unit,
    onTodoChecked: (TodoItem, checked: Boolean) -> Unit,
    onTodoEdit: (TodoItem) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        var expanded by remember { mutableStateOf(false) }
        var todoChecked by remember { mutableStateOf(important) } // Use `important` state

        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = todoItem.priority.getIcon()),
                    contentDescription = "Priority",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 10.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = todoItem.title,
                        textDecoration = if (todoChecked) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = todoItem.description,
                        textDecoration = if (todoChecked) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = todoItem.price,
                        textDecoration = if (todoChecked) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = todoChecked,
                        onCheckedChange = {
                            todoChecked = it
                            onTodoChecked(todoItem, todoChecked)

                        }
                    )
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.clickable { onTodoDelete(todoItem) },
                        tint = Color.Red
                    )
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.clickable { onTodoEdit(todoItem) },
                        tint = Color.Gray
                    )
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (expanded) "Less" else "More"
                        )
                    }
                }
            }

            if (expanded) {
                Text(
                    text = todoItem.createDate,
                    style = TextStyle(fontSize = 12.sp)
                )
            }
        }
    }
}
