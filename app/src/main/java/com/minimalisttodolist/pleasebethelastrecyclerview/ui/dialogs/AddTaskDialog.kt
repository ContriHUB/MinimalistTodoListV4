package com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.PriorityColor
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.RecurrenceType
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.CustomBox
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.DatePicker
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskEvent
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskState
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.TimePickerFromOldApp
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.LocalDarkTheme
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.AppEvent
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.DataStoreViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    taskState: TaskState,
    onEvent: (TaskEvent) -> Unit,
    viewModel: TaskViewModel,
    dataStoreViewModel: DataStoreViewModel,
    onAppEvent: (AppEvent) -> Unit,
    isEdit: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val focusRequesterNote = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showWarning by remember { mutableStateOf(false) }

    val borderAlpha by animateFloatAsState(
        targetValue = if (showWarning) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = EaseOutExpo),
        label = "Border flash"
    )

    if(!isEdit) {
        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(taskState.title) {
        if (taskState.title.isNotBlank()) {
            showWarning = false
        }
    }

    BasicAlertDialog(
        onDismissRequest = { onEvent(TaskEvent.HideAddTaskDialog) },
    ) {
        CustomBox {
            Column(
                // Best thing ever -> IntrinsicSize
                modifier = Modifier
                    .padding(15.dp)
                    .width(IntrinsicSize.Max)
                    .widthIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Title(
                    taskState = taskState,
                    onEvent = onEvent,
                    focusRequester = focusRequester,
                    borderAlpha = borderAlpha
                )

                PrioritySelector(priorityFromEdit = taskState.priority, onPriorityChange = onEvent)

                Note(taskState = taskState, onEvent = onEvent, focusRequester = focusRequesterNote)

                DateSelector(taskState = taskState, onEvent = onEvent, viewModel = viewModel, onAppEvent = onAppEvent)

                if (taskState.dueDate != null) {
                    TimeSelector(taskState = taskState, onEvent = onEvent, viewModel = viewModel, dataStoreViewModel = dataStoreViewModel)
                    RecurrenceSelector(
                        recurrenceFromEdit = taskState.recurrenceType,
                        onRecurrenceTypeSelected = { recurrenceType ->
                            onEvent(TaskEvent.SetRecurrenceType(recurrenceType))
                        }
                    )
                }

                SaveButton(
                    onSave = {
                        onEvent(TaskEvent.SaveTask)
                    },
                    canSave = { taskState.title.isNotBlank() },
                    onInvalidSave = {
                        focusRequesterNote.requestFocus()
                        focusRequester.requestFocus()
                        keyboardController?.show()
                        showWarning = true
                        // Start a coroutine to reset the warning state after a delay
                        viewModel.viewModelScope.launch {
                            delay(500) // Adjust this delay to control how long the border flashes
                            showWarning = false
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun Title(
    modifier: Modifier = Modifier,
    taskState: TaskState,
    onEvent: (TaskEvent) -> Unit,
    focusRequester: FocusRequester,
    borderAlpha: Float
) {
    val textFieldValue = remember(taskState.title) {
        mutableStateOf(TextFieldValue(taskState.title, TextRange(taskState.title.length)))
    }
    val darkTheme = LocalDarkTheme.current
    val borderColor = PriorityColor.PRIORITY3.getColor(darkTheme).copy(alpha = borderAlpha)

    TextField(
        value = textFieldValue.value,
        onValueChange = {
            textFieldValue.value = it
            onEvent(TaskEvent.SetTitle(it.text))
        },
//        singleLine = true,
        maxLines = 5,
        placeholder = {
            Text(
                text = "I want to...",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            unfocusedTextColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.primary
        ),
        textStyle = LocalTextStyle.current.copy(
            fontSize = MaterialTheme.typography.titleLarge.fontSize
        ),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(percent = 25),
            ),
    )
}

@Composable
fun PrioritySelector(
    priorityFromEdit: Int,
    onPriorityChange: (TaskEvent) -> Unit
) {
    var selectedPriority by remember { mutableStateOf(priorityFromEdit) }
    var isPrioritySelected by remember { mutableStateOf(priorityFromEdit != 0) }

    AnimatedContent(
        targetState = isPrioritySelected || selectedPriority != 0,
        label = "Priority Selector Size Change Animation"
    ) { targetState ->
        if (targetState) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                PriorityStar(index = 3, selectedPriority) { priority ->
                    selectedPriority = if (priority == selectedPriority) {
                        0
                    } else {
                        priority
                    }
                    onPriorityChange(TaskEvent.SetPriority(selectedPriority))
                }
                PriorityStar(index = 2, selectedPriority) { priority ->
                    selectedPriority = if (priority == selectedPriority) {
                        0
                    } else {
                        priority
                    }
                    onPriorityChange(TaskEvent.SetPriority(selectedPriority))
                }
                PriorityStar(index = 1, selectedPriority) { priority ->
                    selectedPriority = if (priority == selectedPriority) {
                        0
                    } else {
                        priority
                    }
                    onPriorityChange(TaskEvent.SetPriority(selectedPriority))
                }
            }
        }
        else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .clickable {
                        isPrioritySelected = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    tint = MaterialTheme.colorScheme.tertiary,
                    contentDescription = "Priority Toggle Icon",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Priority",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(15.dp)
                )
            }
        }
    }
}

@Composable
fun PriorityStar(index: Int, selectedPriority: Int, onPriorityChange: (Int) -> Unit) {
    val darkTheme = LocalDarkTheme.current

    val starColor = when (index) {
        0 -> PriorityColor.PRIORITY0.getColor(darkTheme)
        1 -> PriorityColor.PRIORITY1.getColor(darkTheme)
        2 -> PriorityColor.PRIORITY2.getColor(darkTheme)
        3 -> PriorityColor.PRIORITY3.getColor(darkTheme)
        else -> Color.Gray
    }

    val starIcon = if (index == selectedPriority) Icons.Rounded.Star else Icons.Rounded.StarOutline

    Icon(
        imageVector = starIcon,
        contentDescription = "Priority $index",
        tint = starColor,
        modifier = Modifier
            .size(30.dp)
            .clickable { onPriorityChange(index) }
    )
}

@Composable
fun Note(modifier: Modifier = Modifier, taskState: TaskState, onEvent: (TaskEvent) -> Unit, focusRequester: FocusRequester) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.StickyNote2,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "Note Icon",
            modifier = Modifier.size(20.dp)
        )
        TextField(
            value = taskState.note,
            onValueChange = { onEvent(TaskEvent.SetNote(it)) },
            modifier=Modifier.focusRequester(focusRequester) ,
            placeholder = {
                Text(
                    text = "Note",
                    color = MaterialTheme.colorScheme.tertiary,
//                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedTextColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.primary,
            ),
            textStyle = LocalTextStyle.current.copy(
            ),
            maxLines = 3,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateSelector(modifier: Modifier = Modifier, taskState: TaskState, onEvent: (TaskEvent) -> Unit, viewModel: TaskViewModel, onAppEvent: (AppEvent) -> Unit) {
    val emptyText = "Date"
    val text = viewModel.formatDueDateWithDateOnly(taskState.dueDate).ifEmpty { emptyText }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 4.dp, bottom = 4.dp)
            .clickable { onEvent(TaskEvent.ShowDatePicker) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.DateRange,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "Date Icon",
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            modifier = Modifier
                .padding(15.dp),
            color = if (text == emptyText) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    if (taskState.isDatePickerVisible) {
        DatePicker(
            onDateSelected = { date ->
                onEvent(TaskEvent.SetDueDate(date))
            },
            closeSelection = {
                onEvent(TaskEvent.HideDatePicker)
                if(viewModel.state.value.dueDate != null) {
                    onAppEvent(AppEvent.CheckNotificationPermissions)
                }
            },
            initialDate = viewModel.getLocalDateFromEpochMilli(taskState.dueDate)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeSelector(modifier: Modifier = Modifier, taskState: TaskState, onEvent: (TaskEvent) -> Unit, viewModel: TaskViewModel, dataStoreViewModel: DataStoreViewModel) {
    val text = viewModel.formatDueDateWithTimeOnly(taskState.dueDate).ifEmpty { "Time" }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.AccessTime,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "Time Icon",
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            modifier = Modifier
                .padding(15.dp)
                .clickable { onEvent(TaskEvent.ShowTimePicker) },
            color = if (text == "Time") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
//            fontWeight = FontWeight.Light,
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    if (taskState.isTimePickerVisible) {
        TimePickerFromOldApp(
            onTimeSelected = { time -> onEvent(TaskEvent.SetDueTime(time)) },
            closeSelection = { onEvent(TaskEvent.HideTimePicker) },
            initialTime = viewModel.getLocalTimeFromEpochMilli(taskState.dueDate),
            dataStoreViewModel = dataStoreViewModel
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecurrenceSelector(
    recurrenceFromEdit: RecurrenceType,
    onRecurrenceTypeSelected: (RecurrenceType) -> Unit
) {
    var selectedRecurrenceType by remember { mutableStateOf(recurrenceFromEdit) }
    var isRecurrenceSelected by remember { mutableStateOf(recurrenceFromEdit != RecurrenceType.NONE) }

    AnimatedContent(
        targetState = isRecurrenceSelected || selectedRecurrenceType != RecurrenceType.NONE,
        label = "Recurrence Selector Size Change Animation"
    ) { targetState ->
        if(targetState) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 11.dp, bottom = 11.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Top,
            ) {
                RecurrenceType.entriesWithoutNONE.forEach { recurrenceType ->
                    Text(
                        text = recurrenceType.toDisplayString(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .clickable {
                                selectedRecurrenceType = if (selectedRecurrenceType == recurrenceType) {
                                    RecurrenceType.NONE
                                } else {
                                    recurrenceType
                                }
                                onRecurrenceTypeSelected(selectedRecurrenceType)
                            }
                            .background(
                                color = if (recurrenceType == selectedRecurrenceType) MaterialTheme.colorScheme.primary
                                else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (recurrenceType == selectedRecurrenceType) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, top = 4.dp, bottom = 4.dp)
                    .clickable {
                        isRecurrenceSelected = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Repeat,
                    tint = MaterialTheme.colorScheme.tertiary,
                    contentDescription = "Recurrence Toggle Icon",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Repeat",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(15.dp)
                )
            }
        }
    }
}

@Composable
fun SaveButton(
    onSave: () -> Unit,
    canSave: () -> Boolean,
    onInvalidSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable {
                if (canSave()) {
                    onSave()
                } else {
                    onInvalidSave()
                }
            }
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = RoundedCornerShape(percent = 50)
            ),
    ) {
        Text(
            text = "SAVE",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 25.dp, vertical = 10.dp)
        )
    }
}