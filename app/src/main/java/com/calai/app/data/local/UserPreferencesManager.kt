package com.calai.app.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("calai_user_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode" // "DARK", "LIGHT", "SYSTEM"
        private const val KEY_REMINDER_BREAKFAST = "pref_reminder_breakfast"
        private const val KEY_REMINDER_BREAKFAST_TIME = "pref_reminder_breakfast_time"
        private const val KEY_REMINDER_LUNCH = "pref_reminder_lunch"
        private const val KEY_REMINDER_LUNCH_TIME = "pref_reminder_lunch_time"
        private const val KEY_REMINDER_DINNER = "pref_reminder_dinner"
        private const val KEY_REMINDER_DINNER_TIME = "pref_reminder_dinner_time"
        private const val KEY_REMINDER_SNACK = "pref_reminder_snack"
        private const val KEY_REMINDER_SNACK_TIME = "pref_reminder_snack_time"
        private const val KEY_REMINDER_WATER = "pref_reminder_water"
        private const val KEY_REMINDER_WATER_INTERVAL = "pref_reminder_water_interval"
        private const val KEY_UNIT_WEIGHT = "pref_unit_weight" // "kg", "lb"
        private const val KEY_UNIT_HEIGHT = "pref_unit_height" // "cm", "ft"
        private const val KEY_UNIT_ENERGY = "pref_unit_energy" // "kcal", "kJ"
    }

    private val _isDarkTheme = MutableStateFlow(
        prefs.getString(KEY_THEME_MODE, "DARK") != "LIGHT"
    )
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _themeMode = MutableStateFlow(
        prefs.getString(KEY_THEME_MODE, "DARK") ?: "DARK"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
        _isDarkTheme.value = (mode != "LIGHT")
    }

    fun toggleTheme(isDark: Boolean) {
        val mode = if (isDark) "DARK" else "LIGHT"
        setThemeMode(mode)
    }

    // Reminders
    fun isBreakfastReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_BREAKFAST, true)
    fun getBreakfastReminderTime(): String = prefs.getString(KEY_REMINDER_BREAKFAST_TIME, "07:30") ?: "07:30"
    fun setBreakfastReminder(enabled: Boolean, time: String = "07:30") {
        prefs.edit()
            .putBoolean(KEY_REMINDER_BREAKFAST, enabled)
            .putString(KEY_REMINDER_BREAKFAST_TIME, time)
            .apply()
    }

    fun isLunchReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_LUNCH, true)
    fun getLunchReminderTime(): String = prefs.getString(KEY_REMINDER_LUNCH_TIME, "12:00") ?: "12:00"
    fun setLunchReminder(enabled: Boolean, time: String = "12:00") {
        prefs.edit()
            .putBoolean(KEY_REMINDER_LUNCH, enabled)
            .putString(KEY_REMINDER_LUNCH_TIME, time)
            .apply()
    }

    fun isDinnerReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_DINNER, true)
    fun getDinnerReminderTime(): String = prefs.getString(KEY_REMINDER_DINNER_TIME, "19:00") ?: "19:00"
    fun setDinnerReminder(enabled: Boolean, time: String = "19:00") {
        prefs.edit()
            .putBoolean(KEY_REMINDER_DINNER, enabled)
            .putString(KEY_REMINDER_DINNER_TIME, time)
            .apply()
    }

    fun isSnackReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_SNACK, false)
    fun getSnackReminderTime(): String = prefs.getString(KEY_REMINDER_SNACK_TIME, "15:30") ?: "15:30"
    fun setSnackReminder(enabled: Boolean, time: String = "15:30") {
        prefs.edit()
            .putBoolean(KEY_REMINDER_SNACK, enabled)
            .putString(KEY_REMINDER_SNACK_TIME, time)
            .apply()
    }

    fun isWaterReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_WATER, true)
    fun getWaterReminderInterval(): Int = prefs.getInt(KEY_REMINDER_WATER_INTERVAL, 2)
    fun setWaterReminder(enabled: Boolean, intervalHours: Int = 2) {
        prefs.edit()
            .putBoolean(KEY_REMINDER_WATER, enabled)
            .putInt(KEY_REMINDER_WATER_INTERVAL, intervalHours)
            .apply()
    }

    // Units
    fun getWeightUnit(): String = prefs.getString(KEY_UNIT_WEIGHT, "kg") ?: "kg"
    fun setWeightUnit(unit: String) = prefs.edit().putString(KEY_UNIT_WEIGHT, unit).apply()

    fun getHeightUnit(): String = prefs.getString(KEY_UNIT_HEIGHT, "cm") ?: "cm"
    fun setHeightUnit(unit: String) = prefs.edit().putString(KEY_UNIT_HEIGHT, unit).apply()

    fun getEnergyUnit(): String = prefs.getString(KEY_UNIT_ENERGY, "kcal") ?: "kcal"
    fun setEnergyUnit(unit: String) = prefs.edit().putString(KEY_UNIT_ENERGY, unit).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
