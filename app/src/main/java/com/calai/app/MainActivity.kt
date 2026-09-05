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
import com.calai.app.presentation.components.DockTab
import com.calai.app.presentation.navigation.Screen
import com.calai.app.presentation.screens.*
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

                    fun navigateToTab(tab: DockTab) {
                        val targetRoute = when (tab) {
                            DockTab.HOME -> Screen.Home.route
                            DockTab.STATISTICS -> Screen.Statistics.route
                            DockTab.SCAN -> Screen.CameraScan.route
                            DockTab.CHAT -> Screen.Chat.route
                            DockTab.PROFILE -> Screen.Profile.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route
                    ) {
                        // 1. Màn hình Đăng nhập / Đăng ký
                        composable(Screen.Login.route) {
                            LoginScreen(onLoginSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            })
                        }

                        // 2. Màn hình Trang Chủ (Home Bento)
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onAddMealClick = {
                                    navController.navigate(Screen.AddMeal.route)
                                },
                                onCameraClick = {
                                    navController.navigate(Screen.CameraScan.route)
                                },
                                onNavigateTab = { tab ->
                                    navigateToTab(tab)
                                },
                                onLogout = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Màn hình Thêm bữa ăn (Add Meal)
                        composable(Screen.AddMeal.route) {
                            AddMealScreen(onBack = {
                                navController.popBackStack()
                            })
                        }

                        // 4. Màn hình Quét Camera AI (AI Camera Scan)
                        composable(Screen.CameraScan.route) {
                            CameraScanScreen(onBack = {
                                navController.popBackStack()
                            })
                        }

                        // 5. Màn hình Thống kê & Xu hướng (Statistics)
                        composable(Screen.Statistics.route) {
                            StatisticsScreen(
                                onNavigateTab = { tab ->
                                    navigateToTab(tab)
                                }
                            )
                        }

                        // 6. Màn hình Trợ lý Dinh dưỡng AI (Chatbot Coach)
                        composable(Screen.Chat.route) {
                            ChatbotScreen(
                                onNavigateTab = { tab ->
                                    navigateToTab(tab)
                                }
                            )
                        }

                        // 7. Màn hình Hồ sơ & Mục tiêu (Profile & Settings)
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                onNavigateTab = { tab ->
                                    navigateToTab(tab)
                                },
                                onLogout = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
