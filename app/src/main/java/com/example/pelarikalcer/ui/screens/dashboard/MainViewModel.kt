package com.example.pelarikalcer.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pelarikalcer.data.local.dao.ChallengeDao
import com.example.pelarikalcer.data.local.dao.RunDao
import com.example.pelarikalcer.data.local.dao.UserDao
import com.example.pelarikalcer.data.local.entity.RunEntity
import com.example.pelarikalcer.data.local.entity.UserChallengeEntity
import com.example.pelarikalcer.data.local.entity.UserEntity
import com.example.pelarikalcer.ui.screens.challenges.defaultChallenges
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val user: UserEntity? = null,
    val recentRuns: List<RunEntity> = emptyList(),
    val totalDistanceKm: Double = 0.0,
    val totalCalories: Int = 0,
    val totalRuns: Int = 0
)

class MainViewModel(
    private val userDao: UserDao,
    private val runDao: RunDao,
    private val challengeDao: ChallengeDao,
    private val userId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                userDao.getUserById(userId),
                runDao.getRecentRuns(userId),
                runDao.getTotalDistanceKm(userId).map { it ?: 0.0 },
                runDao.getTotalCalories(userId).map { it ?: 0 },
                runDao.getTotalRunCount(userId)
            ) { user, runs, dist, cal, count ->
                DashboardUiState(user, runs, dist, cal, count)
            }.collect { newState ->
                _state.value = newState
                checkAndAwardChallenges(newState.totalDistanceKm)
            }
        }
    }

    /**
     * Dipanggil tiap kali totalDistanceKm berubah (habis run baru tersimpan).
     * Ngecek semua challenge di catalog: kalau target udah tercapai dan BELUM
     * pernah tercatat selesai di DB, baru simpan completion + tambah poin.
     * Aman dipanggil berkali-kali (idempotent) karena selalu cek dulu ke DB.
     */
    private fun checkAndAwardChallenges(totalDistanceKm: Double) {
        viewModelScope.launch {
            val alreadyCompleted = challengeDao.getCompletedChallengeIdsSnapshot(userId)
            defaultChallenges.forEach { challenge ->
                val reached = totalDistanceKm >= challenge.targetKm
                if (reached && challenge.id !in alreadyCompleted) {
                    challengeDao.upsertUserChallenge(
                        UserChallengeEntity(
                            userId = userId,
                            challengeId = challenge.id,
                            currentProgressKm = totalDistanceKm,
                            isCompleted = true,
                            completedAt = System.currentTimeMillis()
                        )
                    )
                    userDao.addPoints(userId, challenge.rewardPoints)
                }
            }
        }
    }

    fun updateProfile(updated: UserEntity) {
        viewModelScope.launch { userDao.updateUser(updated) }
    }

    /** Called after a run is saved to update streak */
    fun processStreakAfterRun(distanceKm: Double) {
        viewModelScope.launch {
            val user = userDao.getUserByIdSnapshot(userId) ?: return@launch
            if (distanceKm < 1.0) return@launch // Need at least 1km for streak

            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val lastRun = user.lastRunDate ?: 0L
            val yesterdayStart = todayStart - 86_400_000L

            val newStreak = when {
                lastRun >= todayStart -> user.currentStreak // Already ran today
                lastRun >= yesterdayStart -> user.currentStreak + 1 // Consecutive day
                else -> 1 // Streak broken, restart
            }

            val newHighest = maxOf(newStreak, user.highestStreak)
            userDao.updateStreak(userId, newStreak, newHighest, System.currentTimeMillis())
        }
    }

    fun logout() {
        // Simply handled via nav controller at screen level
    }
}

class MainViewModelFactory(
    private val userDao: UserDao,
    private val runDao: RunDao,
    private val challengeDao: ChallengeDao,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(userDao, runDao, challengeDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}