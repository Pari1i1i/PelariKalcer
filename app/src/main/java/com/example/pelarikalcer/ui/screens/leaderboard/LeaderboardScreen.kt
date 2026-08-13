package com.example.pelarikalcer.ui.screens.leaderboard

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import com.example.pelarikalcer.data.remote.SupabaseClient
import com.example.pelarikalcer.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@Composable
fun LeaderboardScreen(
    globalLeaderboard: List<UserEntity>,
    friendsLeaderboard: List<UserEntity>,
    suggestedFriends: List<UserEntity> = emptyList(),
    pendingRequests: List<UserEntity> = emptyList(),
    sentRequestEmails: Set<String> = emptySet(),
    friendUserIds: Set<Int> = emptySet(),
    currentUserId: Int,
    currentUserEmail: String = "",
    onSendRequest: (UserEntity) -> Unit = {},
    onAcceptRequest: (UserEntity) -> Unit = {},
    onRejectRequest: (UserEntity) -> Unit = {},
    onRemoveFriend: (UserEntity) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Global, 1: Teman
    val activeList = if (selectedTab == 0) globalLeaderboard else friendsLeaderboard

    var searchQuery by remember { mutableStateOf("") }
    var cloudSearchResults by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val myEmail = currentUserEmail.ifBlank {
        globalLeaderboard.firstOrNull { it.userId == currentUserId }?.email ?: ""
    }
    val friendEmails = remember(friendsLeaderboard) { friendsLeaderboard.map { it.email }.toSet() }

    // Debounced cloud search (waits 400ms after user stops typing)
    LaunchedEffect(searchQuery) {
        searchJob?.cancel()
        if (searchQuery.isBlank()) {
            cloudSearchResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(400)
        val cloud = SupabaseClient.searchUsers(searchQuery.trim(), myEmail)
        val localMatches = globalLeaderboard.filter {
            it.userId != currentUserId && it.email != myEmail &&
            (it.username.contains(searchQuery.trim(), ignoreCase = true) ||
             it.email.contains(searchQuery.trim(), ignoreCase = true))
        }
        val cloudEmails = cloud.map { it.email }.toSet()
        val merged = cloud + localMatches.filter { it.email !in cloudEmails }
        cloudSearchResults = merged
        isSearching = false
    }

    // Entire Screen in a single unified LazyColumn for 100% responsive scrolling on all device screen sizes
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // 1. Header
        item {
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
        }

        // 2. Tabs
        item {
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
                            if (pendingRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(StreakOrange, CircleShape)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${pendingRequests.size}",
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
        }

        // 3. Incoming Friend Requests Section (on Friends Tab)
        if (selectedTab == 1 && pendingRequests.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Permintaan Pertemanan (${pendingRequests.size})",
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        pendingRequests.forEach { sender ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(NeonGreen.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        (sender.username.firstOrNull()?.uppercaseChar() ?: '?').toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sender.username, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    Text("Ingin menjadi temanmu", color = TextMuted, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { onAcceptRequest(sender) },
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                                ) {
                                    Text("Terima", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = { onRejectRequest(sender) },
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                    border = BorderStroke(1.dp, DangerRed)
                                ) {
                                    Text("Tolak", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Search & Add Friend bar on Friends Tab
        if (selectedTab == 1) {
            item {
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

                        // Display Cloud Search Results if search is active
                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            if (isSearching) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = NeonGreen,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mencari...", color = TextMuted, fontSize = 12.sp)
                                }
                            } else if (cloudSearchResults.isEmpty()) {
                                Text(
                                    "Tidak ada pengguna dengan nama '$searchQuery'",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                Text(
                                    "Hasil Pencarian (${cloudSearchResults.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                cloudSearchResults.forEach { user ->
                                    val isFriend = friendUserIds.contains(user.userId) || (user.email.isNotBlank() && friendEmails.contains(user.email))
                                    val isPendingSent = sentRequestEmails.contains(user.email)
                                    UserAddCard(
                                        user = user,
                                        isFriend = isFriend,
                                        isPendingSent = isPendingSent,
                                        onSendRequest = { onSendRequest(user) },
                                        onRemoveFriend = { onRemoveFriend(user) }
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Suggested Friends Section (if search is empty and on Friends Tab)
        if (selectedTab == 1 && searchQuery.isBlank() && suggestedFriends.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(
                        "Rekomendasi Pelari Lain",
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    suggestedFriends.take(3).forEach { user ->
                        val isFriend = friendUserIds.contains(user.userId) || (user.email.isNotBlank() && friendEmails.contains(user.email))
                        val isPendingSent = sentRequestEmails.contains(user.email)
                        UserAddCard(
                            user = user,
                            isFriend = isFriend,
                            isPendingSent = isPendingSent,
                            onSendRequest = { onSendRequest(user) },
                            onRemoveFriend = { onRemoveFriend(user) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // 6. Top 3 Podium
        if (activeList.size >= 3 && searchQuery.isBlank()) {
            item {
                PodiumRow(
                    first = activeList[0],
                    second = activeList[1],
                    third = activeList[2],
                    currentUserId = currentUserId
                )
            }
        } else if (selectedTab == 1 && activeList.size <= 1 && searchQuery.isBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Belum ada teman berteman. Cari & kirim permintaan pertemanan di atas!",
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }

        item {
            HorizontalDivider(color = TextMuted.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        }

        // 7. Rest of Leaderboard list
        if (activeList.isNotEmpty() && searchQuery.isBlank()) {
            val restList = if (activeList.size > 3) activeList.drop(3) else activeList
            itemsIndexed(restList) { index, user ->
                val rank = if (activeList.size > 3) index + 4 else index + 1
                val isFriend = friendUserIds.contains(user.userId) || (user.email.isNotBlank() && friendEmails.contains(user.email))
                val isPendingSent = sentRequestEmails.contains(user.email)
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    LeaderboardRow(
                        rank = rank,
                        user = user,
                        isCurrentUser = user.userId == currentUserId || (myEmail.isNotBlank() && user.email == myEmail),
                        isFriend = isFriend,
                        isPendingSent = isPendingSent,
                        onSendRequest = { onSendRequest(user) },
                        onRemoveFriend = { onRemoveFriend(user) }
                    )
                }
            }
        }

        // 8. Bottom Navigation Spacing
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun UserAddCard(
    user: UserEntity,
    isFriend: Boolean,
    isPendingSent: Boolean = false,
    onSendRequest: () -> Unit = {},
    onRemoveFriend: () -> Unit = {}
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
            when {
                isFriend -> {
                    Button(
                        onClick = onRemoveFriend,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.2f), contentColor = DangerRed)
                    ) {
                        Text("Hapus", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                isPendingSent -> {
                    OutlinedButton(
                        onClick = onRemoveFriend,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StreakOrange),
                        border = BorderStroke(1.dp, StreakOrange)
                    ) {
                        Text("Menunggu...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                else -> {
                    Button(
                        onClick = onSendRequest,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = DeepNavy)
                    ) {
                        Text("+ Tambah", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
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
    isPendingSent: Boolean = false,
    onSendRequest: () -> Unit = {},
    onRemoveFriend: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) NeonGreen.copy(alpha = 0.1f) else CardSurface
        ),
        border = if (isCurrentUser) BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)) else null
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
            when {
                isCurrentUser -> {
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
                isFriend -> {
                    IconButton(
                        onClick = onRemoveFriend,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = "Hapus Teman",
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                isPendingSent -> {
                    Surface(
                        color = StreakOrange.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { onRemoveFriend() }
                    ) {
                        Text(
                            "Menunggu",
                            color = StreakOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                else -> {
                    IconButton(
                        onClick = onSendRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Kirim Permintaan",
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
