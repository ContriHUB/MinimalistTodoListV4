package com.minimalisttodolist.pleasebethelastrecyclerview.data.model

import androidx.compose.ui.text.font.FontFamily
import com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme.CustomFonts

enum class FontFamilyType(val displayName: String) {
    DEFAULT("Default"),
    SERIF("Serif"),
    SANS_SERIF("SansSerif"),
    MONOSPACE("Monospace"),
    CURSIVE("Cursive");

    companion object {
        fun fromDisplayName(displayName: String): FontFamilyType {
            return entries.firstOrNull { it.displayName == displayName } ?: DEFAULT
        }
    }

    fun toDisplayString(): String = displayName

    fun getFontFamily(): FontFamily {
        return when (this) {
            DEFAULT -> CustomFonts.Default
            SERIF -> CustomFonts.Serif
            SANS_SERIF -> CustomFonts.SansSerif
            MONOSPACE -> CustomFonts.Monospace
            CURSIVE -> CustomFonts.Cursive
        }
    }
}