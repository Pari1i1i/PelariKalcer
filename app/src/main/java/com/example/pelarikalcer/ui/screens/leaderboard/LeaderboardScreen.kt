package com.example.pelarikalcer.ui.screens.leaderboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pelarikalcer.data.local.entity.UserEntity
import com.example.pelarikalcer.ui.theme.*

@Composable
fun LeaderboardScreen(
    globalLeaderboard: List<UserEntity>,
    friendsLeaderboard: List<UserEntity>,
    suggestedFriends: List<UserEntity> = emptyList(),
    friendUserIds: Set<Int> = emptySet(),
    currentUserId: Int,
    onToggleFriend: (UserEntity) -> Unit = {},
    onAddFriendByUsername: (String, (String) -> Unit) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Global, 1: Teman
    val activeList = if (selectedTab == 0) globalLeaderboard else friendsLeaderboard

    var searchQuery by remember { mutableStateOf("") }

    // Live search filtered candidates across all non-current users
    val filteredSearchResults = remember(searchQuery, globalLeaderboard, currentUserId) {
        if (searchQuery.isBlank()) emptyList()
        else {
            globalLeaderboard.filter {
                it.userId != currentUserId &&
                (it.username.contains(searchQuery.trim(), ignoreCase = true) ||
                 it.email.contains(searchQuery.trim(), ignoreCase = true))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(CardSurface, DeepNavy)))
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "Leaderboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Peringkat & koneksi sesama pelari",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardSurface,
            contentColor = NeonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonGreen
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Global", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Teman", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (friendsLeaderboard.size > 1) { // includes self
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(NeonGreen, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "${friendsLeaderboard.size - 1}",
                                    color = DeepNavy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            )
        }

        // Search & Add Friend bar on Friends Tab
        if (selectedTab == 1) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Cari & Tambah Teman",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Ketik nama pelari...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonGreen
                        )
                    )

                    // Display Live Search Results if search is active
                    if (searchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        if (filteredSearchResults.isEmpty()) {
                            Text(
                                "Tidak ada pengguna dengan nama '$searchQuery'",
                                color = TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Text(
                                "Hasil Pencarian (${filteredSearchResults.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            filteredSearchResults.forEach { user ->
                                UserAddCard(
                                    user = user,
                                    isFriend = friendUserIds.contains(user.userId),
                                    onToggle = { onToggleFriend(user) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }

        // Suggested Friends Section (if search is empty and on Friends Tab)
        if (selectedTab == 1 && searchQuery.isBlank() && suggestedFriends.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(
                    "Rekomendasi Pelari Lain",
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                suggestedFriends.take(3).forEach { user ->
                    UserAddCard(
                        user = user,
                        isFriend = friendUserIds.contains(user.userId),
                        onToggle = { onToggleFriend(user) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        // Top 3 Podium
        if (activeList.size >= 3 && searchQuery.isBlank()) {
            PodiumRow(
                first = activeList[0],
                second = activeList[1],
                third = activeList[2],
                currentUserId = currentUserId
            )
        } else if (selectedTab == 1 && activeList.size <= 1 && searchQuery.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Belum ada teman berteman. Tambahkan dari rekomendasi di atas!",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

        HorizontalDivider(color = TextMuted.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 20.dp))

        // Rest of list
        if (activeList.isNotEmpty() && searchQuery.isBlank()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val restList = if (activeList.size > 3) activeList.drop(3) else activeList
                itemsIndexed(restList) { index, user ->
                    val rank = if (activeList.size > 3) index + 4 else index + 1
                    LeaderboardRow(
                        rank = rank,
                        user = user,
                        isCurrentUser = user.userId == currentUserId,
                        isFriend = friendUserIds.contains(user.userId),
                        onToggleFriend = { onToggleFriend(user) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun UserAddCard(
    user: UserEntity,
    isFriend: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(NeonGreen.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (user.username.firstOrNull()?.uppercaseChar() ?: '?').toString(),
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.username, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                Text("${user.totalPoints} pts • ${user.currentStreak} hari streak", color = TextMuted, fontSize = 11.sp)
            }
            Button(
                onClick = onToggle,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFriend) DangerRed.copy(alpha = 0.2f) else NeonGreen,
                    contentColor = if (isFriend) DangerRed else DeepNavy
                )
            ) {
                Text(
                    text = if (isFriend) "Hapus" else "+ Tambah",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PodiumRow(
    first: UserEntity,
    second: UserEntity,
    third: UserEntity,
    currentUserId: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd Place
        PodiumItem(rank = 2, user = second, isCurrentUser = second.userId == currentUserId,
            height = 80.dp, color = Color(0xFFC0C0C0))
        // 1st Place
        PodiumItem(rank = 1, user = first, isCurrentUser = first.userId == currentUserId,
            height = 110.dp, color = GoldStar)
        // 3rd Place
        PodiumItem(rank = 3, user = third, isCurrentUser = third.userId == currentUserId,
            height = 60.dp, color = Color(0xFFCD7F32))
    }
}

@Composable
fun PodiumItem(
    rank: Int,
    user: UserEntity,
    isCurrentUser: Boolean,
    height: androidx.compose.ui.unit.Dp,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        if (rank == 1) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldStar, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isCurrentUser) Brush.linearGradient(listOf(NeonGreen, Color(0xFF00C896)))
                    else Brush.linearGradient(listOf(color.copy(alpha = 0.8f), color.copy(alpha = 0.4f))),
                    CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                (user.username.firstOrNull()?.uppercaseChar() ?: '?').toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = DeepNavy
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            user.username,
            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Medium,
            color = if (isCurrentUser) NeonGreen else TextPrimary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${user.totalPoints} pts",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        // Podium base
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(height)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "#$rank",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = color
            )
        }
    }
}

@Composable
fun LeaderboardRow(
    rank: Int,
    user: UserEntity,
    isCurrentUser: Boolean,
    isFriend: Boolean = false,
    onToggleFriend: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) NeonGreen.copy(alpha = 0.1f) else CardSurface
        ),
        border = if (isCurrentUser) androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#$rank",
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.width(32.dp)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(TextMuted.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (user.username.firstOrNull()?.uppercaseChar() ?: '?').toString(),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.username,
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCurrentUser) NeonGreen else TextPrimary,
                    fontSize = 13.sp
                )
                if (user.currentStreak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, null, tint = StreakOrange, modifier = Modifier.size(12.dp))
                        Text(" ${user.currentStreak} hari streak", style = MaterialTheme.typography.labelSmall, color = StreakOrange)
                    }
                }
            }
            Text(
                "${user.totalPoints} pts",
                fontWeight = FontWeight.Bold,
                color = GoldStar,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (!isCurrentUser) {
                IconButton(
                    onClick = onToggleFriend,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFriend) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                        contentDescription = if (isFriend) "Hapus Teman" else "Tambah Teman",
                        tint = if (isFriend) DangerRed else NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Surface(
                    color = NeonGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Kamu",
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
