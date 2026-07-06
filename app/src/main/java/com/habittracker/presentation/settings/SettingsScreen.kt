package com.habittracker.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userPrefs by viewModel.userPreferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("AI Coach Profile 🤖", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Fitness Level", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    PreferenceDropdown(
                        options = listOf("Beginner", "Intermediate", "Advanced"),
                        selectedOption = userPrefs.fitnessLevel,
                        onOptionSelected = { viewModel.updatePreferences(it, userPrefs.dietaryPreference, userPrefs.primaryGoal) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dietary Preference", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    PreferenceDropdown(
                        options = listOf("Anything", "Vegetarian", "Vegan", "Keto", "High Protein"),
                        selectedOption = userPrefs.dietaryPreference,
                        onOptionSelected = { viewModel.updatePreferences(userPrefs.fitnessLevel, it, userPrefs.primaryGoal) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Primary Goal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    PreferenceDropdown(
                        options = listOf("General Wellbeing", "Weight Loss", "Muscle Gain", "Productivity"),
                        selectedOption = userPrefs.primaryGoal,
                        onOptionSelected = { viewModel.updatePreferences(userPrefs.fitnessLevel, userPrefs.dietaryPreference, it) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Account", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Offline User", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.initiateGoogleSignIn(context) }) {
                        Text("Sign in with Google")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Data", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(onClick = { viewModel.triggerManualSync(context) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Manual Sync")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
