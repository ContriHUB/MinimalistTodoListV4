package com.minimalisttodolist.pleasebethelastrecyclerview.data.model

import androidx.compose.ui.text.font.FontWeight

enum class FontWeightType(val displayName: String) {
    LIGHT("Light"),
    NORMAL("Normal"),
    BOLD("Bold");

    companion object {
        fun fromDisplayName(displayName: String): FontWeightType {
            return entries.firstOrNull { it.displayName == displayName } ?: NORMAL
        }
    }

    fun toDisplayString(): String = displayName

    fun getFontWeight(): FontWeight {
        return when (this) {
            LIGHT -> FontWeight.Light
            NORMAL -> FontWeight.Normal
            BOLD -> FontWeight.Bold
        }
    }
}
