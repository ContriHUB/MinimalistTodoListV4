package com.minimalisttodolist.pleasebethelastrecyclerview.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.RecurrenceType
import java.time.Instant
import java.time.ZoneId


@RequiresApi(Build.VERSION_CODES.O)
fun calculateNextDueDate(dueDate: Long?, recurrenceType: RecurrenceType): Long? {
    if (dueDate == null) return null
    val dateTime = Instant.ofEpochMilli(dueDate).atZone(ZoneId.systemDefault()).toLocalDateTime()
    var nextDateTime = dateTime
    while (nextDateTime.isBefore(LocalDateTime.now())) {
        nextDateTime = when (recurrenceType) {
            RecurrenceType.DAILY -> nextDateTime.plusDays(1)
            RecurrenceType.WEEKLY -> nextDateTime.plusWeeks(1)
            RecurrenceType.MONTHLY -> nextDateTime.plusMonths(1)
            RecurrenceType.YEARLY -> nextDateTime.plusYears(1)
            else -> return null
        }
    }
    return nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

