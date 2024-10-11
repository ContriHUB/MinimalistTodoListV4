package com.minimalisttodolist.pleasebethelastrecyclerview.data.model

import androidx.compose.ui.graphics.Color
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.DarkPriority0
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.DarkPriority1
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.DarkPriority2
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.DarkPriority3
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.LightPriority0
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.LightPriority1
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.LightPriority2
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.LightPriority3

enum class PriorityColor(private val darkColor: Color, private val lightColor: Color) {
    PRIORITY3(DarkPriority3, LightPriority3),
    PRIORITY2(DarkPriority2, LightPriority2),
    PRIORITY1(DarkPriority1, LightPriority1),
    PRIORITY0(DarkPriority0, LightPriority0);

    fun getColor(darkTheme: Boolean): Color {
        return if (darkTheme) darkColor else lightColor
    }
}
