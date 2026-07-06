package com.habittracker.domain.repository

import kotlinx.coroutines.flow.Flow

data class UserPreferences(
    val fitnessLevel: String,
    val dietaryPreference: String,
    val primaryGoal: String
)

interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun updatePreferences(fitnessLevel: String, dietaryPreference: String, primaryGoal: String)
}
