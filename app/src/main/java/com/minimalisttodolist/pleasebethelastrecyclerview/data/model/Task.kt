package com.minimalisttodolist.pleasebethelastrecyclerview.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    val title: String,
    val priority: Int,
    val note: String,
    val dueDate: Long?, // Timestamp in milliseconds

    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val isCompleted: Boolean = false // Future use (strike-through)
)