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
        private const val KEY_UNIT_WEIGHT = "pref_unit_weight" // "kg", "lb"
        private const val KEY_UNIT_HEIGHT = "pref_unit_height" // "cm", "ft"
        private const val KEY_UNIT_ENERGY = "pref_unit_energy" // "kcal", "kJ"
        private const val KEY_MEAL_STRUCTURE_MODE = "pref_meal_structure_mode" // "TIMELINE", "FIXED_MEALS"

        private const val KG_TO_LB = 2.20462f

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

    // Food Log Display — BRD: "meal_structure_mode (timeline / fixed_meals)", mặc định Timeline,
    // chỉ đổi cách nhóm hiển thị, không ảnh hưởng dữ liệu/tính toán calo-macro đã lưu.
    private val _mealStructureMode = MutableStateFlow(
        prefs.getString(KEY_MEAL_STRUCTURE_MODE, "TIMELINE") ?: "TIMELINE"
    )
    val mealStructureMode: StateFlow<String> = _mealStructureMode.asStateFlow()

    fun getMealStructureMode(): String = _mealStructureMode.value
    fun setMealStructureMode(mode: String) {
        prefs.edit().putString(KEY_MEAL_STRUCTURE_MODE, mode).apply()
        _mealStructureMode.value = mode
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
