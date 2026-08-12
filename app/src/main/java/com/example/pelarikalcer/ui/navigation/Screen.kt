package com.example.pelarikalcer.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Dashboard : Screen("dashboard")
    object Run : Screen("run")
    object ActiveRun : Screen("active_run")
    object RunSummary : Screen("run_summary/{runId}") {
        fun createRoute(runId: Long) = "run_summary/$runId"
    }
    object AiCoach : Screen("ai_coach")
    object Profile : Screen("profile")
    object Challenges : Screen("challenges")
    object PetShop : Screen("pet_shop")
    object SetupProfile : Screen("setup_profile")
}
