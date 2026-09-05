package com.calai.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calai.app.data.remote.dto.CreateMealItemDto
import com.calai.app.data.remote.dto.CreateMealRequest
import com.calai.app.data.remote.dto.FoodItemDto
import com.calai.app.domain.repository.CalAIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AddMealUiState(
    val mealType: String = "LUNCH", // BREAKFAST, LUNCH, DINNER, SNACK
    val searchQuery: String = "",
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchResults: List<FoodItemDto> = emptyList(),
    val selectedFoods: List<CreateMealItemDto> = emptyList(),
    val favoriteNames: Set<String> = emptySet(),
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddMealViewModel @Inject constructor(
    private val repository: CalAIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMealUiState())
    val uiState: StateFlow<AddMealUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadFavorites()
        searchFoods("")
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getFoodCategories().onSuccess { cats ->
                _uiState.value = _uiState.value.copy(categories = cats)
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            repository.fetchFavoriteFoods().onSuccess { names ->
                _uiState.value = _uiState.value.copy(favoriteNames = names.toSet())
            }
        }
    }

    fun toggleFavorite(foodName: String) {
        val isCurrentlyFavorite = foodName in _uiState.value.favoriteNames
        val updated = _uiState.value.favoriteNames.toMutableSet()
        if (isCurrentlyFavorite) updated.remove(foodName) else updated.add(foodName)
        _uiState.value = _uiState.value.copy(favoriteNames = updated)

        viewModelScope.launch {
            if (isCurrentlyFavorite) {
                repository.removeFavoriteFood(foodName)
            } else {
                repository.addFavoriteFood(foodName)
            }
        }
    }

    /** Ghi nhận calo/macro nhanh mà không cần chọn từng món từ kho món ăn. */
    fun quickAdd(name: String, calories: Float, protein: Float, carb: Float, fat: Float) {
        if (name.isBlank() || calories <= 0f) {
            _uiState.value = _uiState.value.copy(errorMessage = "Vui lòng nhập tên món và lượng calo hợp lệ")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            repository.quickAddMeal(
                name = name,
                mealType = _uiState.value.mealType,
                date = dateStr,
                calories = calories,
                protein = protein,
                carb = carb,
                fat = fat
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false, isSaveSuccess = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Lỗi khi ghi nhận calo nhanh"
                )
            }
        }
    }

    fun onMealTypeSelect(type: String) {
        _uiState.value = _uiState.value.copy(mealType = type)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchFoods(query, _uiState.value.selectedCategory)
    }

    fun onCategorySelect(category: String?) {
        val newCategory = if (_uiState.value.selectedCategory == category) null else category
        _uiState.value = _uiState.value.copy(selectedCategory = newCategory)
        searchFoods(_uiState.value.searchQuery, newCategory)
    }

    private fun searchFoods(query: String, category: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            val result = repository.searchFoods(
                query = query.ifBlank { null },
                category = category
            )
            result.onSuccess { foods ->
                _uiState.value = _uiState.value.copy(searchResults = foods, isSearching = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun addFoodToMeal(food: FoodItemDto) {
        val currentList = _uiState.value.selectedFoods.toMutableList()
        currentList.add(
            CreateMealItemDto(
                name = food.name,
                servingSize = food.servingSize,
                quantity = 1f,
                calories = food.calories,
                protein = food.protein,
                carb = food.carb,
                fat = food.fat,
                source = "vietnamese_database"
            )
        )
        _uiState.value = _uiState.value.copy(selectedFoods = currentList, errorMessage = null)
    }

    fun removeFoodFromMeal(index: Int) {
        val currentList = _uiState.value.selectedFoods.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _uiState.value = _uiState.value.copy(selectedFoods = currentList)
        }
    }

    fun saveMeal() {
        val state = _uiState.value
        if (state.selectedFoods.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Vui lòng chọn ít nhất 1 món ăn")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            val request = CreateMealRequest(
                mealType = state.mealType,
                date = dateStr,
                items = state.selectedFoods
            )

            val result = repository.createRemoteMeal(request)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false, isSaveSuccess = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Lỗi khi lưu bữa ăn"
                )
            }
        }
    }
}
