package com.minimalisttodolist.pleasebethelastrecyclerview

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.minimalisttodolist.pleasebethelastrecyclerview.data.database.MIGRATION_1_2
import com.minimalisttodolist.pleasebethelastrecyclerview.data.preferences.AppPreferences
import com.minimalisttodolist.pleasebethelastrecyclerview.data.database.TaskDatabase
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ReviewStateType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ThemeType
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.navigation.NavGraph
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.AppViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.DataStoreViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.PreferencesViewModelFactory
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.MinimalistTodoListV4Theme
import com.minimalisttodolist.pleasebethelastrecyclerview.util.NotificationHelper
import com.minimalisttodolist.pleasebethelastrecyclerview.util.PermissionManager
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.AppViewModelFactory
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var permissionManager: PermissionManager
    private lateinit var postNotificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var appPreferences: AppPreferences

    private val appViewModel by viewModels<AppViewModel> {
        AppViewModelFactory(appPreferences)
    }

    private val dataStoreViewModel by viewModels<DataStoreViewModel> {
        PreferencesViewModelFactory(appPreferences)
    }

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "tasks.db"
        ).addMigrations(MIGRATION_1_2).build()
    }

    private val notificationHelper by lazy {
        NotificationHelper(applicationContext)
    }

    private val taskViewModel by viewModels<TaskViewModel> {
        TaskViewModelFactory(db.dao, notificationHelper, dataStoreViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        setupUI()
        initializeComponents()
        setContent {
            SetupTheme()
        }
    }

    private fun setupUI() {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
    }

    private fun initializeComponents() {
        appPreferences = AppPreferences.getInstance(this)
        initializeNotificationPermission()
        appViewModel.setPermissionManager(permissionManager)
    }

    private fun initializeNotificationPermission() {
        postNotificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            lifecycleScope.launch {
                permissionManager.handlePostNotificationPermissionResult(isGranted)
            }
        }
        permissionManager = PermissionManager(
            context = this,
            activity = this,
            postNotificationPermissionLauncher = postNotificationPermissionLauncher,
            onTaskEvent = appViewModel::onEvent,
            appPreferences = appPreferences
        )
    }

    @Composable
    private fun SetupTheme() {
        val theme by dataStoreViewModel.theme.collectAsState()
        val fontFamilyType by dataStoreViewModel.fontFamily.collectAsState()
        val fontSize by dataStoreViewModel.fontSize.collectAsState()
        val fontWeight by dataStoreViewModel.fontWeight.collectAsState()

        val darkTheme = when (theme) {
            ThemeType.DARK -> true
            ThemeType.LIGHT -> false
            ThemeType.AUTO -> isSystemInDarkTheme()
        }

        MinimalistTodoListV4Theme(
            darkTheme = darkTheme,
            fontFamilyType = fontFamilyType,
            baseFontSize = fontSize,
            fontWeight = fontWeight
        ) {
            NavGraph(
                taskViewModel = taskViewModel,
                dataStoreViewModel = dataStoreViewModel,
                appViewModel = appViewModel,
                maybeShowReview = { maybeShowReview() }
            )
        }
    }

    private fun maybeShowReview() {
        val manager = ReviewManagerFactory.create(applicationContext)
        manager.requestReviewFlow().addOnCompleteListener {
            if(it.isSuccessful) {
                manager.launchReviewFlow(this, it.result)
                dataStoreViewModel.updateReviewState(ReviewStateType.SHOWN)
                taskViewModel.logReviewStatus(ReviewStateType.SHOWN)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        taskViewModel.reloadTasks()
    }
}