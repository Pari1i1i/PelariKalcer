package com.example.pelarikalcer.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Pet Entity
 * - Independent standalone table
 * - Time-based hatch calculation using hatchStartTime timestamp
 * - Level 1-10: Baby, Level 11+: Adult
 */
@Entity(
    tableName = "pets",
    indices = [Index("ownerId"), Index("isActive")]
)
data class PetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Int = 0,
    val speciesId: Int = 1,
    val rarity: Rarity = Rarity.COMMON,
    val stage: PetStage = PetStage.EGG,
    val level: Int = 1,              // Level 1-10: Baby, Level 11+: Adult
    val currentExp: Int = 0,         // Current EXP towards next level
    val maxExp: Int = 100,           // Max EXP per level (100)
    val foodPoints: Int = 0,
    val hatchStartTime: Long = System.currentTimeMillis(), // Timestamp when egg incubation started
    val isHatched: Boolean = false,
    val isActive: Boolean = false,
    val dateObtained: Long = System.currentTimeMillis(),
    val speedStat: Int = 10,
    val staminaStat: Int = 10,
    val customNickname: String? = null
) {
    val species: PetSpecies
        get() = defaultSpeciesCatalog.find { it.id == speciesId } ?: defaultSpeciesCatalog.first()

    val displayName: String
        get() = customNickname?.takeIf { it.isNotBlank() } ?: species.name

    val totalHatchSeconds: Int
        get() = rarity.hatchSeconds

    fun getRemainingSeconds(currentTime: Long = System.currentTimeMillis()): Int {
        if (isHatched) return 0
        val elapsed = ((currentTime - hatchStartTime) / 1000L).toInt()
        return maxOf(0, totalHatchSeconds - elapsed)
    }

    fun getHatchProgress(currentTime: Long = System.currentTimeMillis()): Float {
        if (isHatched) return 1f
        val elapsed = ((currentTime - hatchStartTime) / 1000L).toFloat()
        return (elapsed / totalHatchSeconds.toFloat()).coerceIn(0f, 1f)
    }

    fun isReadyToHatch(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (isHatched) return true
        val elapsed = ((currentTime - hatchStartTime) / 1000L).toInt()
        return elapsed >= totalHatchSeconds
    }

    val expPercent: Float
        get() = if (maxExp > 0) (currentExp.toFloat() / maxExp).coerceIn(0f, 1f) else 0f

    val effectiveStage: PetStage
        get() = if (!isHatched) PetStage.EGG else if (level >= 11) PetStage.ADULT else PetStage.BABY
}
