package com.minimalisttodolist.pleasebethelastrecyclerview.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.minimalisttodolist.pleasebethelastrecyclerview.util.AnalyticsEvents
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.DueDateFilterType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.PriorityColor
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.RecurrenceType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ReviewStateType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.Task
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.CompleteIcon
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.emptyStateMessages
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.DataStoreViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskEvent
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskState
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs.AddTaskDialog
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs.Theme_FontSettingsDialog
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs.HistoryDialog
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs.MenuDialog
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs.PersonalizeDialog
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs.ScheduleExactAlarmPermissionDialog
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs.Tutorial
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.AppEvent
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.AppState
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.DateRed
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.LocalDarkTheme
import com.minimalisttodolist.pleasebethelastrecyclerview.util.vibrate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskScreen(
    taskState: TaskState,
    appState: AppState,
    onEvent: (TaskEvent) -> Unit,
    onAppEvent: (AppEvent) -> Unit,
    taskViewModel: TaskViewModel,
    dataStoreViewModel: DataStoreViewModel,
    maybeShowReview: () -> Unit
) {
    val darkTheme = LocalDarkTheme.current

    val tutorialVisibility = dataStoreViewModel.tutorialVisibility.collectAsState()

    // Necessary for the vibrate function
    val context = LocalContext.current

    // Generate a random message when the task list becomes empty
    val randomMessage = remember(taskState.tasks.isEmpty()) {
        if (taskState.tasks.isEmpty()) {
            if(tutorialVisibility.value) {
                emptyStateMessages.first()
            } else {
                emptyStateMessages.random()
            }
        } else {
            ""
        }
    }

    Scaffold(
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (tutorialVisibility.value) 32.dp else 0.dp),
                horizontalArrangement = if (tutorialVisibility.value) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tutorialVisibility.value) {
                    FloatingActionButton(
                        onClick = {
                            vibrate(context = context, strength = 1)
                            onAppEvent(AppEvent.ShowTutorialDialog)
                            Firebase.analytics.logEvent(AnalyticsEvents.TUTORIAL_CLICKED){
                                param("screen", "TaskScreen")
                            }
                        },
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(percent = 50))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.onBackground,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .size(36.dp),
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) {
                        Image(
                            painter = painterResource( id = com.minimalisttodolist.pleasebethelastrecyclerview.R.drawable.light_bulb ),
                            contentDescription = "Show Tutorial",
                            modifier = Modifier
                                .padding(8.dp),
                            colorFilter = ColorFilter.tint(PriorityColor.PRIORITY1.getColor(darkTheme))
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                FloatingActionButton(
                    onClick = {},
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(percent = 7))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(percent = 25)
                        )
                    ,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add task",
                        modifier = Modifier.combinedClickable(
                            onLongClick = {
                                vibrate(context = context, strength = 1)
                                onAppEvent(AppEvent.ShowMenuDialog)
                                Firebase.analytics.logEvent(AnalyticsEvents.MENU_CLICKED){
                                    param("screen", "TaskScreen")
                                }
                            },
                            onClick = {
                                vibrate(context = context, strength = 1)
                                onEvent(TaskEvent.ShowAddTaskDialog)
                                Firebase.analytics.logEvent(AnalyticsEvents.ADD_TASK_CLICKED){
                                    param("screen", "TaskScreen")
                                }
                            }
                        )
                    )
                }
            }
        },
    ) { padding ->

        TaskList(onEvent, onClearFilters = { onEvent(TaskEvent.ClearFilters) }, taskState, taskViewModel, padding, dataStoreViewModel, checkAndShowReview = {
            if (dataStoreViewModel.reviewStateType.value == ReviewStateType.READY) {
                maybeShowReview()
            }
        })

        if (taskState.isAddTaskDialogVisible) {
            AddTaskDialog(taskState, onEvent, taskViewModel, dataStoreViewModel, onAppEvent, isEdit = taskState.editingTaskId != null)
        }

        if (appState.isMenuDialogVisible) {
            MenuDialog(taskState, onEvent, dataStoreViewModel = dataStoreViewModel, onAppEvent = onAppEvent)
        }

        if (appState.isPersonalizeDialogVisible) {
            PersonalizeDialog(
                onAppEvent = onAppEvent,
                dataStoreViewModel = dataStoreViewModel,
                onBack = {
                    onAppEvent(AppEvent.HidePersonalizeDialog)
                    onAppEvent(AppEvent.ShowMenuDialog)
                },
                onTaskEvent = onEvent
            )
        }

        if (appState.isHistoryDialogVisible) {
            HistoryDialog(
                taskViewModel,
                onEvent,
                onAppEvent = onAppEvent,
                onBack = {
                    onAppEvent(AppEvent.HideHistoryDialog)
                    onAppEvent(AppEvent.ShowMenuDialog)
                }
            )
        }

        if (appState.isScheduleExactAlarmPermissionDialogVisible) {
            ScheduleExactAlarmPermissionDialog(
                onDismissOrDisallow = {
                    onAppEvent(AppEvent.HideScheduleExactAlarmPermissionDialog)
                    onAppEvent(AppEvent.IncrementPostNotificationDenialCount)
                },
                onAllow = {
                    onAppEvent(AppEvent.ShowScheduleExactAlarmPermissionIntent)
                    onAppEvent(AppEvent.HideScheduleExactAlarmPermissionDialog)
                },
            )
        }

        if (appState.isFontSettingsDialogVisible) {
            Theme_FontSettingsDialog(
                dataStoreViewModel = dataStoreViewModel,
                onDismiss = { onAppEvent(AppEvent.HideFontSettingsDialog) },
                onBack = {
                    onAppEvent(AppEvent.HideFontSettingsDialog)
                    onAppEvent(AppEvent.ShowPersonalizeDialog)
                }
            )
        }

        if (appState.isTutorialDialogVisible) {
            Tutorial(
                onDismiss = {
                    onAppEvent(AppEvent.HideTutorialDialog)
                },
                onDisable = {
                    onAppEvent(AppEvent.HideTutorialDialog)
                    onAppEvent(AppEvent.DisableTutorialDialog)
                },
                onShowAddTaskDialog = {
                    onEvent(TaskEvent.ShowAddTaskDialog)
                },
                onShowMenuDialog = {
                    onAppEvent(AppEvent.ShowMenuDialog)
                },
                onEdit = {
                    onEvent(TaskEvent.EditTask(it))
                },
                viewModel = taskViewModel
            )
        }

        if (taskState.tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = randomMessage,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .clickable {
                            onEvent(TaskEvent.ShowAddTaskDialog)
                        }
                        .padding(32.dp)
                )
            }
        }
    }
}

@Composable
fun TaskList(onEvent: (TaskEvent) -> Unit, onClearFilters: () -> Unit, taskState: TaskState, viewModel: TaskViewModel, padding: PaddingValues, dataStoreViewModel: DataStoreViewModel, checkAndShowReview: () -> Unit) {
    val dueDateFilterType by dataStoreViewModel.dueDateFilter.collectAsState()
    val recurrenceType by dataStoreViewModel.recurrenceFilter.collectAsState()
    val filterText = buildString {
        if (dueDateFilterType != DueDateFilterType.NONE) {
            append(dueDateFilterType.toDisplayString())
        }
        if (recurrenceType != RecurrenceType.NONE) {
            if (isNotEmpty()) append(", ")
            append(recurrenceType.toDisplayString())
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(top = 8.dp),
    ) {
        item {
            AnimatedVisibility(
                visible = filterText.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkOut() + fadeOut(),
            ) {
                Text(
                    text = filterText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClearFilters() }
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                )
            }
        }
        items(taskState.tasks, key = { it.id }) { task ->
            var visible by remember { mutableStateOf(true) }

            // For calling the Delay before onEvent(Delete)
            val coroutineScope = rememberCoroutineScope()

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
                        + shrinkVertically(animationSpec = tween(300)),
            ) {
                TaskItem(
                    task = task,
                    onEdit = {
                        onEvent(TaskEvent.EditTask(it))
                        Firebase.analytics.logEvent(AnalyticsEvents.EDIT_CLICKED){
                            param("screen", "TaskScreen")
                        }
                    },
                    onDelete = {
                        coroutineScope.launch {
                            delay(300)
                            onEvent(TaskEvent.DeleteTask(task))
                            if (
                                task.recurrenceType == RecurrenceType.NONE ||
                                task.dueDate?.let { it <= System.currentTimeMillis() } == false
                            ) {
                                visible = false
                            }
                        }
                        checkAndShowReview()
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onEdit: (Task) -> Unit, onDelete: (Task) -> Unit, viewModel: TaskViewModel) {
    val darkTheme = LocalDarkTheme.current
    val context = LocalContext.current

    val priorityColor = when (task.priority) {
        0 -> PriorityColor.PRIORITY0.getColor(darkTheme)
        1 -> PriorityColor.PRIORITY1.getColor(darkTheme)
        2 -> PriorityColor.PRIORITY2.getColor(darkTheme)
        3 -> PriorityColor.PRIORITY3.getColor(darkTheme)
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .clickable {
                vibrate(context = context, strength = 1)
                onEdit(task)
            }
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = "Priority",
            tint = priorityColor,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                task.title,
                style = MaterialTheme.typography.titleLarge,
            )
            DueDate_Recurrence_Note(task = task, viewModel = viewModel)
        }
        CompleteIcon(
            onDelete = {
                onDelete(task)
                Firebase.analytics.logEvent(AnalyticsEvents.COMPLETE_TASK_CLICKED){
                    param("screen", "TaskScreen")
                }
            }
        )
    }
}

@Composable
fun DueDate_Recurrence_Note(
    task: Task,
    viewModel: TaskViewModel
) {
    val dueDate = viewModel.formatDueDateWithDateTime(task.dueDate)
    val note = task.note

    val textColor = if (task.dueDate?.let {
        viewModel.isDueOrPast(it)
        } == true) {
        DateRed
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    if (dueDate.isNotEmpty()) {
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = textColor)) {
                    append(dueDate)
                }
                if(task.recurrenceType != RecurrenceType.NONE) {
                    withStyle(style = SpanStyle(color = textColor)) {
                        append(", ")
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.tertiary)) {
                        append(task.recurrenceType.toDisplayString())
                    }
                }
                if (note.isNotBlank()) {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.tertiary)) {
                        append(" | ")
                        append(note)
                    }
                }
            },
            style = MaterialTheme.typography.bodySmall
        )
    } else if(note.isNotEmpty()) {
        Text(
            text = note,
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
