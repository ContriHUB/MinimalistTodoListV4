package com.minimalisttodolist.pleasebethelastrecyclerview.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.minimalisttodolist.pleasebethelastrecyclerview.R
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.Task

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleNotification(task: Task) {
        val notificationIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
        }
        Log.d("TAG", task.id.toString())
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationTime = task.dueDate

        // Check if the due date is in the past
        if (notificationTime != null && notificationTime <= System.currentTimeMillis()) {
            Log.d("TAG", "Notification time is in the past, not scheduling")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(alarmManager.canScheduleExactAlarms()) {
                notificationTime?.let {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        it,
                        pendingIntent
                    )
                }
            }
        } else {
            notificationTime?.let {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    it,
                    pendingIntent
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showNotification(taskId: Int, taskTitle: String) {
        val channelId = "task_reminders"
        val channelName = "Task Reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance)
        notificationManager.createNotificationChannel(channel)

        val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "COMPLETE"
            putExtra("TASK_ID", taskId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId * 2,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "SNOOZE"
            putExtra("TASK_ID", taskId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId * 2 + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo_dark)
            .setContentTitle(taskTitle)
            .setContentText("Your task is due now")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.logo_dark, "Complete", completePendingIntent)
            .addAction(R.drawable.logo_dark, "Snooze", snoozePendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId, notification)
    }

    fun cancelNotification(taskId: Int) {
        Log.d("TAG", taskId.toString())
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        notificationManager.cancel(taskId)
        Log.d("TAG", "end")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun snoozeNotification(taskId: Int, task: Task) {
        // Cancel the current notification
        cancelNotification(taskId)

        // Reschedule for 10 minutes later
        val newDueDate = System.currentTimeMillis() + 10 * 60 * 1000 // 10 minutes from now
        val updatedTask = task.copy(dueDate = newDueDate)
        scheduleNotification(updatedTask)

        Log.d("TAG", "Notification snoozed for taskId: $taskId")
    }
}
