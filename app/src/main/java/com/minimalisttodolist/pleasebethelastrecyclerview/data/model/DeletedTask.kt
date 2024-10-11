package com.minimalisttodolist.pleasebethelastrecyclerview.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_tasks")
data class DeletedTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val priority: Int,
    val note: String,
    val dueDate: Long? = null,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val nextDueDate: Long? = null,
    val deletedAt: Long // Timestamp of deletion
)
