package com.example.pelarikalcer.ui.screens

import android.content.Context
import com.example.pelarikalcer.BuildConfig
import androidx.compose.animation.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import com.example.pelarikalcer.ui.screens.shop.PetShopScreen
import com.example.pelarikalcer.ui.screens.shop.LottiePet
import com.example.pelarikalcer.ui.screens.shop.availablePets
import com.example.pelarikalcer.ui.theme.*
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import com.google.android.gms.location.LocationServices
import com.example.pelarikalcer.ui.screens.run.OsmMap
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
    object Challenges : BottomNavItem("challenges", "Tantangan", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents)
    object Leaderboard : BottomNavItem("leaderboard", "Papan", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard)
    object Profile : BottomNavItem("profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Run,
    BottomNavItem.Challenges,
    BottomNavItem.Leaderboard,
    BottomNavItem.Profile
)

@Composable
fun MainScreen(
    userId: Int,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var currentRoute by remember { mutableStateOf("home") }
    var showAiCoach by remember { mutableStateOf(false) }
    var showRunSummary by remember { mutableStateOf(false) }
    var showPetShop by remember { mutableStateOf(false) }

    // Persistent storage for Pets (offline fallback)
    val sharedPrefs = remember { context.getSharedPreferences("pelarikalcer_prefs", Context.MODE_PRIVATE) }
    var ownedPetIds by remember {
        mutableStateOf(
            sharedPrefs.getStringSet("owned_pet_ids_$userId", setOf("1"))?.mapNotNull { it.toIntOrNull() } ?: listOf(1)
        )
    }
    var equippedPetId by remember {
        mutableStateOf(
            if (sharedPrefs.contains("equipped_pet_id_$userId")) sharedPrefs.getInt("equipped_pet_id_$userId", 1) else 1
        )
    }

    val equippedPet = remember(equippedPetId) {
        availablePets.find { it.id == equippedPetId } ?: availablePets.first()
    }

    // Physical Back Button Interception
    BackHandler(enabled = showPetShop || showAiCoach || currentRoute != "home") {
        when {
            showPetShop -> showPetShop = false
            showAiCoach -> showAiCoach = false
            currentRoute != "home" -> currentRoute = "home"
        }
    }

    // ViewModels
    val mainVm: MainViewModel = viewModel(factory = MainViewModelFactory(db.userDao(), db.runDao(), db.challengeDao(), userId))
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

    // Live background polling (syncs every 3s automatically across devices without relog)
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
                    e.printStackTrace()
                }
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    // Merge local + cloud: deduplicate strictly by email and username so no double cards appear
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

    // Merge local friends + cloud accepted friends for Friends tab
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

    // Auto-seed fake competitors for offline leaderboard (only once, deduped by username)
    LaunchedEffect(Unit) {
        val seedKey = "leaderboard_seeded_v2" // bumped version to re-run clean seed
        val prefs = context.getSharedPreferences("pelarikalcer_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean(seedKey, false)) {
            // Delete old duplicates first
            val fakeEmails = listOf("budi@fake.com", "siti@fake.com", "kalcer@fake.com", "alex@fake.com", "dian@fake.com")
            val fakeUsers = listOf(
                com.example.pelarikalcer.data.local.entity.UserEntity(username = "BudiSantoso", email = "budi@fake.com", passwordHash = "x", totalPoints = 4200, currentStreak = 14, weightKg = 68.0),
                com.example.pelarikalcer.data.local.entity.UserEntity(username = "SitiRun", email = "siti@fake.com", passwordHash = "x", totalPoints = 3100, currentStreak = 7, weightKg = 55.0),
                com.example.pelarikalcer.data.local.entity.UserEntity(username = "KalcerRunner", email = "kalcer@fake.com", passwordHash = "x", totalPoints = 2600, currentStreak = 5, weightKg = 72.0),
                com.example.pelarikalcer.data.local.entity.UserEntity(username = "CoachAlex", email = "alex@fake.com", passwordHash = "x", totalPoints = 5800, currentStreak = 21, weightKg = 75.0),
                com.example.pelarikalcer.data.local.entity.UserEntity(username = "DianFit", email = "dian@fake.com", passwordHash = "x", totalPoints = 1900, currentStreak = 3, weightKg = 58.0)
            )
            fakeUsers.forEach { fake ->
                // Only insert if username doesn't exist yet
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

    // Auto-sync user to Supabase Cloud when logged in or profile updates
    LaunchedEffect(mainState.user) {
        mainState.user?.let { user ->
            com.example.pelarikalcer.data.remote.SupabaseClient.upsertUser(user)
        }
    }

    LaunchedEffect(runState.isFinished) {
        if (runState.isFinished) {
            runState.savedRunId?.let { mainVm.processStreakAfterRun(runState.distanceKm) }
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
                onDone = { showRunSummary = false; currentRoute = "home" }
            )

            currentRoute == "run" && runState.isRunning -> ActiveRunScreen(
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
                        when (currentRoute) {
                            "home" -> {
                                // Add cute animated pet preview to DashboardScreen
                                Box(modifier = Modifier.fillMaxSize()) {
                                    DashboardScreen(
                                        user = mainState.user,
                                        recentRuns = mainState.recentRuns,
                                        totalDistanceKm = mainState.totalDistanceKm,
                                        totalCalories = mainState.totalCalories,
                                        onStartRun = { currentRoute = "run" },
                                        onOpenAiCoach = { showAiCoach = true }
                                    )

                                    // Display equipped animated pet in dashboard header corner
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 80.dp, end = 20.dp)
                                            .size(56.dp)
                                    ) {
                                        LottiePet(url = equippedPet.lottieUrl, modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                            "run" -> PreRunScreen(onStart = { runVm.startRun() })
                            "challenges" -> ChallengesScreen(
                                totalDistanceKm = mainState.totalDistanceKm
                            )
                            "leaderboard" -> LeaderboardScreen(
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
                                            // Instant refresh of accepted friends
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
                            "profile" -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    ProfileScreen(
                                        user = mainState.user,
                                        totalDistanceKm = mainState.totalDistanceKm,
                                        totalRuns = mainState.totalRuns,
                                        totalCalories = mainState.totalCalories,
                                        onLogout = onLogout,
                                        onUpdateProfile = mainVm::updateProfile
                                    )

                                    // Display equipped pet anim next to avatar
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 130.dp, start = 80.dp)
                                            .size(52.dp)
                                    ) {
                                        LottiePet(url = equippedPet.lottieUrl, modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                    }

                    MainBottomNavBar(
                        selectedRoute = currentRoute,
                        onItemSelected = { route -> currentRoute = route }
                    )
                }
            }
        }

        // Pet Shop FAB on profile screen
        if (currentRoute == "profile" && !showRunSummary && !(currentRoute == "run" && runState.isRunning)) {
            FloatingActionButton(
                onClick = { showPetShop = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 160.dp, end = 20.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                containerColor = GoldStar,
                contentColor = DeepNavy
            ) {
                Icon(Icons.Default.Pets, contentDescription = "Toko Pet", modifier = Modifier.size(24.dp))
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

        // Pet Shop overlay
        AnimatedVisibility(
            visible = showPetShop,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showPetShop = false }) {
                            Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                        }
                        Text("Kembali ke Profil", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    PetShopScreen(
                        userPoints = mainState.user?.totalPoints ?: 0,
                        ownedPetIds = ownedPetIds,
                        equippedPetId = equippedPetId,
                        onBuyPet = { pet ->
                            if (mainState.user != null && mainState.user!!.totalPoints >= pet.costPoints) {
                                scope.launch {
                                    db.userDao().addPoints(userId, -pet.costPoints)
                                    val newOwned = ownedPetIds + pet.id
                                    ownedPetIds = newOwned
                                    sharedPrefs.edit().putStringSet("owned_pet_ids_$userId", newOwned.map { it.toString() }.toSet()).apply()
                                }
                            }
                        },
                        onEquipPet = { petId ->
                            equippedPetId = petId
                            sharedPrefs.edit().putInt("equipped_pet_id_$userId", petId).apply()
                        }
                    )
                }
            }
        }

    }
}

@Composable
fun MainBottomNavBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(listOf(DarkSurface, CardSurface)),
                    shape = RoundedCornerShape(32.dp)
                )
                .clip(RoundedCornerShape(32.dp))
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (item in bottomNavItems) {
                    val isSelected = selectedRoute == item.route
                    val isRunButton = item is BottomNavItem.Run

                    if (isRunButton) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    Brush.linearGradient(listOf(NeonGreen, Color(0xFF00C896))),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { onItemSelected(item.route) }) {
                                Icon(
                                    imageVector = item.selectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(26.dp),
                                    tint = DeepNavy
                                )
                            }
                        }
                    } else {
                        NavBarItem(item = item, isSelected = isSelected, onClick = { onItemSelected(item.route) })
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
            .then(if (isSelected) Modifier.background(NeonGreen.copy(alpha = 0.12f), RoundedCornerShape(14.dp)) else Modifier)
            .size(48.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) NeonGreen else TextMuted
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(item.label, color = NeonGreen, fontSize = 8.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
    }
}

@Composable
fun PreRunScreen(onStart: () -> Unit) {
    val context = LocalContext.current
    var centerPoint by remember { mutableStateOf(GeoPoint(-6.2088, 106.8456)) } // default Jakarta

    LaunchedEffect(Unit) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        try {
            client.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    centerPoint = GeoPoint(loc.latitude, loc.longitude)
                }
            }
        } catch (e: SecurityException) {
            // Location permission not granted/active yet
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // OSM Map background
//        OsmMap(
//            modifier = Modifier.fillMaxSize(),
//            centerPoint = centerPoint,
//            zoomLevel = 15.5
//        )

        RunScreen()

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.1f),
                            DeepNavy.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // PreRun Card Content on Top
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
            
            // Start button
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
