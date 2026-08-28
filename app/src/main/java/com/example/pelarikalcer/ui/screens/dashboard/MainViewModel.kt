package com.example.pelarikalcer.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pelarikalcer.data.local.dao.ChallengeDao
import com.example.pelarikalcer.data.local.dao.PetDao
import com.example.pelarikalcer.data.local.dao.RunDao
import com.example.pelarikalcer.data.local.dao.UserDao
import com.example.pelarikalcer.data.local.entity.*
import com.example.pelarikalcer.ui.screens.challenges.defaultChallenges
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val user: UserEntity? = null,
    val recentRuns: List<RunEntity> = emptyList(),
    val totalDistanceKm: Double = 0.0,
    val totalCalories: Int = 0,
    val totalRuns: Int = 0,
    val activePet: PetEntity? = null,
    val inventoryPets: List<PetEntity> = emptyList()
)

class MainViewModel(
    private val userDao: UserDao,
    private val runDao: RunDao,
    private val challengeDao: ChallengeDao,
    private val petDao: PetDao,
    private val userId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    init {
        observeData()
        ensureDefaultStarterPet()
        startHatchTicker()
    }

    /**
     * Periodic safe check for hatch readiness
     */
    private fun startHatchTicker() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(1000L)
                try {
                    petDao.checkAndHatchActivePet(userId)
                } catch (e: Exception) {
                    // Safe error absorption
                }
            }
        }
    }

    /**
     * Starter Pet: Common Bunny Hop egg (5s hatch)
     */
    private fun ensureDefaultStarterPet() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pets = petDao.getPetsByOwnerSnapshot(userId)
                if (pets.isEmpty()) {
                    val starterPet = PetEntity(
                        ownerId = userId,
                        speciesId = 1, // Bunny Hop
                        rarity = Rarity.COMMON,
                        stage = PetStage.EGG,
                        level = 1,
                        currentExp = 0,
                        hatchStartTime = System.currentTimeMillis(),
                        isHatched = false,
                        isActive = true
                    )
                    petDao.insertPet(starterPet)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeData() {
        val userFlow = userDao.getUserById(userId).distinctUntilChanged()
        val runsFlow = runDao.getRecentRuns(userId).distinctUntilChanged()
        val distFlow = runDao.getTotalDistanceKm(userId).map { it ?: 0.0 }.distinctUntilChanged()
        val calFlow = runDao.getTotalCalories(userId).map { it ?: 0 }.distinctUntilChanged()
        val countFlow = runDao.getTotalRunCount(userId).distinctUntilChanged()
        val activePetFlow = petDao.getActivePet(userId).distinctUntilChanged()
        val allPetsFlow = petDao.getPetsByOwner(userId).distinctUntilChanged()

        viewModelScope.launch(Dispatchers.IO) {
            combine(
                combine(userFlow, runsFlow, distFlow) { user, runs, dist ->
                    Triple(user, runs, dist)
                },
                combine(calFlow, countFlow) { cal, count ->
                    Pair(cal, count)
                },
                combine(activePetFlow, allPetsFlow) { activePet, allPets ->
                    Pair(activePet, allPets)
                }
            ) { triple, pairCalCount, pairPets ->
                DashboardUiState(
                    user = triple.first,
                    recentRuns = triple.second,
                    totalDistanceKm = triple.third,
                    totalCalories = pairCalCount.first,
                    totalRuns = pairCalCount.second,
                    activePet = pairPets.first,
                    inventoryPets = pairPets.second
                )
            }.catch { e ->
                e.printStackTrace()
            }.collect { newState ->
                _state.value = newState
                checkAndAwardChallenges(newState.totalDistanceKm)
            }
        }
    }

    private fun checkAndAwardChallenges(totalDistanceKm: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProfile(updated: UserEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userDao.updateUser(updated)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun processRunCompletion(distanceKm: Double, avgPaceMinPerKm: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                petDao.processRunForActivePet(userId, distanceKm, avgPaceMinPerKm)

                val user = userDao.getUserByIdSnapshot(userId) ?: return@launch
                if (distanceKm < 1.0) return@launch

                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val lastRun = user.lastRunDate ?: 0L
                val yesterdayStart = todayStart - 86_400_000L

                val newStreak = when {
                    lastRun >= todayStart -> user.currentStreak
                    lastRun >= yesterdayStart -> user.currentStreak + 1
                    else -> 1
                }

                val newHighest = maxOf(newStreak, user.highestStreak)
                userDao.updateStreak(userId, newStreak, newHighest, System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Gacha Roll */
    suspend fun performGacha(): PetEntity? {
        val user = userDao.getUserByIdSnapshot(userId) ?: return null
        if (user.totalPoints < GachaConfig.GACHA_COST_POINTS) return null

        userDao.addPoints(userId, -GachaConfig.GACHA_COST_POINTS)

        val species = GachaConfig.rollSpecies()
        val currentActive = petDao.getActivePetSnapshot(userId)
        val shouldBeActive = currentActive == null

        val newPet = PetEntity(
            ownerId = userId,
            speciesId = species.id,
            rarity = species.rarity,
            stage = PetStage.EGG,
            level = 1,
            currentExp = 0,
            hatchStartTime = System.currentTimeMillis(),
            isHatched = false,
            isActive = shouldBeActive,
            speedStat = species.speedBase,
            staminaStat = species.staminaBase
        )

        val id = petDao.insertPet(newPet)
        return newPet.copy(id = id)
    }

    /** Direct Buy Species */
    suspend fun directBuyPet(species: PetSpecies): PetEntity? {
        val user = userDao.getUserByIdSnapshot(userId) ?: return null
        val cost = species.rarity.directCostPoints
        if (user.totalPoints < cost) return null

        userDao.addPoints(userId, -cost)

        val currentActive = petDao.getActivePetSnapshot(userId)
        val shouldBeActive = currentActive == null

        val newPet = PetEntity(
            ownerId = userId,
            speciesId = species.id,
            rarity = species.rarity,
            stage = PetStage.EGG,
            level = 1,
            currentExp = 0,
            hatchStartTime = System.currentTimeMillis(),
            isHatched = false,
            isActive = shouldBeActive,
            speedStat = species.speedBase,
            staminaStat = species.staminaBase
        )

        val id = petDao.insertPet(newPet)
        return newPet.copy(id = id)
    }

    /** Swap active pet */
    fun swapActivePet(petId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            petDao.setActivePet(userId, petId)
        }
    }

    /** Rename Pet */
    fun renamePet(petId: Long, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            petDao.renamePet(petId, newName.trim())
        }
    }

    /** Add Points directly (e.g. inject / test points) */
    fun addPoints(points: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.addPoints(userId, points)
        }
    }

    /** Feed pet with Points (e.g. 25 pts = 25 EXP) */
    fun feedPetExp(petId: Long, points: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.getUserByIdSnapshot(userId) ?: return@launch
            if (user.totalPoints < points) return@launch
            userDao.addPoints(userId, -points)
            petDao.addExpToPet(petId, points)
        }
    }
}

class MainViewModelFactory(
    private val userDao: UserDao,
    private val runDao: RunDao,
    private val challengeDao: ChallengeDao,
    private val petDao: PetDao,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(userDao, runDao, challengeDao, petDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
