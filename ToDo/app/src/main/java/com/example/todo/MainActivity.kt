package com.example.todo

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Image

class MainActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoListScreen(taskViewModel)
        }
    }
}

@Composable
fun TodoListScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var taskText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    val context = LocalContext.current


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val defaultImages = listOf("default_image1", "default_image2", "default_image3")
    fun getRandomDrawableUri(): Uri {
        val randomImage = defaultImages.random()
        return Uri.parse("android.resource://${context.packageName}/drawable/$randomImage")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        OutlinedTextField(
            value = taskText,
            onValueChange = { taskText = it },
            label = { Text(if (editingTask == null) "New Task" else "Edit Task") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { imagePicker.launch("image/*") }) {
                Icon(Icons.Default.Image, contentDescription = "Select Image")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Image")
            }
            Button(
                onClick = {
                    if (taskText.isNotBlank()) {
                        if (editingTask == null) {
                            viewModel.addTask(taskText, selectedImageUri ?: getRandomDrawableUri())
                        } else {
                            editingTask?.let { task ->
                                viewModel.updateTask(task.id, taskText, selectedImageUri ?: getRandomDrawableUri())
                            }
                            editingTask = null
                        }
                        taskText = ""
                        selectedImageUri = null
                    }
                }
            ) {
                Text(if (editingTask == null) "Add Task" else "Save Changes")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                TaskItem(
                    task,
                    onEdit = {
                        taskText = task.text
                        selectedImageUri = task.imageUri
                        editingTask = task
                    },
                    onDelete = { viewModel.removeTask(task) }
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onEdit: () -> Unit, onDelete: () -> Unit) {
    var grayscale by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val defaultImages = listOf("default_image1", "default_image2", "default_image")
    val randomImage = defaultImages.random()
    val imageUri = task.imageUri ?: Uri.parse("android.resource://${context.packageName}/drawable/$randomImage")

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp).clickable { grayscale = !grayscale }
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Task Image",
                modifier = Modifier.size(50.dp).graphicsLayer(shape = CircleShape, clip = true),
                colorFilter = if (grayscale) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = task.text,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp
            )

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Task")
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = Color.Red)
            }
        }
    }
}
