package com.example.android_fefu_homeworks

import android.net.Uri
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_fefu_homeworks.model.Note
import com.example.android_fefu_homeworks.ui.screen.HolidayDetailScreen
import com.example.android_fefu_homeworks.ui.screen.HolidayListScreen
import com.example.android_fefu_homeworks.ui.screen.NoteDetailScreen
import com.example.android_fefu_homeworks.ui.screen.SettingsScreen
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayViewModel

sealed class HolidayRoute(val route: String) {
    data object List : HolidayRoute("list")
    data object Settings : HolidayRoute("settings")
    data object Detail : HolidayRoute("detail/{holidayId}") {
        const val ARG_HOLIDAY_ID = "holidayId"
        fun createRoute(holidayId: String): String = "detail/${Uri.encode(holidayId)}"
    }
    data object NoteDetail : HolidayRoute("note_detail/{date}?noteId={noteId}") {
        const val ARG_DATE = "date"
        const val ARG_NOTE_ID = "noteId"
        fun createRoute(date: String, noteId: String? = null): String {
            val base = "note_detail/$date"
            return if (noteId != null) "$base?noteId=$noteId" else base
        }
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
                onToggleShowOnlyNotes = viewModel::onToggleShowOnlyNotes,
                onHolidayClick = { holidayId ->
                    navController.navigate(HolidayRoute.Detail.createRoute(holidayId))
                },
                onAddNoteClick = { date ->
                    navController.navigate(HolidayRoute.NoteDetail.createRoute(date.toString()))
                },
                onNoteClick = { noteId ->
                    val note = uiState.notes.values.flatten().find { it.id == noteId }
                    if (note != null) {
                        navController.navigate(HolidayRoute.NoteDetail.createRoute(note.date, noteId))
                    }
                },
                onToggleNoteFavourite = viewModel::onToggleNoteFavourite,
                onDeleteNote = viewModel::deleteNote,
                onRetry = viewModel::retry,
                onRefresh = viewModel::refresh,
                onSettingsClick = {
                    navController.navigate(HolidayRoute.Settings.route)
                }
            )
        }

        composable(HolidayRoute.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
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

            HolidayDetailScreen(
                holiday = holiday,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(
            route = HolidayRoute.NoteDetail.route,
            arguments = listOf(
                navArgument(HolidayRoute.NoteDetail.ARG_DATE) { type = NavType.StringType },
                navArgument(HolidayRoute.NoteDetail.ARG_NOTE_ID) { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString(HolidayRoute.NoteDetail.ARG_DATE) ?: ""
            val noteId = backStackEntry.arguments?.getString(HolidayRoute.NoteDetail.ARG_NOTE_ID)
            
            var note by remember { mutableStateOf<Note?>(null) }
            
            LaunchedEffect(noteId) {
                if (noteId != null) {
                    note = viewModel.getNoteById(noteId)
                }
            }

            NoteDetailScreen(
                note = note,
                initialDate = date,
                onSave = viewModel::saveNote,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
