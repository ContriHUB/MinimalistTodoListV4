package com.minimalisttodolist.pleasebethelastrecyclerview.data.model

enum class SortType(val displayName: String) {
    PRIORITY("Priority"),
    DUE_DATE("Time Remaining"),
    ALPHABETICAL("Alphabetical"),
    ALPHABETICAL_REV("Alphabetical z-a");

    companion object {
        fun fromDisplayName(displayName: String): SortType {
            return entries.firstOrNull { it.displayName == displayName } ?: PRIORITY
        }
    }

    fun toDisplayString(): String = displayName
}