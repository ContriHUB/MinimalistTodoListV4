package com.minimalisttodolist.pleasebethelastrecyclerview.ui.dialogs

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ClockType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.FontFamilyType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.FontWeightType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ThemeType
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.CompleteIconWithoutDelay
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.CustomBox
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.components.FilterSelector
import com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel.DataStoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Theme_FontSettingsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    dataStoreViewModel: DataStoreViewModel
) {
    BasicAlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        modifier = modifier
            .width(350.dp)
    ) {
        CustomBox {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Theme_FontOptionsTitle(onBack = onBack)
                ThemeSelector(
                    dataStoreViewModel = dataStoreViewModel,
                    onThemeTypeChange = { dataStoreViewModel.saveTheme(it) }
                )
                FontFamilySelector(
                    dataStoreViewModel = dataStoreViewModel,
                    onFontFamilyChange = { dataStoreViewModel.saveFontFamily(it) }
                )
                FontWeightSelector(
                    dataStoreViewModel = dataStoreViewModel,
                    onFontWeightChange = { dataStoreViewModel.saveFontWeight(it) }
                )
                FontSizeSelector(dataStoreViewModel = dataStoreViewModel)
            }
        }
    }
}

@Composable
fun Theme_FontOptionsTitle(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Box (
        modifier = Modifier
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
            text = "Theme & Font",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun ThemeSelector(
    onThemeTypeChange: (ThemeType) -> Unit,
    dataStoreViewModel: DataStoreViewModel
) {
    FilterSelector(
        title = "Theme",
        onFilterChange = onThemeTypeChange,
        dataStoreViewModel = dataStoreViewModel,
        filterOptions = ThemeType.entries.toList(),
        getDisplayString = { it.toDisplayString() },
        saveFilter = { vm, filter -> vm.saveTheme(filter) },
        collectFilter = { it.theme },
        initialValue = ThemeType.DARK
    )
}

@Composable
fun FontFamilySelector(
    onFontFamilyChange: (FontFamilyType) -> Unit,
    dataStoreViewModel: DataStoreViewModel
) {
    FilterSelector(
        title = "Font Family",
        onFilterChange = onFontFamilyChange,
        dataStoreViewModel = dataStoreViewModel,
        filterOptions = FontFamilyType.entries.toList(),
        getDisplayString = { it.toDisplayString() },
        saveFilter = { vm, filter -> vm.saveFontFamily(filter) },
        collectFilter = { it.fontFamily },
        initialValue = FontFamilyType.DEFAULT,
        customDisplayContent = { option ->
            Text(
                text = option.toDisplayString(),
                fontFamily = option.getFontFamily()
            )
        }
    )
}

@Composable
fun FontWeightSelector(
    onFontWeightChange: (FontWeightType) -> Unit,
    dataStoreViewModel: DataStoreViewModel
) {
    FilterSelector(
        title = "Font Weight",
        onFilterChange = onFontWeightChange,
        dataStoreViewModel = dataStoreViewModel,
        filterOptions = FontWeightType.entries.toList(),
        getDisplayString = { it.toDisplayString() },
        saveFilter = { vm, filter -> vm.saveFontWeight(filter) },
        collectFilter = { it.fontWeight },
        initialValue = FontWeightType.NORMAL,
        customDisplayContent = { option ->
            Text(
                text = option.toDisplayString(),
                fontWeight = option.getFontWeight()
            )
        }
    )
}

@Composable
fun FontSizeSelector(
    modifier: Modifier = Modifier,
    dataStoreViewModel: DataStoreViewModel
) {
    val fontSize by dataStoreViewModel.fontSize.collectAsState()

    Text(
        "Select Font Size: $fontSize",
        fontSize = 16.sp,
        modifier = Modifier
            .padding(top = 12.dp)
    )
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Text(
            "A",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.weight(1f)
        )
        Slider(
            value = fontSize.toFloat(),
            onValueChange = { newSize ->
                dataStoreViewModel.saveFontSize(newSize.toInt())
            },
            valueRange = 10f..22f,
            steps = 20,
            modifier = Modifier.weight(1f)
        )
        Text(
            "A",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}


fun FontWeight.toDisplayString(): String {
    return when (this) {
        FontWeight.Thin -> "Thin"
        FontWeight.ExtraLight -> "Extra Light"
        FontWeight.Light -> "Light"
        FontWeight.Normal -> "Normal"
        FontWeight.Medium -> "Medium"
        FontWeight.SemiBold -> "Semi Bold"
        FontWeight.Bold -> "Bold"
        FontWeight.ExtraBold -> "Extra Bold"
        FontWeight.Black -> "Black"
        else -> "Normal"
    }
}

fun fontWeightFromDisplayName(displayName: String): FontWeight {
    return when (displayName) {
        "Light" -> FontWeight.Light
        "Thin" -> FontWeight.Thin
        "Normal" -> FontWeight.Normal
        "Medium" -> FontWeight.Medium
        "Bold" -> FontWeight.Bold
        "Black" -> FontWeight.Black
        else -> FontWeight.Normal
    }
}
