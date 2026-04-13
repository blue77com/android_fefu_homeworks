package com.example.android_fefu_homeworks

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_fefu_homeworks.model.HolidayFilter
import com.example.android_fefu_homeworks.ui.screen.HolidayDetailScreen
import com.example.android_fefu_homeworks.ui.screen.HolidayListScreen
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayViewModel

sealed class HolidayRoute(val route: String) {
    data object List : HolidayRoute("list")
    data object Detail : HolidayRoute("detail/{holidayId}") {
        const val ARG_HOLIDAY_ID = "holidayId"
        fun createRoute(holidayId: String): String = "detail/$holidayId"
    }
}

@Composable
fun HolidayApp() {
    val navController = rememberNavController()
    val viewModel: HolidayViewModel = hiltViewModel()
    val uiState = viewModel.uiState

    NavHost(
        navController = navController,
        startDestination = HolidayRoute.List.route
    ) {
        composable(HolidayRoute.List.route) {
            HolidayListScreen(
                state = uiState,
                onQueryChange = viewModel::onQueryChange,
                onCountryChange = viewModel::onCountryChange,
                onYearChange = viewModel::onYearChange,
                onFilterChange = viewModel::onFilterChange,
                onToggleFavourite = viewModel::onToggleFavourite,
                onHolidayClick = { holidayId ->
                    navController.navigate(HolidayRoute.Detail.createRoute(holidayId))
                },
                onRetry = viewModel::retry,
                onRefresh = viewModel::refresh,
                onDismissFavouriteActionError = viewModel::dismissFavouriteActionError,
            )
        }

        composable(
            route = HolidayRoute.Detail.route,
            arguments = listOf(
                navArgument(HolidayRoute.Detail.ARG_HOLIDAY_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val holidayId = backStackEntry.arguments?.getString(HolidayRoute.Detail.ARG_HOLIDAY_ID) ?: ""
            val holiday = viewModel.getHolidayById(holidayId)
            val isFavourite = holidayId in uiState.favourites

            HolidayDetailScreen(
                holiday = holiday,
                isFavourite = isFavourite,
                onToggleFavourite = { viewModel.onToggleFavourite(holidayId) },
                onBackClick = { navController.popBackStack() },
                favouriteActionError = uiState.favouriteActionError,
                onDismissFavouriteActionError = viewModel::dismissFavouriteActionError,
            )
        }
    }
}
