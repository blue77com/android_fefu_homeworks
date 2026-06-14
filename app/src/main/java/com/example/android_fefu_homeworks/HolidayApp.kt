package com.example.android_fefu_homeworks

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_fefu_homeworks.ui.screen.HolidayDetailScreen
import com.example.android_fefu_homeworks.ui.screen.HolidayListScreen
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayViewModel

sealed class HolidayRoute(val route: String) {
    data object List : HolidayRoute("list")
    data object Detail : HolidayRoute("detail/{holidayId}") {
        const val ARG_HOLIDAY_ID = "holidayId"
        fun createRoute(holidayId: String): String = "detail/${Uri.encode(holidayId)}"
    }
}

@Composable
fun HolidayApp() {
    val navController = rememberNavController()
    val viewModel: HolidayViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = HolidayRoute.List.route
    ) {
        composable(HolidayRoute.List.route) {
            HolidayListScreen(
                state = uiState,
                onCountryChange = viewModel::onCountryChange,
                onYearChange = viewModel::onYearChange,
                onMonthChange = viewModel::onMonthChange,
                onDateSelected = viewModel::onDateSelected,
                onGoToToday = viewModel::goToToday,
                onToggleFavourite = viewModel::onToggleFavourite,
                onHolidayClick = { holidayId ->
                    navController.navigate(HolidayRoute.Detail.createRoute(holidayId))
                },
                onAddNote = viewModel::addNote,
                onDeleteNote = viewModel::deleteNote,
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
            val holidayId = Uri.decode(
                backStackEntry.arguments?.getString(HolidayRoute.Detail.ARG_HOLIDAY_ID) ?: ""
            )
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
