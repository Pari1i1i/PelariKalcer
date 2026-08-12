package com.example.pelarikalcer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "runs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class RunEntity(
    @PrimaryKey(autoGenerate = true)
    val runId: Int = 0,
    val userId: Int,
    val distanceKm: Double,
    val durationSeconds: Int,
    val avgPaceMinutesPerKm: Double,
    val caloriesBurned: Int,
    val routeGeometry: String?,          // GeoJSON or JSON string of coordinates
    val startTime: Long,                 // Unix timestamp
    val endTime: Long,                   // Unix timestamp
    val elevationGainM: Double = 0.0,   // Total elevation gain in meters
    val elevationLossM: Double = 0.0,   // Total elevation loss in meters
    val maxAltitudeM: Double = 0.0,     // Highest point reached in meters
    val createdAt: Long = System.currentTimeMillis()
)
