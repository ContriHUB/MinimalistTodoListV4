package com.minimalisttodolist.pleasebethelastrecyclerview.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ClockType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.DueDateFilterType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.FirstDayOfTheWeekType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.FontFamilyType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.FontWeightType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.RecurrenceType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ReviewStateType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.SortType
import com.minimalisttodolist.pleasebethelastrecyclerview.data.model.ThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "settings"

class AppPreferences private constructor(context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)
    private val dataStore = context.dataStore

    companion object {
        @Volatile
        private var INSTANCE: AppPreferences? = null
        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferences(context).also { INSTANCE = it }
            }
        }
    }

    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val CLOCK_TYPE = stringPreferencesKey("clockType")
        val POST_NOTIFICATION_DENIAL_COUNT = intPreferencesKey("post_notification_denial_count")
        val FONT_FAMILY = stringPreferencesKey("fontFamily")
        val FONT_SIZE = intPreferencesKey("fontSize")
        val FONT_WEIGHT = stringPreferencesKey("fontWeight")
        val SORTING_OPTION = stringPreferencesKey("sorting_option")
        val RECURRENCE_FILTER = stringPreferencesKey("recurrence_option")
        val TUTORIAL_VISIBILITY = booleanPreferencesKey("tutorial_visibility")
        val DUE_DATE_FILTER = stringPreferencesKey("due_date_filter")
        val FIRST_DAY_OF_THE_WEEK = stringPreferencesKey("first_day_of_the_week")
        val REVIEW_STATE = stringPreferencesKey("review_state")
    }

    val theme: Flow<ThemeType> = dataStore.data.map { preferences ->
        ThemeType.fromDisplayName(preferences[PreferencesKeys.THEME] ?: ThemeType.DARK.displayName)
    }

    val clockType: Flow<ClockType> = dataStore.data.map { preferences ->
        ClockType.fromDisplayName(preferences[PreferencesKeys.CLOCK_TYPE] ?: ClockType.TWENTY_FOUR_HOUR.displayName)
    }

    val postNotificationDenialCount: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.POST_NOTIFICATION_DENIAL_COUNT] ?: 0
    }

    val fontFamily: Flow<FontFamilyType> = dataStore.data.map { preferences ->
        FontFamilyType.fromDisplayName(preferences[PreferencesKeys.FONT_FAMILY] ?: FontFamilyType.DEFAULT.displayName)
    }

    val fontSize: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_SIZE] ?: 16
    }

    val fontWeight: Flow<FontWeightType> = dataStore.data.map { preferences ->
        FontWeightType.fromDisplayName(preferences[PreferencesKeys.FONT_WEIGHT] ?: FontWeightType.NORMAL.displayName)
    }

    val priorityOption: Flow<SortType> = dataStore.data.map { preferences ->
        SortType.fromDisplayName(preferences[PreferencesKeys.SORTING_OPTION] ?: SortType.PRIORITY.displayName)
    }

    val recurrenceFilter: Flow<RecurrenceType> = dataStore.data.map { preferences ->
        RecurrenceType.fromDisplayName(preferences[PreferencesKeys.RECURRENCE_FILTER] ?: RecurrenceType.NONE.displayName)
    }

    val tutorialVisibility: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TUTORIAL_VISIBILITY] ?: true
    }

    val dueDateFilterType: Flow<DueDateFilterType> = dataStore.data.map { preferences ->
        DueDateFilterType.fromDisplayName(preferences[PreferencesKeys.DUE_DATE_FILTER] ?: DueDateFilterType.NONE.displayName)
    }

    val firstDayOfTheWeekType: Flow<FirstDayOfTheWeekType> = dataStore.data.map { preferences ->
        FirstDayOfTheWeekType.fromDisplayName(preferences[PreferencesKeys.FIRST_DAY_OF_THE_WEEK] ?: FirstDayOfTheWeekType.MONDAY.displayName)
    }

    val reviewState: Flow<ReviewStateType> = dataStore.data.map { preferences ->
        ReviewStateType.valueOf(preferences[PreferencesKeys.REVIEW_STATE] ?: ReviewStateType.NOT_YET.name)
    }

    suspend fun saveTheme(theme: ThemeType) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.THEME] = theme.displayName }
    }

    suspend fun saveClockType(clockType: ClockType) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.CLOCK_TYPE] = clockType.displayName }
    }

    suspend fun incrementPostNotificationDenialCount() {
        dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.POST_NOTIFICATION_DENIAL_COUNT] ?: 0
            preferences[PreferencesKeys.POST_NOTIFICATION_DENIAL_COUNT] = currentCount + 1
        }
    }

    suspend fun resetPostNotificationDenialCount() {
        dataStore.edit { preferences -> preferences[PreferencesKeys.POST_NOTIFICATION_DENIAL_COUNT] = 0 }
    }

    suspend fun saveFontFamily(fontFamily: FontFamilyType) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.FONT_FAMILY] = fontFamily.displayName }
    }

    suspend fun saveFontSize(fontSize: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.FONT_SIZE] = fontSize }
    }

    suspend fun saveFontWeight(fontWeight: FontWeightType) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.FONT_WEIGHT] = fontWeight.displayName}
    }

    suspend fun savePriority(priority: SortType) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SORTING_OPTION] = priority.displayName }
    }

    suspend fun saveRecurrence(recurrenceFilter: RecurrenceType) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.RECURRENCE_FILTER] = recurrenceFilter.displayName }
    }

    suspend fun disableTutorialDialog() {
        dataStore.edit { preferences -> preferences[PreferencesKeys.TUTORIAL_VISIBILITY] = false }
    }

    suspend fun saveDueDateFilter(dueDateFilterType: DueDateFilterType) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.DUE_DATE_FILTER] = dueDateFilterType.displayName }
    }

    suspend fun saveFirstDayOfTheWeekType(firstDayOfTheWeekType: FirstDayOfTheWeekType) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.FIRST_DAY_OF_THE_WEEK] = firstDayOfTheWeekType.displayName }
    }

    suspend fun updateReviewState(reviewState: ReviewStateType) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.REVIEW_STATE] = reviewState.name }
    }
}