package com.minimalisttodolist.pleasebethelastrecyclerview.util

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.minimalisttodolist.pleasebethelastrecyclerview.data.preferences.AppPreferences
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.AppEvent
import kotlinx.coroutines.flow.first

class PermissionManager(
    private val context: Context,
    private val activity: Activity,
    private val postNotificationPermissionLauncher: ActivityResultLauncher<String>,
    private val onTaskEvent: (AppEvent) -> Unit,
    private val appPreferences: AppPreferences
) {
    companion object {
        private const val MAX_DENIAL_COUNT = 2
    }

    suspend fun requestPermissions() {
        val denialCount = appPreferences.postNotificationDenialCount.first()
        if (denialCount >= MAX_DENIAL_COUNT) {
            // Do not request permission again
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPostNotificationPermission()) {
                postNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else if (!hasScheduleExactAlarmPermission()) {
                onTaskEvent(AppEvent.ShowScheduleExactAlarmPermissionDialog)
            }
        }
    }

    suspend fun handlePostNotificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            appPreferences.resetPostNotificationDenialCount()
            if (!hasScheduleExactAlarmPermission()) {
                onTaskEvent(AppEvent.ShowScheduleExactAlarmPermissionDialog)
            }
        } else {
            appPreferences.incrementPostNotificationDenialCount()
        }
    }

    private fun hasScheduleExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }
    }

    private fun hasPostNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            activity.startActivity(intent)
        }
    }
}
