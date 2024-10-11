package com.minimalisttodolist.pleasebethelastrecyclerview.data.model

enum class ClockType(val displayName: String) {
    TWELVE_HOUR("12-Hour"),
    TWENTY_FOUR_HOUR("24-Hour");

    companion object {
        fun fromDisplayName(displayName: String): ClockType {
            return entries.firstOrNull { it.displayName == displayName } ?: TWELVE_HOUR
        }
    }

    fun toDisplayString(): String = displayName
}