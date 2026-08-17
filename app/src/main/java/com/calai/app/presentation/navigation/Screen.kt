package com.calai.app.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object AddMeal : Screen("add_meal")
}
