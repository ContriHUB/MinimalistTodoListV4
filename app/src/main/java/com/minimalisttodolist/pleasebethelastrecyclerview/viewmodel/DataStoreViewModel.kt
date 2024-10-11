package com.minimalisttodolist.pleasebethelastrecyclerview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ClockType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.DueDateFilterType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.FirstDayOfTheWeekType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.FontFamilyType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.FontWeightType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.RecurrenceType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ReviewStateType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.SortType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.preferences.AppPreferences
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ThemeType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DataStoreViewModel(private val appPreferences: AppPreferences) : ViewModel() {
    val theme: StateFlow<ThemeType> = appPreferences.theme
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeType.DARK)

    val clockType: StateFlow<ClockType> = appPreferences.clockType
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ClockType.TWENTY_FOUR_HOUR)

    val fontFamily: StateFlow<FontFamilyType> = appPreferences.fontFamily
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, FontFamilyType.DEFAULT)

    val fontSize: StateFlow<Int> = appPreferences.fontSize
        .stateIn(viewModelScope, SharingStarted.Eagerly, 16)

    val fontWeight: StateFlow<FontWeightType> = appPreferences.fontWeight
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, FontWeightType.NORMAL)

    val priorityOption: StateFlow<SortType> = appPreferences.priorityOption
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SortType.PRIORITY)

    val recurrenceFilter: StateFlow<RecurrenceType> = appPreferences.recurrenceFilter
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RecurrenceType.NONE)

    val tutorialVisibility: StateFlow<Boolean> = appPreferences.tutorialVisibility
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val dueDateFilter: StateFlow<DueDateFilterType> = appPreferences.dueDateFilterType
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DueDateFilterType.NONE)

    val firstDayOfTheWeekType: StateFlow<FirstDayOfTheWeekType> = appPreferences.firstDayOfTheWeekType
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, FirstDayOfTheWeekType.MONDAY)

    val reviewStateType: StateFlow<ReviewStateType> = appPreferences.reviewState
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReviewStateType.NOT_YET)

    fun saveTheme(themeType: ThemeType) {
        viewModelScope.launch { appPreferences.saveTheme(themeType) }
    }

    fun saveClockType(clockType: ClockType) {
        viewModelScope.launch { appPreferences.saveClockType(clockType) }
    }

    fun saveFontFamily(fontFamilyType: FontFamilyType) {
        viewModelScope.launch { appPreferences.saveFontFamily(fontFamilyType) }
    }

    fun saveFontSize(fontSize: Int) {
        viewModelScope.launch { appPreferences.saveFontSize(fontSize) }
    }

    fun saveFontWeight(fontWeightType: FontWeightType) {
        viewModelScope.launch { appPreferences.saveFontWeight(fontWeightType) }
    }

    fun savePriorityOption(priority: SortType) {
        viewModelScope.launch { appPreferences.savePriority(priority)}
    }

    fun saveRecurrenceFilter(recurrenceFilter: RecurrenceType) {
        viewModelScope.launch { appPreferences.saveRecurrence(recurrenceFilter) }
    }

    fun saveTutorialVisibility(tutorialVisibility: Boolean) {
        viewModelScope.launch { appPreferences.disableTutorialDialog() }
    }

    fun saveDueDateFilter(dueDateFilterType: DueDateFilterType) {
        viewModelScope.launch { appPreferences.saveDueDateFilter(dueDateFilterType) }
    }

    fun saveFirstDayOfTheWeek(firstDayOfTheWeekType: FirstDayOfTheWeekType) {
        viewModelScope.launch { appPreferences.saveFirstDayOfTheWeekType(firstDayOfTheWeekType) }
    }

    fun updateReviewState(reviewState: ReviewStateType) {
        viewModelScope.launch { appPreferences.updateReviewState(reviewState) }
    }
}


class PreferencesViewModelFactory(private val appPreferences: AppPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DataStoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DataStoreViewModel(appPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
