package com.example.pelarikalcer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "friends",
    primaryKeys = ["userId", "friendUserId"],
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["friendUserId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("friendUserId")]
)
data class FriendEntity(
    val userId: Int,
    val friendUserId: Int
)
