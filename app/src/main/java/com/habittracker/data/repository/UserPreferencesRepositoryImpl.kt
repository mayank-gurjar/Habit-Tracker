package com.habittracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.habittracker.domain.repository.UserPreferences
import com.habittracker.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private val FITNESS_LEVEL = stringPreferencesKey("fitness_level")
    private val DIETARY_PREF = stringPreferencesKey("dietary_pref")
    private val PRIMARY_GOAL = stringPreferencesKey("primary_goal")

    override val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                fitnessLevel = preferences[FITNESS_LEVEL] ?: "Beginner",
                dietaryPreference = preferences[DIETARY_PREF] ?: "Anything",
                primaryGoal = preferences[PRIMARY_GOAL] ?: "General Wellbeing"
            )
        }

    override suspend fun updatePreferences(
        fitnessLevel: String,
        dietaryPreference: String,
        primaryGoal: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[FITNESS_LEVEL] = fitnessLevel
            preferences[DIETARY_PREF] = dietaryPreference
            preferences[PRIMARY_GOAL] = primaryGoal
        }
    }
}
