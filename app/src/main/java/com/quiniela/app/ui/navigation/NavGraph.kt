package com.quiniela.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quiniela.app.ui.screens.*
import com.quiniela.app.ui.theme.BackgroundDark
import com.quiniela.app.ui.viewmodels.QuinielaViewModel

sealed class Screen(val route: String) {
    object UserSelection : Screen("user_selection")
    object AdminPin : Screen("admin_pin")
    object Home : Screen("home")
    object Matches : Screen("matches")
    object Standings : Screen("standings")
    object Rules : Screen("rules")
    object Prediction : Screen("prediction/{matchId}") {
        fun createRoute(matchId: String) = "prediction/$matchId"
    }
    object Result : Screen("result/{matchId}") {
        fun createRoute(matchId: String) = "result/$matchId"
    }
    object ScoreDetail : Screen("score/{matchId}") {
        fun createRoute(matchId: String) = "score/$matchId"
    }
    // matchId opcional: null = crear nuevo, non-null = editar
    object CreateMatch : Screen("create_match?matchId={matchId}") {
        fun createRoute(matchId: String? = null) =
            if (matchId != null) "create_match?matchId=$matchId" else "create_match"
    }
}

@Composable
fun QuinielaNavGraph() {
    val vm: QuinielaViewModel = viewModel()
    val isReady by vm.isReady.collectAsState()
    val currentUser by vm.currentUser.collectAsState()

    if (!isReady) {
        Box(Modifier.fillMaxSize().background(BackgroundDark))
        return
    }

    val startDest = if (currentUser != null) Screen.Home.route else Screen.UserSelection.route
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDest) {

        composable(Screen.UserSelection.route) {
            UserSelectionScreen(
                vm = vm,
                onUserSelected = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.UserSelection.route) { inclusive = true }
                    }
                },
                onAdminSelected = { navController.navigate(Screen.AdminPin.route) }
            )
        }

        composable(Screen.AdminPin.route) {
            AdminPinScreen(
                vm = vm,
                onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.UserSelection.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                vm = vm,
                onNavigateMatches = { navController.navigate(Screen.Matches.route) },
                onNavigateStandings = { navController.navigate(Screen.Standings.route) },
                onNavigateRules = { navController.navigate(Screen.Rules.route) },
                onSwitchUser = {
                    vm.switchUser()
                    navController.navigate(Screen.UserSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Matches.route) {
            MatchesScreen(
                vm = vm,
                onPredictClick = { navController.navigate(Screen.Prediction.createRoute(it)) },
                onResultClick = { navController.navigate(Screen.Result.createRoute(it)) },
                onScoreClick = { navController.navigate(Screen.ScoreDetail.createRoute(it)) },
                onEditMatchClick = { navController.navigate(Screen.CreateMatch.createRoute(it)) },
                onCreateMatchClick = { navController.navigate(Screen.CreateMatch.createRoute()) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CreateMatch.route,
            arguments = listOf(navArgument("matchId") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { backStack ->
            CreateMatchScreen(
                matchId = backStack.arguments?.getString("matchId"),
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Prediction.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStack ->
            PredictionScreen(
                matchId = backStack.arguments!!.getString("matchId")!!,
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStack ->
            ResultScreen(
                matchId = backStack.arguments!!.getString("matchId")!!,
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ScoreDetail.route,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStack ->
            ScoreDetailScreen(
                matchId = backStack.arguments!!.getString("matchId")!!,
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Standings.route) {
            StandingsScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Screen.Rules.route) {
            RulesScreen(onBack = { navController.popBackStack() })
        }
    }
}
