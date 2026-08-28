package com.example.pelarikalcer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.pelarikalcer.data.local.entity.PetEntity
import com.example.pelarikalcer.data.local.entity.PetStage
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity): Long

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Query("SELECT * FROM pets WHERE ownerId = :userId ORDER BY isActive DESC, dateObtained DESC")
    fun getPetsByOwner(userId: Int): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE ownerId = :userId ORDER BY isActive DESC, dateObtained DESC")
    suspend fun getPetsByOwnerSnapshot(userId: Int): List<PetEntity>

    @Query("SELECT * FROM pets WHERE ownerId = :userId AND isActive = 1 LIMIT 1")
    fun getActivePet(userId: Int): Flow<PetEntity?>

    @Query("SELECT * FROM pets WHERE ownerId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActivePetSnapshot(userId: Int): PetEntity?

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getPetById(petId: Long): PetEntity?

    @Query("UPDATE pets SET isActive = 0 WHERE ownerId = :userId")
    suspend fun deactivateAllPets(userId: Int)

    @Query("UPDATE pets SET isActive = 1 WHERE id = :petId AND ownerId = :userId")
    suspend fun activatePetById(userId: Int, petId: Long)

    @Query("DELETE FROM pets WHERE id = :petId")
    suspend fun deletePet(petId: Long)

    @Query("UPDATE pets SET customNickname = :newName WHERE id = :petId")
    suspend fun renamePet(petId: Long, newName: String)

    /**
     * Swap active pet atomically
     */
    @Transaction
    suspend fun setActivePet(userId: Int, petId: Long) {
        deactivateAllPets(userId)
        activatePetById(userId, petId)
    }

    /**
     * Check if active pet has finished hatch countdown, and hatch it!
     */
    @Transaction
    suspend fun checkAndHatchActivePet(userId: Int) {
        val active = getActivePetSnapshot(userId) ?: return
        if (!active.isHatched && active.isReadyToHatch()) {
            updatePet(
                active.copy(
                    isHatched = true,
                    stage = PetStage.BABY,
                    level = 1,
                    currentExp = 0
                )
            )
        }
    }

    /**
     * Level up / EXP addition
     */
    @Transaction
    suspend fun addExpToPet(petId: Long, expToAdd: Int) {
        val pet = getPetById(petId) ?: return
        if (!pet.isHatched) return

        var newExp = pet.currentExp + expToAdd
        var newLevel = pet.level
        val expNeeded = 100

        while (newExp >= expNeeded) {
            newExp -= expNeeded
            newLevel += 1
        }

        val newStage = if (newLevel >= 11) PetStage.ADULT else PetStage.BABY
        val speedInc = (expToAdd / 50).coerceAtLeast(1)
        val staminaInc = (expToAdd / 50).coerceAtLeast(1)

        updatePet(
            pet.copy(
                level = newLevel,
                currentExp = newExp,
                stage = newStage,
                speedStat = pet.speedStat + speedInc,
                staminaStat = pet.staminaStat + staminaInc
            )
        )
    }

    /**
     * Process running distance for active pet
     */
    @Transaction
    suspend fun processRunForActivePet(userId: Int, distanceKm: Double, avgPaceMinPerKm: Double) {
        val pet = getActivePetSnapshot(userId) ?: return
        if (pet.isHatched) {
            val expGain = (distanceKm * 25).toInt()
            if (expGain > 0) {
                addExpToPet(pet.id, expGain)
            }
        }
    }
}
