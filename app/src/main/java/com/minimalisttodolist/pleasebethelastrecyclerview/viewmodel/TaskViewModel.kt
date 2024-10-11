package com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.minimalisttodolist.pleasebethelastrecyclerview.util.AnalyticsEvents
import com.minimalisttodolist.pleasebethelastrecyclerview.util.NotificationHelper
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.DeletedTask
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.RecurrenceType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.SortType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.Task
import com.minimalisttodolist.pleasebethelastrecyclerview.data.database.TaskDao
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ClockType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.DueDateFilterType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.FirstDayOfTheWeekType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ReviewStateType
import com.minimalisttodolist.pleasebethelastrecyclerview.util.calculateNextDueDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(
    private val dao: TaskDao,
    private val notificationHelper: NotificationHelper,
    private val dataStoreViewModel: DataStoreViewModel
) : ViewModel() {

    private val _sortType = MutableStateFlow(SortType.PRIORITY)
    private val _recurrenceFilter = MutableStateFlow(RecurrenceType.NONE)
    private val _dueDateFilterType = MutableStateFlow(DueDateFilterType.NONE)
    private val _firstDayOfTheWeek = MutableStateFlow(FirstDayOfTheWeekType.MONDAY)
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private val _state = MutableStateFlow(TaskState())

    val state = combine(_state, _sortType, _recurrenceFilter, _tasks, _dueDateFilterType) { state, sortType, recurrenceType, tasks, dueDateFilterType ->
        state.copy(
            tasks = tasks,
            sortType = sortType,
            recurrenceFilter = recurrenceType,
            dueDateFilterType = dueDateFilterType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskState())

    private val _deletedTasks = MutableStateFlow<List<DeletedTask>>(emptyList())
    val deletedTasks: StateFlow<List<DeletedTask>> = _deletedTasks

    init {
        initializeDataStore()
        reloadTasks()
    }

    private fun initializeDataStore() {
        viewModelScope.launch {
            dataStoreViewModel.priorityOption.collect { _sortType.value = it }
        }
        viewModelScope.launch {
            dataStoreViewModel.recurrenceFilter.collect { _recurrenceFilter.value = it }
        }
        viewModelScope.launch {
            dataStoreViewModel.dueDateFilter.collect { _dueDateFilterType.value = it }
        }
        viewModelScope.launch {
            dataStoreViewModel.firstDayOfTheWeekType.collect { _firstDayOfTheWeek.value = it }
        }
    }

    fun onEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.DeleteTask -> handleDeleteTask(event.task)
            TaskEvent.SaveTask -> handleSaveTask()

            is TaskEvent.SetTitle -> _state.update { it.copy(title = event.title) }
            is TaskEvent.SetPriority -> _state.update { it.copy(priority = event.priority) }
            is TaskEvent.SetNote -> _state.update { it.copy(note = event.note) }
            is TaskEvent.SetDueDate -> handleSetDueDate(event.dueDate)
            is TaskEvent.SetDueTime -> handleSetDueTime(event.dueTime)
            is TaskEvent.SortTasks -> _sortType.value = event.sortType
            is TaskEvent.SetRecurrenceType -> _state.update { it.copy(recurrenceType = event.recurrenceType) }

            is TaskEvent.EditTask -> handleEditTask(event.task)

            is TaskEvent.SetRecurrenceFilter -> _recurrenceFilter.value = event.recurrenceType
            is TaskEvent.SetDueDateFilter -> _dueDateFilterType.value = event.dueDateFilterType
            TaskEvent.ClearFilters -> handleClearFilters()

            TaskEvent.ShowDatePicker -> _state.update { it.copy(isDatePickerVisible = true) }
            TaskEvent.HideDatePicker -> _state.update { it.copy(isDatePickerVisible = false) }
            TaskEvent.ShowTimePicker -> _state.update { it.copy(isTimePickerVisible = true) }
            TaskEvent.HideTimePicker -> _state.update { it.copy(isTimePickerVisible = false) }
            TaskEvent.ShowAddTaskDialog -> _state.update { it.copy(isAddTaskDialogVisible = true) }
            TaskEvent.HideAddTaskDialog -> resetAddTaskDialog()

            is TaskEvent.DeleteForever -> handleDeleteForever(event.deletedTask)
            is TaskEvent.UndoDeleteTask -> handleUndoDeleteTask(event.deletedTask)
            TaskEvent.DeleteAllHistoryTasks -> viewModelScope.launch { dao.deleteAllDeletedTasks() }

            TaskEvent.RefreshTasks -> reloadTasks()
        }
    }

    private fun handleClearFilters() {
        _recurrenceFilter.value = RecurrenceType.NONE
        _dueDateFilterType.value = DueDateFilterType.NONE
        dataStoreViewModel.saveRecurrenceFilter(_recurrenceFilter.value)
        dataStoreViewModel.saveDueDateFilter(_dueDateFilterType.value)
    }

    private fun handleDeleteTask(task: Task) {
        viewModelScope.launch {
            delay(500)

            when {
                task.recurrenceType == RecurrenceType.NONE -> {
                    // Delete the task normally
                    deleteTaskNormally(task)
                }
                task.recurrenceType != RecurrenceType.NONE && isDueOrPast(task.dueDate) -> {
                    // Task is recurring and due or past due, update with new due date
                    updateTaskWithNewDueDate(task)
                }
                else -> {
                    // Task is recurring but in the future, delete normally
                    deleteTaskNormally(task)
                }
            }
        }
    }

    private suspend fun deleteTaskNormally(task: Task) {
        notificationHelper.cancelNotification(task.id)
        dao.deleteTask(task)
        dao.insertDeletedTask(task.toDeletedTask())
    }

    private suspend fun updateTaskWithNewDueDate(task: Task) {
        val newDueDate = calculateNextDueDate(task.dueDate, task.recurrenceType)
        if (newDueDate != null) {
            val updatedTask = task.copy(dueDate = newDueDate)
            dao.upsertTask(updatedTask)
            notificationHelper.cancelNotification(task.id)
            notificationHelper.scheduleNotification(updatedTask)
        } else {
            // If we couldn't calculate a new due date, delete the task
            deleteTaskNormally(task)
        }
    }

    fun isDueOrPast(dueDate: Long?): Boolean {
        if (dueDate == null) return false
        val now = Instant.now().toEpochMilli()
        return dueDate <= now
    }

    private fun handleSaveTask() {
        val currentState = state.value
        if (currentState.title.isBlank()) {
            resetAddTaskDialog()
            return
        }

        val task = Task(
            id = currentState.editingTaskId ?: 0,
            title = currentState.title,
            priority = currentState.priority,
            note = currentState.note,
            dueDate = currentState.dueDate,
            recurrenceType = currentState.recurrenceType
        )

        logTaskSaveAnalytics(task)

        viewModelScope.launch {
            val taskId = dao.upsertTask(task)
            val savedTask = dao.getTaskById(if (taskId.toInt() == -1) currentState.editingTaskId!! else taskId.toInt())
            savedTask?.let { notificationHelper.scheduleNotification(it) }

            checkAndUpdateReviewState(taskId.toInt())
        }

        resetAddTaskDialog()
    }

    private fun checkAndUpdateReviewState(taskId: Int) {
        val currentReviewState = dataStoreViewModel.reviewStateType.value

        // 07/08/2024 current number of tasks saved by a user everyday = 5.1, reviewDialog shown after a week
        if (currentReviewState == ReviewStateType.NOT_YET && taskId >= 35) {
            dataStoreViewModel.updateReviewState(ReviewStateType.READY)
            logReviewStatus(ReviewStateType.READY)
        }
    }

    fun logReviewStatus(reviewStateType: ReviewStateType) {
        Firebase.analytics.logEvent(AnalyticsEvents.MAYBE_REVIEW_SHOWN) {
            param("reviewState", reviewStateType.toString())
        }
    }

    private fun logTaskSaveAnalytics(task: Task) {
        Firebase.analytics.logEvent(AnalyticsEvents.SAVE_TASK_CLICKED) {
            param("taskId", task.id.toString())
            param("priority", (task.priority != 0).toString())
            param("note", (task.note.isNotBlank()).toString())
            param("dueDateTime", (task.dueDate != null).toString())
            param("repeat", (task.recurrenceType != RecurrenceType.NONE).toString())
        }

        val eventMap = mapOf(
            1000 to AnalyticsEvents.ABOVE_1000,
            900 to AnalyticsEvents.ABOVE_900,
            800 to AnalyticsEvents.ABOVE_800,
            700 to AnalyticsEvents.ABOVE_700,
            600 to AnalyticsEvents.ABOVE_600,
            500 to AnalyticsEvents.ABOVE_500,
            400 to AnalyticsEvents.ABOVE_400,
            300 to AnalyticsEvents.ABOVE_300,
            200 to AnalyticsEvents.ABOVE_200,
            100 to AnalyticsEvents.ABOVE_100,
            0 to AnalyticsEvents.ABOVE_0
        )
        eventMap.entries.firstOrNull { task.id > it.key }?.let { entry ->
            Firebase.analytics.logEvent(entry.value) {
                param("screen", "AddTaskDialog")
            }
        }
    }

    private fun handleEditTask(task: Task) {
        _state.update {
            val dueDateOnly = getLocalDateFromEpochMilliWithNull(task.dueDate)
            val dueTimeOnly = getLocalTimeFromEpochMilliWithNull(task.dueDate)
            it.copy(
                title = task.title,
                priority = task.priority,
                note = task.note,
                dueDate = task.dueDate,
                dueDateOnly = dueDateOnly,
                dueTimeOnly = dueTimeOnly,
                recurrenceType = task.recurrenceType,
                isAddTaskDialogVisible = true,
                editingTaskId = task.id
            )
        }
    }

    private fun handleSetDueDate(dueDate: LocalDate?) {
        _state.update {
            it.copy(dueDateOnly = dueDate).also { updatedState ->
                combineDateAndTime(updatedState)
            }
        }
    }

    private fun handleSetDueTime(dueTime: LocalTime) {
        _state.update {
            it.copy(dueTimeOnly = dueTime).also { updatedState ->
                combineDateAndTime(updatedState)
            }
        }
    }

    private fun handleDeleteForever(deletedTask: DeletedTask) {
        viewModelScope.launch {
            delay(500)
            dao.deleteDeletedTask(deletedTask)
        }
    }

    private fun handleUndoDeleteTask(deletedTask: DeletedTask) {
        viewModelScope.launch {
            delay(500)
            val restoredTask = deletedTask.toTask()
            val taskId = dao.upsertTask(restoredTask)
            val savedTask = dao.getTaskById(taskId.toInt())
            dao.deleteDeletedTask(deletedTask)
            notificationHelper.cancelNotification(savedTask!!.id)
            notificationHelper.scheduleNotification(savedTask)
        }
    }

    private fun resetAddTaskDialog() {
        _state.update {
            it.copy(
                isAddTaskDialogVisible = false,
                title = "",
                priority = 0,
                note = "",
                dueDate = null,
                dueDateOnly = null,
                dueTimeOnly = null,
                recurrenceType = RecurrenceType.NONE,
                nextDueDate = null,
                editingTaskId = null
            )
        }
    }

    fun reloadTasks() {
        viewModelScope.launch {
            combine(
                _sortType,
                _recurrenceFilter,
                _dueDateFilterType,
                dao.getDeletedTasks()
            ) { sortType, recurrenceType, dueDateFilterType, deletedTasks ->
                Pair(Triple(sortType, recurrenceType, dueDateFilterType), deletedTasks)
            }
                .flatMapLatest { (triple, deletedTasks) ->
                    val (sortType, recurrenceType, dueDateFilterType) = triple
                    when (sortType) {
                        SortType.ALPHABETICAL -> dao.getTasksOrderedAlphabetically()
                        SortType.ALPHABETICAL_REV -> dao.getTasksOrderedAlphabeticallyRev()
                        SortType.DUE_DATE -> dao.getTasksSortedByDueDate()
                        SortType.PRIORITY -> dao.getTasksSortedByPriority()
                    }.map { tasks ->
                        Pair(
                            tasks.filter { task ->
                                (recurrenceType == RecurrenceType.NONE || task.recurrenceType == recurrenceType) &&
                                        isWithinDueDateFilter(task, dueDateFilterType, _firstDayOfTheWeek.value)
                            },
                            deletedTasks
                        )
                    }
                }
                .collect { (filteredTasks, deletedTasks) ->
                    _tasks.value = filteredTasks
                    _deletedTasks.value = deletedTasks
                }
        }
    }

    private fun isWithinDueDateFilter(
        task: Task,
        filterType: DueDateFilterType,
        firstDayOfWeek: FirstDayOfTheWeekType
    ): Boolean {
        val taskDueDate = task.dueDate?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        } ?: return filterType == DueDateFilterType.NONE

        val now = LocalDate.now()
        return when (filterType) {
            DueDateFilterType.NONE -> true
            DueDateFilterType.TODAY -> taskDueDate == now
            DueDateFilterType.THIS_WEEK -> {
                val startOfWeek = when (firstDayOfWeek) {
                    FirstDayOfTheWeekType.MONDAY -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    FirstDayOfTheWeekType.SUNDAY -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                    FirstDayOfTheWeekType.SATURDAY -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))
                }
                val endOfWeek = startOfWeek.plusDays(6)
                taskDueDate in startOfWeek..endOfWeek
            }
            DueDateFilterType.THIS_MONTH -> taskDueDate.year == now.year && taskDueDate.month == now.month
            DueDateFilterType.THIS_YEAR -> taskDueDate.year == now.year
        }
    }

    private fun combineDateAndTime(state: TaskState) {
        val date = state.dueDateOnly
        val time = state.dueTimeOnly
        if (date != null) {
            val combinedDateTime = if (time != null) date.atTime(time) else date.atStartOfDay()
            _state.update {
                it.copy(dueDate = combinedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            }
        } else {
            _state.update {
                it.copy(dueDate = null, dueTimeOnly = null, recurrenceType = RecurrenceType.NONE)
            }
        }
    }

    private fun Task.toDeletedTask(): DeletedTask {
        return DeletedTask(
            id = this.id,
            title = this.title,
            priority = this.priority,
            note = this.note,
            dueDate = this.dueDate,
            recurrenceType = this.recurrenceType,
            deletedAt = System.currentTimeMillis()
        )
    }

    private fun DeletedTask.toTask(): Task {
        return Task(
            id = this.id,
            title = this.title,
            priority = this.priority,
            note = this.note,
            dueDate = this.dueDate,
            recurrenceType = this.recurrenceType,
        )
    }

    fun formatDueDateWithDateTime(epochMilli: Long?): String {
        val formatter = when (dataStoreViewModel.clockType.value) {
            ClockType.TWELVE_HOUR -> DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")
            ClockType.TWENTY_FOUR_HOUR -> DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        }
        val dateFormatterWithoutTime = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        val dateFormatterCurrentYear = DateTimeFormatter.ofPattern("MMM dd")
        val dateTimeFormatterCurrentYear = when (dataStoreViewModel.clockType.value) {
            ClockType.TWELVE_HOUR -> DateTimeFormatter.ofPattern("MMM dd hh:mm a")
            ClockType.TWENTY_FOUR_HOUR -> DateTimeFormatter.ofPattern("MMM dd HH:mm")
        }
        val timeFormatter = when (dataStoreViewModel.clockType.value) {
            ClockType.TWELVE_HOUR -> DateTimeFormatter.ofPattern("hh:mm a")
            ClockType.TWENTY_FOUR_HOUR -> DateTimeFormatter.ofPattern("HH:mm")
        }
        val currentYear = LocalDateTime.now().year
        val today = LocalDateTime.now().toLocalDate()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        epochMilli ?: return ""
        val dateTime = Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val isMidnight = dateTime.hour == 0 && dateTime.minute == 0
        return when (dateTime.toLocalDate()) {
            today -> "Today" + if (!isMidnight) " ${dateTime.format(timeFormatter)}" else ""
            yesterday -> "Yesterday" + if (!isMidnight) " ${dateTime.format(timeFormatter)}" else ""
            tomorrow -> "Tomorrow" + if (!isMidnight) " ${dateTime.format(timeFormatter)}" else ""
            else -> {
                if (dateTime.year == currentYear) {
                    if (isMidnight) dateTime.format(dateFormatterCurrentYear)
                    else dateTime.format(dateTimeFormatterCurrentYear)
                } else {
                    if (isMidnight) dateTime.format(dateFormatterWithoutTime)
                    else dateTime.format(formatter)
                }
            }
        }
    }

    fun formatDueDateWithDateOnly(epochMilli: Long?): String {
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        val dateFormatterCurrentYear = DateTimeFormatter.ofPattern("MMM dd")
        val currentYear = LocalDateTime.now().year
        val today = LocalDateTime.now().toLocalDate()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        epochMilli ?: return ""
        val dateTime = Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return when (dateTime.toLocalDate()) {
            today -> "Today"
            yesterday -> "Yesterday"
            tomorrow -> "Tomorrow"
            else -> {
                if (dateTime.year == currentYear) dateTime.format(dateFormatterCurrentYear)
                else dateTime.format(dateFormatter)
            }
        }
    }

    fun formatDueDateWithTimeOnly(epochMilli: Long?): String {
        val timeFormatter = when (dataStoreViewModel.clockType.value) {
            ClockType.TWELVE_HOUR -> DateTimeFormatter.ofPattern("hh:mm a")
            ClockType.TWENTY_FOUR_HOUR -> DateTimeFormatter.ofPattern("HH:mm")
        }
        epochMilli ?: return ""
        val dateTime = Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime()
        if (dateTime.hour == 0 && dateTime.minute == 0) {
            return ""
        }
        return dateTime.format(timeFormatter)
    }

    fun getLocalDateFromEpochMilli(epochMilli: Long?): LocalDate {
        epochMilli ?: return LocalDate.now()
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun getLocalTimeFromEpochMilli(epochMilli: Long?): LocalTime {
        epochMilli ?: return LocalTime.now()
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalTime()
    }

    fun getLocalDateFromEpochMilliWithNull(epochMilli: Long?): LocalDate? {
        epochMilli ?: return null
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun getLocalTimeFromEpochMilliWithNull(epochMilli: Long?): LocalTime? {
        epochMilli ?: return null
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalTime()
    }
}

class TaskViewModelFactory(
    private val dao: TaskDao,
    private val notificationHelper: NotificationHelper,
    private val dataStoreViewModel: DataStoreViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(dao, notificationHelper, dataStoreViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}