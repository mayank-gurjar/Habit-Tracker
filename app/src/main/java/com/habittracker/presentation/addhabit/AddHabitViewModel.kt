package com.habittracker.presentation.addhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.domain.repository.AuthRepository
import com.habittracker.domain.usecase.habit.AddHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.habittracker.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val addHabitUseCase: AddHabitUseCase
) : ViewModel() {

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError

    fun clearAiError() {
        _aiError.value = null
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun saveHabit(
        name: String,
        emoji: String,
        colorHex: String,
        frequencyDays: List<Int>,
        reminderTime: String?,
        isTrackerEnabled: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val userId = user?.id ?: "offline_user_id"
            
            addHabitUseCase(
                userId = userId,
                name = name,
                emoji = emoji,
                colorHex = colorHex,
                frequencyDays = frequencyDays,
                reminderTime = reminderTime,
                isTrackerEnabled = isTrackerEnabled
            )
            onSuccess()
        }
    }

    fun generateHabitsFromGoal(goal: String, onSuccess: () -> Unit) {
        if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY.contains("your_copied")) {
            _aiError.value = "Error: Invalid or missing API Key in local.properties"
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val prompt = """
                    You are a habit coach. The user's goal is: "$goal".
                    Suggest exactly 3 habits to help them achieve this goal.
                    Return the result strictly as a JSON array of objects. Each object must have:
                    - "name": String (the habit name, short)
                    - "emoji": String (a single emoji representing it)
                    - "colorHex": String (a hex color code from this list: "#4F46E5", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899")
                    Do not wrap the JSON in markdown blocks, just output the raw JSON array.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val text = response.text ?: ""
                
                // Find the JSON array part
                val startIndex = text.indexOf("[")
                val endIndex = text.lastIndexOf("]")
                
                if (startIndex == -1 || endIndex == -1) {
                    throw Exception("Invalid response format from AI")
                }
                
                val jsonString = text.substring(startIndex, endIndex + 1)
                val jsonArray = JSONArray(jsonString)
                val user = authRepository.currentUser.first()
                val userId = user?.id ?: "offline_user_id"

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    addHabitUseCase(
                        userId = userId,
                        name = obj.optString("name", "New Habit"),
                        emoji = obj.optString("emoji", "🎯"),
                        colorHex = obj.optString("colorHex", "#4F46E5"),
                        frequencyDays = listOf(1, 2, 3, 4, 5, 6, 7),
                        reminderTime = null,
                        isTrackerEnabled = false
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                _aiError.value = e.message ?: "An unknown error occurred"
            } finally {
                _isGenerating.value = false
            }
        }
    }
}
