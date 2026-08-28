package com.example.pelarikalcer.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "friends",
    primaryKeys = ["userId", "friendUserId"],
    indices = [Index("friendUserId")]
)
data class FriendEntity(
    val userId: Int,
    val friendUserId: Int
)
