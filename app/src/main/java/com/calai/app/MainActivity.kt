package com.calai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calai.app.presentation.navigation.Screen
import com.calai.app.presentation.screens.AddMealScreen
import com.calai.app.presentation.screens.HomeScreen
import com.calai.app.presentation.screens.LoginScreen
import com.calai.app.presentation.theme.CalAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(onLoginSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onAddMealClick = {
                                    navController.navigate(Screen.AddMeal.route)
                                },
                                onCameraClick = {
                                    navController.navigate(Screen.CameraScan.route)
                                },
                                onLogout = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.AddMeal.route) {
                            AddMealScreen(onBack = {
                                navController.popBackStack()
                            })
                        }
                        composable(Screen.CameraScan.route) {
                            com.calai.app.presentation.screens.CameraScanScreen(onBack = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }
}
