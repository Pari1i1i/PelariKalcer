package com.example.pelarikalcer.data.local

import androidx.room.TypeConverter
import com.example.pelarikalcer.data.local.entity.PetStage
import com.example.pelarikalcer.data.local.entity.Rarity

class Converters {
    @TypeConverter
    fun fromRarity(value: Rarity?): String? = value?.name

    @TypeConverter
    fun toRarity(value: String?): Rarity? = value?.let {
        try {
            Rarity.valueOf(it)
        } catch (e: Exception) {
            Rarity.COMMON
        }
    }

    @TypeConverter
    fun fromPetStage(value: PetStage?): String? = value?.name

    @TypeConverter
    fun toPetStage(value: String?): PetStage? = value?.let {
        try {
            PetStage.valueOf(it)
        } catch (e: Exception) {
            PetStage.EGG
        }
    }
}
