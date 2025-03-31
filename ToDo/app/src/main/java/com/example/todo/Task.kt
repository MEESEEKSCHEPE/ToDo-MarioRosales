package com.example.todo

import android.net.Uri

data class Task(
    val id: Int,
    val text: String,
    val imageUri: Uri?
)
