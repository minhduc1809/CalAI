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

        private const val KG_TO_LB = 2.20462f

        // Giờ nhắc mặc định — nguồn duy nhất, dùng cả trong class này lẫn ở ProfileViewModel (ReminderSettingsState).
        const val DEFAULT_BREAKFAST_TIME = "07:30"
        const val DEFAULT_LUNCH_TIME = "12:00"
        const val DEFAULT_DINNER_TIME = "19:00"
        const val DEFAULT_SNACK_TIME = "15:30"
        const val DEFAULT_WATER_INTERVAL_HOURS = 2

        /** Nguồn duy nhất cho công thức đổi kg↔lb — dùng ở cả ViewModel (có instance) lẫn Composable (chỉ có chuỗi weightUnit từ UiState). */
        fun convertKg(kg: Float, unit: String): Float = if (unit == "lb") kg * KG_TO_LB else kg

        /** Chiều ngược lại: giá trị người dùng nhập theo `unit` → kg để lưu (backend luôn lưu kg). */
        fun convertToKg(value: Float, unit: String): Float = if (unit == "lb") value / KG_TO_LB else value

        fun formatWeight(kg: Float?, unit: String): String {
            if (kg == null || kg <= 0f) return "--"
            return String.format(java.util.Locale.US, "%.1f %s", convertKg(kg, unit), if (unit == "lb") "lbs" else "kg")
        }

        fun formatWeightValueOnly(kg: Float?, unit: String): String {
            if (kg == null || kg <= 0f) return "--"
            return String.format(java.util.Locale.US, "%.1f", convertKg(kg, unit))
        }
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
    fun getBreakfastReminderTime(): String = prefs.getString(KEY_REMINDER_BREAKFAST_TIME, DEFAULT_BREAKFAST_TIME) ?: DEFAULT_BREAKFAST_TIME
    fun setBreakfastReminder(enabled: Boolean, time: String = DEFAULT_BREAKFAST_TIME) {
        prefs.edit()
            .putBoolean(KEY_REMINDER_BREAKFAST, enabled)
            .putString(KEY_REMINDER_BREAKFAST_TIME, time)
            .apply()
    }

    fun isLunchReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_LUNCH, true)
    fun getLunchReminderTime(): String = prefs.getString(KEY_REMINDER_LUNCH_TIME, DEFAULT_LUNCH_TIME) ?: DEFAULT_LUNCH_TIME
    fun setLunchReminder(enabled: Boolean, time: String = DEFAULT_LUNCH_TIME) {
        prefs.edit()
            .putBoolean(KEY_REMINDER_LUNCH, enabled)
            .putString(KEY_REMINDER_LUNCH_TIME, time)
            .apply()
    }

    fun isDinnerReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_DINNER, true)
    fun getDinnerReminderTime(): String = prefs.getString(KEY_REMINDER_DINNER_TIME, DEFAULT_DINNER_TIME) ?: DEFAULT_DINNER_TIME
    fun setDinnerReminder(enabled: Boolean, time: String = DEFAULT_DINNER_TIME) {
        prefs.edit()
            .putBoolean(KEY_REMINDER_DINNER, enabled)
            .putString(KEY_REMINDER_DINNER_TIME, time)
            .apply()
    }

    fun isSnackReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_SNACK, false)
    fun getSnackReminderTime(): String = prefs.getString(KEY_REMINDER_SNACK_TIME, DEFAULT_SNACK_TIME) ?: DEFAULT_SNACK_TIME
    fun setSnackReminder(enabled: Boolean, time: String = DEFAULT_SNACK_TIME) {
        prefs.edit()
            .putBoolean(KEY_REMINDER_SNACK, enabled)
            .putString(KEY_REMINDER_SNACK_TIME, time)
            .apply()
    }

    fun isWaterReminderEnabled(): Boolean = prefs.getBoolean(KEY_REMINDER_WATER, true)
    fun getWaterReminderInterval(): Int = prefs.getInt(KEY_REMINDER_WATER_INTERVAL, DEFAULT_WATER_INTERVAL_HOURS)
    fun setWaterReminder(enabled: Boolean, intervalHours: Int = DEFAULT_WATER_INTERVAL_HOURS) {
        prefs.edit()
            .putBoolean(KEY_REMINDER_WATER, enabled)
            .putInt(KEY_REMINDER_WATER_INTERVAL, intervalHours)
            .apply()
    }

    private val _weightUnit = MutableStateFlow(
        prefs.getString(KEY_UNIT_WEIGHT, "kg") ?: "kg"
    )
    val weightUnit: StateFlow<String> = _weightUnit.asStateFlow()

    // Units
    fun getWeightUnit(): String = _weightUnit.value
    fun setWeightUnit(unit: String) {
        prefs.edit().putString(KEY_UNIT_WEIGHT, unit).apply()
        _weightUnit.value = unit
    }

    fun convertWeightFromKg(kg: Float): Float = convertKg(kg, _weightUnit.value)
    fun formatWeight(kg: Float?): String = formatWeight(kg, _weightUnit.value)
    fun formatWeightValueOnly(kg: Float?): String = formatWeightValueOnly(kg, _weightUnit.value)

    fun getHeightUnit(): String = prefs.getString(KEY_UNIT_HEIGHT, "cm") ?: "cm"
    fun setHeightUnit(unit: String) = prefs.edit().putString(KEY_UNIT_HEIGHT, unit).apply()

    fun getEnergyUnit(): String = prefs.getString(KEY_UNIT_ENERGY, "kcal") ?: "kcal"
    fun setEnergyUnit(unit: String) = prefs.edit().putString(KEY_UNIT_ENERGY, unit).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
