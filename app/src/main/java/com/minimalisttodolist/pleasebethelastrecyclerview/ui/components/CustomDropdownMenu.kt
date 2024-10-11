package com.minimalisttodolist.pleasebethelastrecyclerview.ui.components

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.DataStoreViewModel
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : Enum<T>> FilterSelector(
    title: String,
    onFilterChange: (T) -> Unit,
    dataStoreViewModel: DataStoreViewModel,
    filterOptions: List<T>,
    getDisplayString: (T) -> String,
    saveFilter: (DataStoreViewModel, T) -> Unit,
    collectFilter: (DataStoreViewModel) -> Flow<T>,
    initialValue: T,
    customDisplayContent: (@Composable (T) -> Unit)? = null
) {
    val filter by collectFilter(dataStoreViewModel).collectAsState(initial = initialValue)
    var expanded by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(DpOffset.Zero) }
    var itemHeight by remember { mutableStateOf(0.dp) }
    var secondTextOffset by remember { mutableStateOf(0.dp) }
    var secondTextWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { with(density) { itemHeight = it.height.toDp() } }
            .pointerInput(true) {
                detectTapGestures(
                    onPress = { offset ->
                        expanded = true
                        pressOffset = DpOffset(offset.x.toDp(), offset.y.toDp())
                    }
                )
            }
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 10.dp)
        )
        Text(
            text = getDisplayString(filter),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
            fontStyle = FontStyle.Italic,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    secondTextOffset = with(density) { coordinates.positionInRoot().x.toDp() }
                    secondTextWidth = with(density) { coordinates.size.width.toDp() }
                }
        )
    }

    val dropdownOffset = DpOffset(
        x = secondTextOffset + secondTextWidth,
//        y = pressOffset.y - itemHeight
        y = 0.dp
    )

    CustomDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        pressOffset = dropdownOffset
    ) {
        filterOptions.forEach { filterOption ->
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 12.dp, end = 25.dp)
                    .combinedClickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            saveFilter(dataStoreViewModel, filterOption)
                            onFilterChange(filterOption)
                            expanded = false
                        }
                    )
            ) {
                CompleteIconWithoutDelay(isChecked = filter == filterOption)
                if (customDisplayContent != null) {
                    customDisplayContent(filterOption)
                } else {
                    Text(
                        text = getDisplayString(filterOption),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
fun CustomDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    pressOffset: DpOffset = DpOffset.Zero,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var widthDp by remember { mutableStateOf(0.dp) }

    Box {
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme,
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        widthDp = with(density) { coordinates.size.width.toDp() }
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(16.dp)
                    ),
                offset = pressOffset.copy(
                    x = pressOffset.x - widthDp - 8.dp,
//                    y = 0.dp
                )
            ) {
                content()
            }
        }
    }
}