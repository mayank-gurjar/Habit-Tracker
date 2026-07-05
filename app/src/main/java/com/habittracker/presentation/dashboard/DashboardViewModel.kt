package com.habittracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.domain.model.Habit
import com.habittracker.domain.repository.AuthRepository
import com.habittracker.domain.repository.HabitRepository
import com.habittracker.domain.usecase.habit.ToggleHabitCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import com.google.ai.client.generativeai.GenerativeModel
import com.habittracker.BuildConfig

data class DashboardUiState(
    val isLoading: Boolean = true,
    val habitsWithLogs: List<Pair<Habit, Boolean>> = emptyList(),
    val todayDate: LocalDate = LocalDate.now(),
    val completionPercentage: Float = 0f,
    val userName: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
) : ViewModel() {

    private val _currentDate = MutableStateFlow(LocalDate.now())

    private val _aiCoachMessage = MutableStateFlow<String?>(null)
    val aiCoachMessage: StateFlow<String?> = _aiCoachMessage

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    private val generativeModel = GenerativeModel(
        modelName = "gemini-flash-latest",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    val uiState: StateFlow<DashboardUiState> = combine(
        authRepository.currentUser,
        _currentDate.flatMapLatest { date ->
            authRepository.currentUser.flatMapLatest { user ->
                val uid = user?.id ?: "offline_user_id"
                combine(
                    habitRepository.getHabits(uid),
                    habitRepository.getAllLogs(uid)
                ) { habits, allLogs ->
                    val dateString = date.toString()
                    habits.map { habit ->
                        val isCompleted = allLogs.any { it.habitId == habit.id && it.completedDate == dateString }
                        habit to isCompleted
                    }
                }
            }
        },
        _currentDate
    ) { user, habitsWithStatus, date ->
        val completedCount = habitsWithStatus.count { it.second }
        val totalCount = habitsWithStatus.size
        val percentage = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount

        DashboardUiState(
            isLoading = false,
            habitsWithLogs = habitsWithStatus,
            todayDate = date,
            completionPercentage = percentage,
            userName = user?.displayName ?: "Explorer"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun toggleHabit(habitId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val uid = user?.id ?: "offline_user_id"
            toggleHabitCompletionUseCase(habitId, uid, _currentDate.value)
        }
    }

    fun generateDailyCoachAdvice() {
        viewModelScope.launch {
            if (_isAiLoading.value) return@launch
            _isAiLoading.value = true
            _aiCoachMessage.value = null
            
            try {
                val user = authRepository.currentUser.first()
                val uid = user?.id ?: "offline_user_id"
                val habits = habitRepository.getHabits(uid).first()
                
                val habitContext = if (habits.isNotEmpty()) {
                    "The user is tracking these habits today: " + habits.joinToString(", ") { it.name }
                } else {
                    "The user hasn't set up any habits yet."
                }
                
                val prompt = """
                    You are an expert AI Life Coach, Personal Trainer, and Nutritionist.
                    $habitContext
                    
                    Give the user a highly detailed, actionable daily plan for today. 
                    Must include:
                    1. A specific, detailed core/ab workout routine (e.g. 3 sets of X).
                    2. A specific, healthy meal idea (e.g. breakfast or lunch) with ingredients.
                    3. A short motivational closing.
                    
                    Keep the response concise, punchy, and beautifully formatted using bullet points and emojis. Do not use markdown headers (like # or ##) because the UI font handles that, just use bold text and emojis.
                """.trimIndent()
                
                val response = generativeModel.generateContent(prompt)
                _aiCoachMessage.value = response.text
            } catch (e: Exception) {
                e.printStackTrace()
                _aiCoachMessage.value = "⚠️ Oops! Your AI Coach hit a snag. Please check your connection or API key and try again."
            } finally {
                _isAiLoading.value = false
            }
        }
    }
}
