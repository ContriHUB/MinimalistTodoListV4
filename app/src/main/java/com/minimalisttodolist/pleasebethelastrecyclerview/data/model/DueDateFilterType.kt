package com.minimalisttodolist.pleasebethelastrecyclerview.data.model

enum class DueDateFilterType (val displayName: String) {
    NONE("Any Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year");

    companion object {
//        val entries = entries.toList()

        fun fromDisplayName(displayName: String): DueDateFilterType {
            return entries.firstOrNull { it.displayName == displayName } ?: NONE
        }
    }

    fun toDisplayString(): String = displayName
}
