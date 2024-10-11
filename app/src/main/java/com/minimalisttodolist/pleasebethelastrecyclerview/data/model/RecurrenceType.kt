package com.minimalisttodolist.pleasebethelastrecyclerview.data.model

enum class RecurrenceType(val displayName: String) {
    NONE("All Tasks"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    companion object {
        val entriesWithoutNONE = listOf(DAILY, WEEKLY, MONTHLY, YEARLY)
        val entriesWithNONE = entries.toList()

        fun fromDisplayName(displayName: String): RecurrenceType {
            return entries.firstOrNull { it.displayName == displayName } ?: NONE
        }
    }

    fun toDisplayString(): String = displayName
}