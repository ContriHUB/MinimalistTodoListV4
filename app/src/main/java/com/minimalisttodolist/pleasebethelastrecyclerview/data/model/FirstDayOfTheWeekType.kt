package com.minimalisttodolist.pleasebethelastrecyclerview.data.model

enum class FirstDayOfTheWeekType(val displayName: String) {
    MONDAY("Monday"),
    SUNDAY("Sunday"),
    SATURDAY("Saturday");

    companion object {
        fun fromDisplayName(displayName: String): FirstDayOfTheWeekType {
            return entries.firstOrNull { it.displayName == displayName } ?: MONDAY
        }
    }

    fun toDisplayString(): String = displayName
}
