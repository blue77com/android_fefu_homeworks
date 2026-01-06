package com.example.android_fefu_homeworks_splitmate.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_fefu_homeworks_splitmate.ui.screens.*
import com.example.android_fefu_homeworks_splitmate.viewmodel.SplitViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Input : Screen("input")
    object Result : Screen("result/{calcId}") {
        const val ARG_CALC_ID = "calcId"
        fun createRoute(calcId: String) = "result/$calcId"
    }
    object History : Screen("history")
}

@Composable
fun SplitMateApp() {
    val holder: SplitViewModel = viewModel()
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onStartClick = { navController.navigate(Screen.Input.route) },
                onHistoryClick = { navController.navigate(Screen.History.route) }
            )
        }

        composable(route = Screen.Input.route) {
            InputScreen(
                viewModel = holder,
                onCalculate = { id ->
                    holder.setResultFromHistory(id, fromHistory = false)
                    navController.navigate(Screen.Result.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument(Screen.Result.ARG_CALC_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val calcId = backStackEntry.arguments?.getString(Screen.Result.ARG_CALC_ID)
            val calculation = calcId?.let(holder::calculationById)

            if (calculation == null) {
                Text("Calculation not found")
            } else {
                ResultScreen(
                    calcId = calcId,
                    viewModel = holder,
                    navController = navController
                )
            }
        }

        composable(route = Screen.History.route) {
            HistoryScreen(
                viewModel = holder,
                onItemClick = { id ->
                    holder.setResultFromHistory(id, fromHistory = true)
                    navController.navigate(Screen.Result.createRoute(id))
                }
            )
        }
    }
}
