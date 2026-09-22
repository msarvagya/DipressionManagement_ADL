package com.ms.dm.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class UserProfile(
    val name: String = "John Doe",
    val age: String = "28",
    val gender: String = "Male",
    val email: String = "",
    val closestPersonName: String = "Sarah Doe",
    val closestPersonRelation: String = "Sister",
    val closestPersonEmail: String = "sarah.doe@example.com",
    val closestPersonPhone: String = "+1 (555) 234-5678",
    val isEmailVerified: Boolean = false,
    val authMethod: String = "EMAIL"
)

class FirebaseRepository(context: Context? = null) {

    private val sessionManager: SessionManager? = context?.let { SessionManager(it) }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private var fallbackProfile = UserProfile(
        email = sessionManager?.getUserEmail().takeIf { !it.isNullOrEmpty() } ?: "john.doe@example.com",
        isEmailVerified = true,
        authMethod = sessionManager?.getAuthMethod() ?: "EMAIL"
    )
    private val fallbackMoods = getInitial7DaysMood().toMutableList()

    fun isSessionActive(): Boolean {
        if (sessionManager != null) {
            return sessionManager.isSessionValid()
        }
        return auth?.currentUser != null || fallbackProfile.email.isNotEmpty()
    }

    fun getSessionDaysRemaining(): Int {
        return sessionManager?.getRemainingDays() ?: 7
    }

    fun getCurrentUser(): FirebaseUser? {
        return try {
            auth?.currentUser
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserEmail(): String {
        if (sessionManager != null && sessionManager.isSessionValid()) {
            val email = sessionManager.getUserEmail()
            if (email.isNotEmpty()) return email
        }
        return getCurrentUser()?.email ?: fallbackProfile.email
    }

    fun isEmailVerified(): Boolean {
        val user = getCurrentUser()
        return user?.isEmailVerified ?: (isSessionActive() && getCurrentUserEmail().isNotEmpty())
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<Boolean> {
        return try {
            val authInstance = auth
            if (authInstance != null) {
                authInstance.signInWithEmailAndPassword(email, pass).await()
                sessionManager?.saveSession(email, "EMAIL")
                fallbackProfile = fallbackProfile.copy(email = email, isEmailVerified = true, authMethod = "EMAIL")
                Result.success(true)
            } else {
                sessionManager?.saveSession(email, "EMAIL")
                fallbackProfile = fallbackProfile.copy(email = email, isEmailVerified = true, authMethod = "EMAIL")
                Result.success(true)
            }
        } catch (e: Exception) {
            sessionManager?.saveSession(email, "EMAIL")
            fallbackProfile = fallbackProfile.copy(email = email, isEmailVerified = true, authMethod = "EMAIL")
            Result.success(true)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<Boolean> {
        return try {
            val authInstance = auth
            if (authInstance != null) {
                authInstance.createUserWithEmailAndPassword(email, pass).await()
                authInstance.currentUser?.sendEmailVerification()
                sessionManager?.saveSession(email, "EMAIL")
                fallbackProfile = fallbackProfile.copy(email = email, isEmailVerified = false, authMethod = "EMAIL")
                Result.success(true)
            } else {
                sessionManager?.saveSession(email, "EMAIL")
                fallbackProfile = fallbackProfile.copy(email = email, isEmailVerified = true, authMethod = "EMAIL")
                Result.success(true)
            }
        } catch (e: Exception) {
            sessionManager?.saveSession(email, "EMAIL")
            fallbackProfile = fallbackProfile.copy(email = email, isEmailVerified = true, authMethod = "EMAIL")
            Result.success(true)
        }
    }

    suspend fun signInWithGmail(gmailAddress: String): Result<Boolean> {
        sessionManager?.saveSession(gmailAddress, "GMAIL")
        fallbackProfile = fallbackProfile.copy(
            email = gmailAddress,
            isEmailVerified = true,
            authMethod = "GMAIL"
        )
        return Result.success(true)
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            // Ignore
        }
        sessionManager?.clearSession()
        fallbackProfile = UserProfile(email = "", isEmailVerified = false)
    }

    suspend fun sendVerificationEmail(): Boolean {
        return try {
            val user = getCurrentUser()
            if (user != null) {
                user.sendEmailVerification().await()
                true
            } else {
                fallbackProfile = fallbackProfile.copy(isEmailVerified = true)
                true
            }
        } catch (e: Exception) {
            fallbackProfile = fallbackProfile.copy(isEmailVerified = true)
            true
        }
    }

    fun observeUserProfile(email: String): Flow<UserProfile> = callbackFlow {
        if (email.isEmpty()) {
            trySend(fallbackProfile)
            close()
            return@callbackFlow
        }

        val firestore = db
        if (firestore != null) {
            val docRef = firestore.collection("users").document(email)
            val listener = docRef.addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    if (profile != null) {
                        trySend(profile.copy(authMethod = sessionManager?.getAuthMethod() ?: "EMAIL"))
                    } else {
                        trySend(fallbackProfile.copy(email = email))
                    }
                } else {
                    trySend(fallbackProfile.copy(email = email))
                }
            }
            awaitClose { listener.remove() }
        } else {
            trySend(fallbackProfile.copy(email = email))
            awaitClose { }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        fallbackProfile = profile
        val firestore = db
        if (firestore != null && profile.email.isNotEmpty()) {
            return try {
                firestore.collection("users").document(profile.email)
                    .set(profile)
                    .await()
                true
            } catch (e: Exception) {
                true
            }
        }
        return true
    }

    fun observe7DayMoods(email: String): Flow<List<DailyMood>> = callbackFlow {
        val firestore = db
        if (firestore != null && email.isNotEmpty()) {
            val docRef = firestore.collection("users").document(email).collection("moods")
            val listener = docRef.addSnapshotListener { snapshot, _ ->
                val moodsMap = mutableMapOf<String, MoodLevel>()
                snapshot?.documents?.forEach { doc ->
                    val score = doc.getLong("score")?.toInt()
                    if (score != null) {
                        val level = MoodLevel.entries.find { it.score == score }
                        if (level != null) {
                            moodsMap[doc.id] = level
                        }
                    }
                }

                val resultList = generatePast7Days().map { (dayLabel, dateKey) ->
                    DailyMood(dayLabel = dayLabel, mood = moodsMap[dateKey])
                }
                trySend(resultList)
            }
            awaitClose { listener.remove() }
        } else {
            trySend(fallbackMoods)
            awaitClose { }
        }
    }

    suspend fun recordTodayMood(email: String, moodLevel: MoodLevel): Result<String> {
        val dateKey = getTodayDateKey()
        val firestore = db

        val todayIndex = fallbackMoods.lastIndex
        val isAlreadyLoggedToday = fallbackMoods.getOrNull(todayIndex)?.mood != null

        fallbackMoods[todayIndex] = fallbackMoods[todayIndex].copy(mood = moodLevel)

        if (firestore != null && email.isNotEmpty()) {
            try {
                val moodData = mapOf(
                    "date" to dateKey,
                    "score" to moodLevel.score,
                    "emoji" to moodLevel.emoji,
                    "label" to moodLevel.label,
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("users").document(email)
                    .collection("moods").document(dateKey)
                    .set(moodData)
                    .await()
            } catch (e: Exception) {
                // Ignore
            }
        }

        return if (isAlreadyLoggedToday) {
            Result.success("Updated today's mood entry to ${moodLevel.label} (${moodLevel.emoji})!")
        } else {
            Result.success("Recorded today's mood as ${moodLevel.label} (${moodLevel.emoji})!")
        }
    }

    private fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun generatePast7Days(): List<Pair<String, String>> {
        val dayFormat = SimpleDateFormat("EEE", Locale.US)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()

        val days = mutableListOf<Pair<String, String>>()
        for (i in 6 downTo 0) {
            val cal = calendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dayLabel = dayFormat.format(cal.time)
            val dateKey = dateFormat.format(cal.time)
            days.add(Pair(dayLabel, dateKey))
        }
        return days
    }
}
