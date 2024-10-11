package com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.minimalisttodolist.pleasebethelastrecyclerview.util.AnalyticsEvents
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.DueDateFilterType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.RecurrenceType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.SortType
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.CustomBox
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.FilterSelector
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.AppEvent
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.DataStoreViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskEvent
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuDialog(
    taskState: TaskState,
    onEvent: (TaskEvent) -> Unit,
    modifier: Modifier = Modifier,
    dataStoreViewModel: DataStoreViewModel,
    onAppEvent: (AppEvent) -> Unit
) {
    val context = LocalContext.current
    BasicAlertDialog(
        onDismissRequest = {
            onAppEvent(AppEvent.HideMenuDialog)
        },
        modifier = modifier
            .width(350.dp)
    ) {
        CustomBox {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                MenuTitle( modifier = Modifier.align(Alignment.CenterHorizontally) )
                Personalize ( onClick = {
                    onAppEvent(AppEvent.ShowPersonalizeDialog)
                })
                RecurrenceSelector(
                    onRecurrenceFilterChange = {
                        onEvent(TaskEvent.SetRecurrenceFilter(it))
                        Firebase.analytics.logEvent(AnalyticsEvents.REPEAT_FILTER_CLICKED){
                            param("screen", "MenuDialog")
                        } },
                    dataStoreViewModel = dataStoreViewModel
                )
                PrioritySelector(
                    onSortChange = { onEvent(TaskEvent.SortTasks(it) ) },
                    dataStoreViewModel = dataStoreViewModel
                )
                DueDateFilterSelector(
                    onDueDateFilterChange = {
                        onEvent(TaskEvent.SetDueDateFilter(it))
                        Firebase.analytics.logEvent(AnalyticsEvents.DUE_FILTER_CHANGE){
                            param("screen", "MenuDialog")
                        } },
                    dataStoreViewModel = dataStoreViewModel
                )
                History( onClick = { onAppEvent(AppEvent.ShowHistoryDialog) } )
            }
        }
    }
}

@Composable
fun MenuTitle(modifier: Modifier = Modifier) {
    Text(
        text = "Menu",
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier
            .padding(bottom = 12.dp)
    )
}

@Composable
fun Personalize(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row (
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(top = 16.dp, bottom = 16.dp)
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Personalize",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 10.dp)
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "History Chevron",
            tint = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
fun RecurrenceSelector(
    onRecurrenceFilterChange: (RecurrenceType) -> Unit,
    dataStoreViewModel: DataStoreViewModel
) {
    FilterSelector(
        title = "Repeat Filter",
        onFilterChange = onRecurrenceFilterChange,
        dataStoreViewModel = dataStoreViewModel,
        filterOptions = RecurrenceType.entriesWithNONE.toList(),
        getDisplayString = { it.toDisplayString() },
        saveFilter = { vm, filter -> vm.saveRecurrenceFilter(filter) },
        collectFilter = { it.recurrenceFilter },
        initialValue = RecurrenceType.NONE
    )
}

@Composable
fun PrioritySelector(
    onSortChange: (SortType) -> Unit,
    dataStoreViewModel: DataStoreViewModel
) {
    FilterSelector(
        title = "Sort By",
        onFilterChange = onSortChange,
        dataStoreViewModel = dataStoreViewModel,
        filterOptions = SortType.entries.toList(),
        getDisplayString = { it.toDisplayString() },
        saveFilter = { vm, filter -> vm.savePriorityOption(filter) },
        collectFilter = { it.priorityOption },
        initialValue = SortType.PRIORITY
    )
}

@Composable
fun DueDateFilterSelector(
    onDueDateFilterChange: (DueDateFilterType) -> Unit,
    dataStoreViewModel: DataStoreViewModel
) {
    FilterSelector(
        title = "Due Filter",
        onFilterChange = onDueDateFilterChange,
        dataStoreViewModel = dataStoreViewModel,
        filterOptions = DueDateFilterType.entries.toList(),
        getDisplayString = { it.toDisplayString() },
        saveFilter = { vm, filter -> vm.saveDueDateFilter(filter) },
        collectFilter = { it.dueDateFilter },
        initialValue = DueDateFilterType.NONE
    )
}

@Composable
fun History(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row (
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(top = 16.dp, bottom = 16.dp)
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 10.dp)
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "History Chevron",
            tint = MaterialTheme.colorScheme.tertiary,
        )
    }
}
