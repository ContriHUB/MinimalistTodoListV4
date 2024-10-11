package com.minimalisttodolist.pleasebethelastrecyclerview.ui.components

import android.app.AlertDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ClockType
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.DataStoreViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(value = 26)
@Composable
fun DatePicker(
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate?) -> Unit,
    closeSelection: () -> Unit,
    initialDate: LocalDate
) {
    val initialMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    val dialogButtonsPadding = PaddingValues(bottom = 8.dp, end = 18.dp)

    // Define a scale factor
    val scaleFactor = 0.9f

    // A useless comment

    BasicAlertDialog(
        onDismissRequest = { closeSelection() },
        modifier = modifier
            .scale(scaleFactor)
            .wrapContentHeight()
        ,
    ) {
        CustomBox(
            modifier = Modifier
                .requiredWidth(360.dp)
                .heightIn(max = 568.dp),
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                androidx.compose.material3.DatePicker(
                    state = datePickerState,
                    headline = null,
                    title = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        headlineContentColor = MaterialTheme.colorScheme.primary,
                        weekdayContentColor = MaterialTheme.colorScheme.primary,
                        subheadContentColor = MaterialTheme.colorScheme.primary,
                        yearContentColor = MaterialTheme.colorScheme.primary,
                        currentYearContentColor = MaterialTheme.colorScheme.primary,
                        selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                        selectedYearContentColor = MaterialTheme.colorScheme.secondary,
                        dayContentColor = MaterialTheme.colorScheme.primary,
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.secondary,
                        todayContentColor = MaterialTheme.colorScheme.primary,
                        todayDateBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(dialogButtonsPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                onDateSelected(null)
                                closeSelection()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Clear")
                        }
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val selectedDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()
                                    onDateSelected(selectedDate)
                                }
                                closeSelection()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerFromOldApp(
    modifier: Modifier = Modifier,
    onTimeSelected: (LocalTime) -> Unit,
    closeSelection: () -> Unit,
    initialTime: LocalTime,
    dataStoreViewModel: DataStoreViewModel
) {
    val selectedTime = remember { mutableStateOf<LocalTime?>(null) }

    val clockType by dataStoreViewModel.clockType.collectAsState()
    var is24Hour by remember { mutableStateOf(false) }
    is24Hour = when (clockType) {
        ClockType.TWELVE_HOUR -> false
        ClockType.TWENTY_FOUR_HOUR -> true
    }
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = is24Hour
    )
    val dialogButtonsPadding = PaddingValues(bottom = 8.dp, end = 18.dp)

    // Define a scale factor
    val scaleFactor = 0.9f

    BasicAlertDialog(
        onDismissRequest = {
            closeSelection()
        },
        modifier = Modifier
            .scale(scaleFactor)
    ) {
        CustomBox(
            modifier = Modifier
                .requiredWidth(360.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = 24.dp)
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.background,
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.background,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.primary,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        periodSelectorBorderColor = MaterialTheme.colorScheme.primary,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.background,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.background,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.primary,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.background,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.background,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Buttons
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(dialogButtonsPadding)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                selectedTime.value = LocalTime.MIN
                                onTimeSelected(selectedTime.value!!)
                                closeSelection()
                            }
                        ) {
                            Text(
                                text = "Clear",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        TextButton(
                            onClick = {
                                selectedTime.value = LocalTime.of(
                                    timePickerState.hour,
                                    timePickerState.minute,
                                    0
                                )
                                onTimeSelected(selectedTime.value!!)
                                closeSelection()
                            }
                        ) {
                            Text(
                                text = "OK",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
