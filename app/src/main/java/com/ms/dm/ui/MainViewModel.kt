package com.ms.dm.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ms.dm.data.DailyMood
import com.ms.dm.data.FirebaseRepository
import com.ms.dm.data.MoodLevel
import com.ms.dm.data.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = FirebaseRepository(application.applicationContext)

    private val _currentUserEmail = MutableStateFlow(repository.getCurrentUserEmail())
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(repository.isEmailVerified())
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    private val _isSessionActive = MutableStateFlow(repository.isSessionActive())
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _sessionDaysRemaining = MutableStateFlow(repository.getSessionDaysRemaining())
    val sessionDaysRemaining: StateFlow<Int> = _sessionDaysRemaining.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val userProfile: StateFlow<UserProfile> = _currentUserEmail
        .flatMapLatest { email ->
            repository.observeUserProfile(email)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile(email = _currentUserEmail.value, isEmailVerified = _isEmailVerified.value)
        )

    val dailyMoods: StateFlow<List<DailyMood>> = _currentUserEmail
        .flatMapLatest { email ->
            repository.observe7DayMoods(email)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun refreshAuthState() {
        _currentUserEmail.value = repository.getCurrentUserEmail()
        _isEmailVerified.value = repository.isEmailVerified()
        _isSessionActive.value = repository.isSessionActive()
        _sessionDaysRemaining.value = repository.getSessionDaysRemaining()
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            if (email.isBlank() || !email.contains("@")) {
                _userMessage.value = "Please enter a valid email address."
                return@launch
            }
            if (pass.length < 6) {
                _userMessage.value = "Password must be at least 6 characters."
                return@launch
            }
            repository.signInWithEmail(email, pass)
            refreshAuthState()
            _userMessage.value = "Signed in successfully as $email (Session valid for 7 days)"
        }
    }

    fun signUpWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            if (email.isBlank() || !email.contains("@")) {
                _userMessage.value = "Please enter a valid email address."
                return@launch
            }
            if (pass.length < 6) {
                _userMessage.value = "Password must be at least 6 characters."
                return@launch
            }
            repository.signUpWithEmail(email, pass)
            refreshAuthState()
            _userMessage.value = "Account created for $email! Verification email sent."
        }
    }

    fun signInWithGmail(gmailAddress: String) {
        viewModelScope.launch {
            if (gmailAddress.isBlank() || !gmailAddress.contains("@")) {
                _userMessage.value = "Please enter a valid Gmail address."
                return@launch
            }
            repository.signInWithGmail(gmailAddress)
            refreshAuthState()
            _userMessage.value = "Signed in via Gmail as $gmailAddress (Saved for 7 days)"
        }
    }

    fun signOut() {
        repository.signOut()
        refreshAuthState()
        _userMessage.value = "Signed out successfully."
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            repository.sendVerificationEmail()
            refreshAuthState()
            _userMessage.value = "Verification link sent to ${_currentUserEmail.value}"
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            val immutableProfile = profile.copy(email = _currentUserEmail.value)
            repository.saveUserProfile(immutableProfile)
            _userMessage.value = "Profile updated successfully!"
        }
    }

    fun recordMoodForToday(moodLevel: MoodLevel) {
        viewModelScope.launch {
            val result = repository.recordTodayMood(_currentUserEmail.value, moodLevel)
            result.onSuccess { msg ->
                _userMessage.value = msg
            }.onFailure { err ->
                _userMessage.value = err.message
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
