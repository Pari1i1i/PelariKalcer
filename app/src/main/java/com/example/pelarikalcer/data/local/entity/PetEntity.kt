package com.example.pelarikalcer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true)
    val petId: Int = 0,
    val petName: String,
    val description: String,
    val costPoints: Int,
    val assetName: String // e.g. "pet_cat", "pet_dragon"
)

@Entity(
    tableName = "user_pets",
    primaryKeys = ["userId", "petId"],
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PetEntity::class, parentColumns = ["petId"], childColumns = ["petId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("petId")]
)
data class UserPetEntity(
    val userId: Int,
    val petId: Int,
    val isEquipped: Boolean = false,
    val purchasedAt: Long = System.currentTimeMillis()
)
