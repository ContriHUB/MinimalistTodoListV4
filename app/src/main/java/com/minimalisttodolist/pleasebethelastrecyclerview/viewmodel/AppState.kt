package com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel

data class AppState(
    val isMenuDialogVisible: Boolean = false,
    val isHistoryDialogVisible: Boolean = false,
    val isScheduleExactAlarmPermissionDialogVisible: Boolean = false,
    val isFontSettingsDialogVisible: Boolean = false,
    val isTutorialDialogVisible: Boolean = false,
    val isPersonalizeDialogVisible: Boolean = false,
)
