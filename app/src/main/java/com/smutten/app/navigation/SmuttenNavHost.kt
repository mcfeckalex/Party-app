package com.smutten.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smutten.app.ui.screens.GameScreen
import com.smutten.app.ui.screens.HomeScreen
import com.smutten.app.ui.screens.PlayerSetupScreen
import com.smutten.app.viewmodel.GameViewModel

private object Routes {
    const val HOME = "home"
    const val SETUP = "setup"
    const val GAME = "game"
}

@Composable
fun SmuttenNavHost(gameViewModel: GameViewModel) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNewGame = {
                    gameViewModel.resetToHome()
                    navController.navigate(Routes.SETUP)
                }
            )
        }
        composable(Routes.SETUP) {
            PlayerSetupScreen(
                viewModel = gameViewModel,
                onStartGame = {
                    gameViewModel.startGame()
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }
        composable(Routes.GAME) {
            GameScreen(
                viewModel = gameViewModel,
                onExitToHome = {
                    gameViewModel.resetToHome()
                    navController.popBackStack(Routes.HOME, inclusive = false)
                }
            )
        }
    }
}
