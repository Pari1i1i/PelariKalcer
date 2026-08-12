package com.example.pelarikalcer.ui.navigation

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pelarikalcer.data.local.AppDatabase
import com.example.pelarikalcer.data.repository.UserRepository
import com.example.pelarikalcer.ui.screens.MainScreen
import com.example.pelarikalcer.ui.screens.SplashScreen
import com.example.pelarikalcer.ui.screens.TutorialOverlay
import com.example.pelarikalcer.ui.screens.auth.AuthState
import com.example.pelarikalcer.ui.screens.auth.AuthViewModel
import com.example.pelarikalcer.ui.screens.auth.AuthViewModelFactory
import com.example.pelarikalcer.ui.screens.auth.LoginScreen
import com.example.pelarikalcer.ui.screens.auth.RegisterScreen
import com.example.pelarikalcer.ui.screens.onboarding.OnboardingScreen
import com.example.pelarikalcer.ui.screens.onboarding.isOnboardingDone
import com.example.pelarikalcer.ui.screens.setTutorialDone
import com.example.pelarikalcer.ui.screens.isTutorialDone
import com.example.pelarikalcer.ui.screens.profile.SetupProfileScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(userRepository)
    )
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val prefs = remember { context.getSharedPreferences("pelarikalcer_prefs", Context.MODE_PRIVATE) }
    var loggedInUserId by remember {
        mutableStateOf<Int?>(
            if (prefs.contains("logged_in_user_id")) prefs.getInt("logged_in_user_id", -1).takeIf { it != -1 } else null
        )
    }
    var showTutorial by remember { mutableStateOf(!isTutorialDone(context)) }

    LaunchedEffect(authState) {
        when (val s = authState) {
            is AuthState.Success -> {
                loggedInUserId = s.userId
                prefs.edit().putInt("logged_in_user_id", s.userId).apply()
                val user = database.userDao().getUserByIdSnapshot(s.userId)
                if (user != null && user.weightKg == 0.0) {
                    navController.navigate(Screen.SetupProfile.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    showTutorial = !isTutorialDone(context)
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
                authViewModel.resetState()
            }
            else -> Unit
        }
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(onSplashDone = {
                val dest = when {
                    loggedInUserId != null -> Screen.Main.route
                    !isOnboardingDone(context) -> Screen.Onboarding.route
                    else -> Screen.Login.route
                }
                navController.navigate(dest) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { /* handled by LaunchedEffect */ },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { /* handled by LaunchedEffect */ },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SetupProfile.route) {
            val uid = loggedInUserId
            val scope = rememberCoroutineScope()
            if (uid != null) {
                SetupProfileScreen(
                    onSaveSetup = { w, h ->
                        scope.launch {
                            val user = database.userDao().getUserByIdSnapshot(uid)
                            if (user != null) {
                                database.userDao().updateUser(
                                    user.copy(weightKg = w, heightCm = h)
                                )
                                showTutorial = !isTutorialDone(context)
                                navController.navigate(Screen.Main.route) {
                                    popUpTo(Screen.SetupProfile.route) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }
        }

        composable(Screen.Main.route) {
            val uid = loggedInUserId
            if (uid != null) {
                MainScreen(
                    userId = uid,
                    showTutorial = showTutorial,
                    onTutorialDone = {
                        setTutorialDone(context)
                        showTutorial = false
                    },
                    onLogout = {
                        prefs.edit().remove("logged_in_user_id").apply()
                        loggedInUserId = null
                        showTutorial = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
