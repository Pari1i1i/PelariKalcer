package com.example.pelarikalcer.ui.screens

import android.content.Context
import com.example.pelarikalcer.BuildConfig
import androidx.compose.animation.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pelarikalcer.data.local.AppDatabase
import com.example.pelarikalcer.ui.screens.aicoach.AiCoachScreen
import com.example.pelarikalcer.ui.screens.aicoach.AiCoachViewModel
import com.example.pelarikalcer.ui.screens.aicoach.AiCoachViewModelFactory
import com.example.pelarikalcer.ui.screens.challenges.ChallengesScreen
import com.example.pelarikalcer.ui.screens.dashboard.DashboardScreen
import com.example.pelarikalcer.ui.screens.dashboard.MainViewModel
import com.example.pelarikalcer.ui.screens.dashboard.MainViewModelFactory
import com.example.pelarikalcer.ui.screens.leaderboard.LeaderboardScreen
import com.example.pelarikalcer.ui.screens.profile.ProfileScreen
import com.example.pelarikalcer.ui.screens.run.ActiveRunScreen
import com.example.pelarikalcer.ui.screens.run.RunSummaryScreen
import com.example.pelarikalcer.ui.screens.run.RunViewModel
import com.example.pelarikalcer.ui.screens.run.RunViewModelFactory
import com.example.pelarikalcer.ui.screens.shop.FullPetScreen
import com.example.pelarikalcer.ui.theme.*
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.text.style.TextAlign
import com.example.pelarikalcer.ui.screens.run.RunScreen

private const val GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "Beranda", Icons.Filled.Home, Icons.Outlined.Home)
    object Run : BottomNavItem("run", "Lari", Icons.Filled.DirectionsRun, Icons.Outlined.DirectionsRun)
    object Pet : BottomNavItem("pet", "Pet", Icons.Filled.Pets, Icons.Outlined.Pets)
    object Challenges : BottomNavItem("challenges", "Tantangan", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
    object Leaderboard : BottomNavItem("leaderboard", "Papan", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard)
    object Profile : BottomNavItem("profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Run,
    BottomNavItem.Pet,
    BottomNavItem.Challenges,
    BottomNavItem.Leaderboard,
    BottomNavItem.Profile
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    userId: Int,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    var showAiCoach by remember { mutableStateOf(false) }
    var showRunSummary by remember { mutableStateOf(false) }

    // Physical Back Button Interception
    BackHandler(enabled = showAiCoach || pagerState.currentPage != 0) {
        when {
            showAiCoach -> showAiCoach = false
            pagerState.currentPage != 0 -> {
                scope.launch { pagerState.animateScrollToPage(0) }
            }
        }
    }

    // ViewModels
    val mainVm: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            userDao = db.userDao(),
            runDao = db.runDao(),
            challengeDao = db.challengeDao(),
            petDao = db.petDao(),
            userId = userId
        )
    )
    val mainState by mainVm.state.collectAsStateWithLifecycle()

    // Leaderboards (local Room)
    val localLeaderboard by db.userDao().getLeaderboard().collectAsStateWithLifecycle(emptyList())
    val friendsLeaderboard by db.userDao().getFriendsLeaderboard(userId).collectAsStateWithLifecycle(emptyList())
    val suggestedFriends by db.userDao().getSuggestedFriends(userId).collectAsStateWithLifecycle(emptyList())
    val friendUserIdsList by db.userDao().getFriendUserIds(userId).collectAsStateWithLifecycle(emptyList())
    val friendUserIds = remember(friendUserIdsList) { friendUserIdsList.toSet() }

    // Cloud leaderboard & Cloud Friends from Supabase
    var cloudLeaderboard by remember { mutableStateOf<List<com.example.pelarikalcer.data.local.entity.UserEntity>>(emptyList()) }
    var cloudAcceptedFriends by remember { mutableStateOf<List<com.example.pelarikalcer.data.local.entity.UserEntity>>(emptyList()) }
    var pendingRequests by remember { mutableStateOf<List<com.example.pelarikalcer.data.local.entity.UserEntity>>(emptyList()) }
    var sentRequestEmails by remember { mutableStateOf<Set<String>>(emptySet()) }
    val myEmail = mainState.user?.email ?: ""

    // Live background polling
    LaunchedEffect(myEmail) {
        if (myEmail.isNotBlank()) {
            while (true) {
                try {
                    val cloud = com.example.pelarikalcer.data.remote.SupabaseClient.fetchGlobalLeaderboard()
                    if (cloud.isNotEmpty()) cloudLeaderboard = cloud
                    pendingRequests = com.example.pelarikalcer.data.remote.SupabaseClient.fetchPendingRequests(myEmail)
                    sentRequestEmails = com.example.pelarikalcer.data.remote.SupabaseClient.fetchSentRequestEmails(myEmail)
                    cloudAcceptedFriends = com.example.pelarikalcer.data.remote.SupabaseClient.fetchAcceptedFriends(myEmail)
                } catch (e: Exception) {
                    // Safe network error absorption
                }
                kotlinx.coroutines.delay(4000)
            }
        }
    }

    val globalLeaderboard = remember(localLeaderboard, cloudLeaderboard) {
        val cloudEmails = cloudLeaderboard.mapNotNull { it.email.takeIf { e -> e.isNotBlank() }?.lowercase() }.toSet()
        val cloudUsernames = cloudLeaderboard.map { it.username.lowercase() }.toSet()
        val localOnly = localLeaderboard.filter { localUser ->
            val emailMatch = localUser.email.isNotBlank() && localUser.email.lowercase() in cloudEmails
            val usernameMatch = localUser.username.lowercase() in cloudUsernames
            !emailMatch && !usernameMatch
        }
        (cloudLeaderboard + localOnly).sortedByDescending { it.totalPoints }
    }

    val mergedFriendsLeaderboard = remember(friendsLeaderboard, cloudAcceptedFriends, mainState.user) {
        val list = mutableListOf<com.example.pelarikalcer.data.local.entity.UserEntity>()
        val existingEmails = mutableSetOf<String>()
        val existingUserIds = mutableSetOf<Int>()

        mainState.user?.let { user ->
            list.add(user)
            existingUserIds.add(user.userId)
            if (user.email.isNotBlank()) existingEmails.add(user.email.lowercase())
        }

        friendsLeaderboard.forEach { friend ->
            val notInIds = friend.userId !in existingUserIds
            val notInEmails = friend.email.isBlank() || friend.email.lowercase() !in existingEmails
            if (notInIds && notInEmails) {
                list.add(friend)
                existingUserIds.add(friend.userId)
                if (friend.email.isNotBlank()) existingEmails.add(friend.email.lowercase())
            }
        }

        cloudAcceptedFriends.forEach { friend ->
            if (friend.email.isNotBlank() && friend.email.lowercase() !in existingEmails) {
                list.add(friend)
                existingEmails.add(friend.email.lowercase())
            }
        }
        list.sortedByDescending { it.totalPoints }
    }

    val runVm: RunViewModel = viewModel(
        factory = RunViewModelFactory(
            runDao = db.runDao(), userDao = db.userDao(),
            userId = userId, userWeightKg = mainState.user?.weightKg ?: 65.0,
            context = context
        )
    )

    LaunchedEffect(Unit) {
        val seedKey = "leaderboard_seeded_v2"
        val prefs = context.getSharedPreferences("pelarikalcer_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean(seedKey, false)) {
            val fakeUsers = listOf(
                com.example.pelarikalcer.data.local.entity.UserEntity(username = "BudiSantoso", email = "budi@fake.com", passwordHash = "x", totalPoints = 4200, currentStreak = 14, weightKg = 68.0),
                com.example.pelarikalcer.data.local.entity.UserEntity(username = "SitiRun", email = "siti@fake.com", passwordHash = "x", totalPoints = 3100, currentStreak = 7, weightKg = 55.0),
                com.example.pelarikalcer.data.local.entity.UserEntity(username = "KalcerRunner", email = "kalcer@fake.com", passwordHash = "x", totalPoints = 2600, currentStreak = 5, weightKg = 72.0),
                com.example.pelarikalcer.data.local.entity.UserEntity(username = "CoachAlex", email = "alex@fake.com", passwordHash = "x", totalPoints = 5800, currentStreak = 21, weightKg = 75.0),
                com.example.pelarikalcer.data.local.entity.UserEntity(username = "DianFit", email = "dian@fake.com", passwordHash = "x", totalPoints = 1900, currentStreak = 3, weightKg = 58.0)
            )
            fakeUsers.forEach { fake ->
                if (db.userDao().getUserByUsername(fake.username) == null) {
                    db.userDao().insertUser(fake)
                }
            }
            prefs.edit().putBoolean(seedKey, true).apply()
        }
    }
    val runState by runVm.state.collectAsStateWithLifecycle()

    val aiCoachVm: AiCoachViewModel = viewModel(
        factory = AiCoachViewModelFactory(db.aiCoachDao(), userId, GEMINI_API_KEY)
    )

    LaunchedEffect(mainState.user) {
        mainState.user?.let { user ->
            com.example.pelarikalcer.data.remote.SupabaseClient.upsertUser(user)
        }
    }

    LaunchedEffect(runState.isFinished) {
        if (runState.isFinished) {
            runState.savedRunId?.let {
                mainVm.processRunCompletion(runState.distanceKm, runState.paceMinPerKm)
            }
            showRunSummary = true
            mainState.user?.let { user ->
                com.example.pelarikalcer.data.remote.SupabaseClient.upsertUser(user)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        when {
            showRunSummary -> RunSummaryScreen(
                distanceKm = runState.distanceKm,
                durationSeconds = runState.durationSeconds,
                paceMinPerKm = runState.paceMinPerKm,
                caloriesBurned = runState.caloriesBurned,
                elevationGainM = runState.elevationGainM,
                elevationLossM = runState.elevationLossM,
                maxAltitudeM = runState.maxAltitudeM,
                onDone = {
                    showRunSummary = false
                    scope.launch { pagerState.animateScrollToPage(0) }
                }
            )

            pagerState.currentPage == 1 && runState.isRunning -> ActiveRunScreen(
                durationSeconds = runState.durationSeconds,
                distanceKm = runState.distanceKm,
                paceMinPerKm = runState.paceMinPerKm,
                calories = runState.caloriesBurned,
                isRunning = !runState.isPaused,
                elevationGainM = runState.elevationGainM,
                elevationLossM = runState.elevationLossM,
                currentAltitudeM = runState.currentAltitudeM,
                currentLatitude = runState.currentLatitude,
                currentLongitude = runState.currentLongitude,
                onPauseResume = runVm::pauseResume,
                onFinish = runVm::finishRun
            )

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        // Smooth Horizontal Pager for Gesture / Slide Swipe
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            beyondViewportPageCount = 1,
                            userScrollEnabled = !runState.isRunning
                        ) { page ->
                            when (page) {
                                0 -> DashboardScreen(
                                    user = mainState.user,
                                    recentRuns = mainState.recentRuns,
                                    totalDistanceKm = mainState.totalDistanceKm,
                                    totalCalories = mainState.totalCalories,
                                    onStartRun = {
                                        scope.launch { pagerState.animateScrollToPage(1) }
                                    },
                                    onOpenAiCoach = { showAiCoach = true }
                                )
                                1 -> PreRunScreen(
                                    isActiveTab = pagerState.currentPage == 1,
                                    onStart = { runVm.startRun() }
                                )
                                2 -> FullPetScreen(
                                    activePet = mainState.activePet,
                                    allPets = mainState.inventoryPets,
                                    userPoints = mainState.user?.totalPoints ?: 0,
                                    onRenamePet = mainVm::renamePet,
                                    onFeedPet = mainVm::feedPetExp,
                                    onSwapActivePet = mainVm::swapActivePet,
                                    onGachaRoll = { mainVm.performGacha() },
                                    onDirectBuy = { species -> mainVm.directBuyPet(species) }
                                )
                                3 -> ChallengesScreen(
                                    totalDistanceKm = mainState.totalDistanceKm
                                )
                                4 -> LeaderboardScreen(
                                    globalLeaderboard = globalLeaderboard,
                                    friendsLeaderboard = mergedFriendsLeaderboard,
                                    suggestedFriends = suggestedFriends,
                                    pendingRequests = pendingRequests,
                                    sentRequestEmails = sentRequestEmails,
                                    friendUserIds = friendUserIds,
                                    currentUserId = userId,
                                    currentUserEmail = myEmail,
                                    onSendRequest = { targetUser ->
                                        scope.launch {
                                            if (myEmail.isNotBlank() && targetUser.email.isNotBlank()) {
                                                com.example.pelarikalcer.data.remote.SupabaseClient.sendFriendRequest(myEmail, targetUser.email)
                                                sentRequestEmails = sentRequestEmails + targetUser.email
                                            }
                                        }
                                    },
                                    onAcceptRequest = { senderUser ->
                                        scope.launch {
                                            if (myEmail.isNotBlank() && senderUser.email.isNotBlank()) {
                                                com.example.pelarikalcer.data.remote.SupabaseClient.acceptFriendRequest(myEmail, senderUser.email)
                                                pendingRequests = pendingRequests.filter { it.email != senderUser.email }
                                                if (senderUser.userId > 0) {
                                                    db.userDao().insertFriend(com.example.pelarikalcer.data.local.entity.FriendEntity(userId, senderUser.userId))
                                                    db.userDao().insertFriend(com.example.pelarikalcer.data.local.entity.FriendEntity(senderUser.userId, userId))
                                                }
                                                cloudAcceptedFriends = com.example.pelarikalcer.data.remote.SupabaseClient.fetchAcceptedFriends(myEmail)
                                            }
                                        }
                                    },
                                    onRejectRequest = { senderUser ->
                                        scope.launch {
                                            if (myEmail.isNotBlank() && senderUser.email.isNotBlank()) {
                                                com.example.pelarikalcer.data.remote.SupabaseClient.removeFriend(myEmail, senderUser.email)
                                                pendingRequests = pendingRequests.filter { it.email != senderUser.email }
                                            }
                                        }
                                    },
                                    onRemoveFriend = { targetUser ->
                                        scope.launch {
                                            if (targetUser.userId > 0) {
                                                db.userDao().removeFriend(userId, targetUser.userId)
                                            }
                                            if (myEmail.isNotBlank() && targetUser.email.isNotBlank()) {
                                                com.example.pelarikalcer.data.remote.SupabaseClient.removeFriend(myEmail, targetUser.email)
                                                sentRequestEmails = sentRequestEmails - targetUser.email
                                                cloudAcceptedFriends = com.example.pelarikalcer.data.remote.SupabaseClient.fetchAcceptedFriends(myEmail)
                                            }
                                        }
                                    }
                                )
                                5 -> ProfileScreen(
                                    user = mainState.user,
                                    totalDistanceKm = mainState.totalDistanceKm,
                                    totalRuns = mainState.totalRuns,
                                    totalCalories = mainState.totalCalories,
                                    onLogout = onLogout,
                                    onUpdateProfile = mainVm::updateProfile
                                )
                            }
                        }
                    }

                    MainBottomNavBar(
                        selectedIndex = pagerState.currentPage,
                        onItemSelected = { index ->
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }

        // AI Coach overlay
        AnimatedVisibility(
            visible = showAiCoach,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            AiCoachScreen(viewModel = aiCoachVm, onBack = { showAiCoach = false })
        }
    }
}

@Composable
fun MainBottomNavBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(listOf(DarkSurface, CardSurface)),
                    shape = RoundedCornerShape(28.dp)
                )
                .clip(RoundedCornerShape(28.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val isRunButton = item is BottomNavItem.Run

                    if (isRunButton) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    Brush.linearGradient(listOf(NeonGreen, Color(0xFF00C896))),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { onItemSelected(index) }) {
                                Icon(
                                    imageVector = item.selectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp),
                                    tint = DeepNavy
                                )
                            }
                        }
                    } else {
                        NavBarItem(item = item, isSelected = isSelected, onClick = { onItemSelected(index) })
                    }
                }
            }
        }
    }
}

@Composable
fun NavBarItem(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .then(
                if (isSelected) Modifier.background(NeonGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                else Modifier
            )
            .size(42.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) NeonGreen else TextMuted
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = item.label,
                    color = NeonGreen,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PreRunScreen(
    isActiveTab: Boolean,
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // Only load and render hardware MapView when the Run tab is active or swiped to, with dark theme background
        if (isActiveTab) {
            RunScreen()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.15f),
                            DeepNavy.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
                .background(CardSurface.copy(alpha = 0.95f), RoundedCornerShape(28.dp))
                .border(BorderStroke(1.dp, NeonGreen.copy(alpha = 0.2f)), RoundedCornerShape(28.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.DirectionsRun,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Siap untuk berlari?",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Peta offline & GPS aktif secara otomatis",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = DeepNavy)
                    Text(
                        "MULAI LARI",
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepNavy,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
