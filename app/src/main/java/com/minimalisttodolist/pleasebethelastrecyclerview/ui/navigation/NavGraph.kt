package com.minimalisttodolist.pleasebethelastrecyclerview.ui.navigation

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.DataStoreViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.screens.TaskScreen
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.AppViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.NoRippleTheme

@Composable
fun NavGraph(
    startDestination: String = "task_list",
    taskViewModel: TaskViewModel,
    dataStoreViewModel: DataStoreViewModel,
    appViewModel: AppViewModel,
    maybeShowReview: () -> Unit
) {
    val navController = rememberNavController()
    val taskState by taskViewModel.state.collectAsState()
    val appState by appViewModel.state.collectAsState()
    NavHost(navController, startDestination) {
        composable("task_list") {

            // Disabling Ripple Effect on Button Press Globally
            CompositionLocalProvider (
                LocalRippleTheme provides NoRippleTheme
            ){
                TaskScreen(
                    taskState = taskState,
                    appState = appState,
                    onEvent = taskViewModel::onEvent,
                    onAppEvent = appViewModel::onEvent,
                    taskViewModel = taskViewModel,
                    dataStoreViewModel = dataStoreViewModel,
                    maybeShowReview = maybeShowReview
                )
            }
        }
    }
}