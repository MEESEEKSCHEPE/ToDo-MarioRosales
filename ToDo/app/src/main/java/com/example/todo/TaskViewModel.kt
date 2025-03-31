package com.example.todo

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskViewModel : ViewModel() {
    private var nextId = 1
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    fun addTask(text: String, imageUri: Uri?) {
        if (text.isNotBlank()) {
            val newTask = Task(nextId++, text, imageUri)
            _tasks.value = _tasks.value + newTask
        }
    }

    fun updateTask(id: Int, newText: String, newImageUri: Uri?) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(text = newText, imageUri = newImageUri) else task
        }
    }

    fun removeTask(task: Task) {
        _tasks.value = _tasks.value - task
    }
}
