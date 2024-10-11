package com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs

import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.minimalisttodolist.pleasebethelastrecyclerview.R
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ClockType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.FirstDayOfTheWeekType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.RecurrenceType
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.CompleteIconWithoutDelay
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.CustomBox
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.CustomDropdownMenu
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.FilterSelector
import com.minimalisttodolist.pleasebethelastrecyclerview.util.darkIcon
import com.minimalisttodolist.pleasebethelastrecyclerview.util.lightIcon
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.AppEvent
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.DataStoreViewModel
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.TaskEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizeDialog(
    modifier: Modifier = Modifier,
    onAppEvent: (AppEvent) -> Unit,
    onTaskEvent: (TaskEvent) -> Unit,
    onBack: () -> Unit,
    dataStoreViewModel: DataStoreViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BasicAlertDialog(
        onDismissRequest = {
            onAppEvent(AppEvent.HidePersonalizeDialog)
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
                PersonalizeTitle( onBack = onBack )
                Theme_Font(onClick = {
                    onAppEvent(AppEvent.HidePersonalizeDialog)
                    onAppEvent(AppEvent.ShowFontSettingsDialog)
                })
                ClockTypeSelector(
                    dataStoreViewModel = dataStoreViewModel,
                    onClockTypeChange = { dataStoreViewModel.saveClockType(it) }
                )
                FirstDayOfTheWeekSelector(
                    onFirstDayOfTheWeekTypeChange = {
                        coroutineScope.launch {
                            dataStoreViewModel.saveFirstDayOfTheWeek(it)
                            delay(500)
                            onTaskEvent(TaskEvent.RefreshTasks)
                        }
                    },
                    dataStoreViewModel = dataStoreViewModel
                )
                AppIconSelector( context = context )
            }
        }
    }
}

@Composable
fun PersonalizeTitle(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Box (
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(top = 12.dp, bottom = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.ArrowBack,
            contentDescription = "back arrow",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .padding(end = 12.dp)
                .clickable {
                    onBack()
                }
        )
        Text(
            text = "Personalize",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun Theme_Font(modifier: Modifier = Modifier, onClick: () -> Unit) {
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
            text = "Theme & Font",
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
fun ClockTypeSelector(
    onClockTypeChange: (ClockType) -> Unit,
    dataStoreViewModel: DataStoreViewModel
) {
    FilterSelector(
        title = "Clock Type",
        onFilterChange = onClockTypeChange,
        dataStoreViewModel = dataStoreViewModel,
        filterOptions = ClockType.entries.toList(),
        getDisplayString = { it.toDisplayString() },
        saveFilter = { vm, filter -> vm.saveClockType(filter) },
        collectFilter = { it.clockType },
        initialValue = ClockType.TWENTY_FOUR_HOUR
    )
}

@Composable
fun FirstDayOfTheWeekSelector(
    onFirstDayOfTheWeekTypeChange: (FirstDayOfTheWeekType) -> Unit,
    dataStoreViewModel: DataStoreViewModel
) {
    FilterSelector(
        title = "First Day of Week",
        onFilterChange = onFirstDayOfTheWeekTypeChange,
        dataStoreViewModel = dataStoreViewModel,
        filterOptions = FirstDayOfTheWeekType.entries.toList(),
        getDisplayString = { it.toDisplayString() },
        saveFilter = { vm, filter -> vm.saveFirstDayOfTheWeek(filter) },
        collectFilter = { it.firstDayOfTheWeekType },
        initialValue = FirstDayOfTheWeekType.MONDAY
    )
}

@Composable
fun AppIconSelector(modifier: Modifier = Modifier, context: Context) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                expanded = !expanded
            }
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "App Icon",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 10.dp)
        )
    }
    CustomDropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        },
    ) {
        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconPreview(isLightIcon = true, onClick = {
                lightIcon(context)
            })
            IconPreview(isLightIcon = false, onClick = {
                darkIcon(context)
            })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconPreview(isLightIcon: Boolean, onClick: () -> Unit) {
    val iconResId = if (isLightIcon) {
        R.drawable.logo_light
    } else {
        R.drawable.logo_dark
    }
    val backgroundColor = if (isLightIcon) {
        Color.White
    } else {
        Color.Black
    }
    val borderColor = if (isLightIcon) {
        Color.Black
    } else {
        Color.White
    }
    val scale = remember { Animatable(initialValue = 1f) }
    var selected by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = selected) {
        if(selected) {
            launch {
                scale.animateTo(
                    targetValue = 0.9f,
                    animationSpec = tween(
                        durationMillis = 50
                    )
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                selected = false
            }
        }
    }

    Box(
        modifier = Modifier
            .scale(scale.value)
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(16.dp))
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    selected = !selected
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
    }
}
