package com.habittracker.presentation.settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.habittracker.data.worker.SyncWorker
import com.habittracker.domain.repository.UserPreferences
import com.habittracker.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences("Beginner", "Anything", "General Wellbeing")
        )

    fun updatePreferences(fitnessLevel: String, dietaryPreference: String, primaryGoal: String) {
        viewModelScope.launch {
            userPreferencesRepository.updatePreferences(fitnessLevel, dietaryPreference, primaryGoal)
        }
    }

    fun triggerManualSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
        workManager.enqueue(request)
        Toast.makeText(context, "Cloud Sync triggered in background...", Toast.LENGTH_SHORT).show()
    }

    fun initiateGoogleSignIn(context: Context) {
        // Without a real google-services.json and SHA-1 fingerprint registered
        // in your Firebase Console, the Google Sign-In API will throw an ApiException.
        // For a production app, you would use CredentialManager or GoogleSignInClient here.
        Toast.makeText(context, "Real Google Sign-In requires SHA-1 Fingerprint setup in Firebase Console.", Toast.LENGTH_LONG).show()
    }
}
