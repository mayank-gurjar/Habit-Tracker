package com.habittracker.presentation.addhabit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddHabitViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("📚") }
    var selectedColor by remember { mutableStateOf("#4F46E5") }
    var isTrackerEnabled by remember { mutableStateOf(false) }

    var showAiDialog by remember { mutableStateOf(false) }
    var aiGoalText by remember { mutableStateOf("") }
    val isGenerating by viewModel.isGenerating.collectAsState()
    val aiError by viewModel.aiError.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(aiError) {
        if (aiError != null) {
            Toast.makeText(context, aiError, Toast.LENGTH_LONG).show()
            viewModel.clearAiError()
        }
    }

    val colors = listOf("#4F46E5", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899")
    val emojis = listOf("📚", "🏃‍♂️", "💧", "🧘‍♀️", "🎸", "💻", "🎨", "🥗", "💪", "🛌")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showAiDialog = true }) {
                        Icon(Icons.Default.Star, contentDescription = "AI", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ask AI")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.saveHabit(
                            name = name,
                            emoji = selectedEmoji,
                            colorHex = selectedColor,
                            frequencyDays = listOf(1, 2, 3, 4, 5, 6, 7),
                            reminderTime = null,
                            isTrackerEnabled = isTrackerEnabled,
                            onSuccess = onNavigateBack
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Check, "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Color", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colors.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .clickable { selectedColor = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == hex) {
                            Icon(Icons.Default.Check, "Selected", tint = Color.White)
                        }
                    }
                }
            }

            Text("Emoji", style = MaterialTheme.typography.titleMedium)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedEmoji = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Enable Activity Tracker", style = MaterialTheme.typography.titleMedium)
                    Text("Count steps & distance for this habit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f))
                }
                Switch(
                    checked = isTrackerEnabled,
                    onCheckedChange = { isTrackerEnabled = it }
                )
            }
        }

        if (showAiDialog) {
            AlertDialog(
                onDismissRequest = { if (!isGenerating) showAiDialog = false },
                title = { Text("Smart Habit Assistant") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Describe your goal and I'll generate a 3-habit plan for you.")
                        OutlinedTextField(
                            value = aiGoalText,
                            onValueChange = { aiGoalText = it },
                            placeholder = { Text("e.g., I want to get fit") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isGenerating
                        )
                        if (isGenerating) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (aiGoalText.isNotBlank()) {
                                viewModel.generateHabitsFromGoal(aiGoalText, onNavigateBack)
                            }
                        },
                        enabled = aiGoalText.isNotBlank() && !isGenerating
                    ) {
                        Text("Generate Plan")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showAiDialog = false },
                        enabled = !isGenerating
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
