package com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel

import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.DueDateFilterType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.RecurrenceType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.SortType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.Task
import java.time.LocalDate
import java.time.LocalTime

data class TaskState(
    val tasks: List<Task> = emptyList(),
    val title: String = "",
    val priority: Int = 0,
    val note: String = "",
    val sortType: SortType = SortType.PRIORITY,
    val isDatePickerVisible: Boolean = false,
    val isTimePickerVisible: Boolean = false,
    val isAddTaskDialogVisible: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceFilter: RecurrenceType = RecurrenceType.NONE,
    val dueDateFilterType: DueDateFilterType = DueDateFilterType.NONE,
    val nextDueDate: Long? = null,
    val dueDate: Long? = null,
    val dueDateOnly: LocalDate? = null,
    val dueTimeOnly: LocalTime? = null,
    val editingTaskId: Int? = null,
)
