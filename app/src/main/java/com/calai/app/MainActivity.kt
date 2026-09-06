package com.calai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calai.app.data.local.UserPreferencesManager
import com.calai.app.presentation.components.DockTab
import com.calai.app.presentation.navigation.Screen
import com.calai.app.presentation.screens.*
import com.calai.app.presentation.theme.CalAITheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkThemePref by preferencesManager.isDarkTheme.collectAsState()
            var isDarkTheme by remember(isDarkThemePref) { mutableStateOf(isDarkThemePref) }

            fun onThemeChanged(newTheme: Boolean) {
                isDarkTheme = newTheme
                preferencesManager.toggleTheme(newTheme)
            }

            CalAITheme(darkTheme = isDarkTheme) {
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
                            LoginScreen(onLoginSuccess = { isNewRegistration ->
                                val destination = if (isNewRegistration) Screen.Onboarding.route else Screen.Home.route
                                navController.navigate(destination) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            })
                        }

                        // 1b. Onboarding Wizard (chỉ hiện sau khi Đăng ký tài khoản mới)
                        composable(Screen.Onboarding.route) {
                            OnboardingScreen(
                                onFinished = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                                    }
                                },
                                isDarkTheme = isDarkTheme
                            )
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
                                },
                                onOpenSuggestions = {
                                    navController.navigate(Screen.Suggestions.route)
                                },
                                isDarkTheme = isDarkTheme,
                                onThemeChanged = { onThemeChanged(it) }
                            )
                        }

                        // 2b. Màn hình Gợi ý Thực đơn & Tập luyện (Suggestions - All in One)
                        composable(Screen.Suggestions.route) {
                            SuggestionsScreen(
                                onBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToLogWorkout = {
                                    navController.navigate(Screen.LogWorkout.route)
                                },
                                onNavigateToWorkoutHub = {
                                    navController.navigate(Screen.WorkoutHub.route)
                                },
                                isDarkTheme = isDarkTheme
                            )
                        }

                        // 2c. Màn hình Trung tâm Tập luyện (Workout Hub: Program + History + Exercise Library)
                        composable(Screen.WorkoutHub.route) {
                            WorkoutHubScreen(
                                onBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToLogWorkout = {
                                    navController.navigate(Screen.LogWorkout.route)
                                },
                                isDarkTheme = isDarkTheme
                            )
                        }

                        // 2d. Màn hình Ghi nhận Buổi tập (Log Workout - Sets, Reps, Calo, MET, Rest Timer)
                        composable(Screen.LogWorkout.route) {
                            LogWorkoutScreen(
                                onBack = {
                                    navController.popBackStack()
                                },
                                onSaveSuccess = {
                                    navController.popBackStack()
                                },
                                isDarkTheme = isDarkTheme
                            )
                        }

                        // 3. Màn hình Thêm bữa ăn (Add Meal)
                        composable(Screen.AddMeal.route) {
                            AddMealScreen(
                                onBack = {
                                    navController.popBackStack()
                                },
                                onCameraClick = {
                                    navController.navigate(Screen.CameraScan.route)
                                },
                                isDarkTheme = isDarkTheme
                            )
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
                                },
                                onNavigateToWeightHistory = {
                                    navController.navigate(Screen.WeightHistory.route)
                                },
                                isDarkTheme = isDarkTheme
                            )
                        }

                        // 5b. Màn hình Lịch sử cân nặng đầy đủ (xem/sửa/xóa từng bản ghi)
                        composable(Screen.WeightHistory.route) {
                            WeightHistoryScreen(
                                onBack = {
                                    navController.popBackStack()
                                },
                                isDarkTheme = isDarkTheme
                            )
                        }

                        // 6. Màn hình Trợ lý Dinh dưỡng AI (Chatbot Coach)
                        composable(Screen.Chat.route) {
                            ChatbotScreen(
                                onNavigateTab = { tab ->
                                    navigateToTab(tab)
                                },
                                isDarkTheme = isDarkTheme
                            )
                        }

                        // 7. Màn hình Hồ sơ & Mục tiêu (Profile)
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                onNavigateTab = { tab ->
                                    navigateToTab(tab)
                                },
                                onLogout = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { onThemeChanged(it) },
                                onOpenGoalSetup = {
                                    navController.navigate(Screen.GoalSetup.route)
                                },
                                onOpenSettings = {
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                        }

                        // 8. Màn hình Mục tiêu & Chương trình (Goal Setup)
                        composable(Screen.GoalSetup.route) {
                            GoalSetupScreen(
                                onBack = {
                                    navController.popBackStack()
                                },
                                isDarkTheme = isDarkTheme
                            )
                        }

                        // 9. Màn hình Cài đặt Hệ thống & Giao diện (Settings Screen - Spec STT 132-134)
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onBack = {
                                    navController.popBackStack()
                                },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { onThemeChanged(it) },
                                onOpenGoalSetup = {
                                    navController.navigate(Screen.GoalSetup.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

