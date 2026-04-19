package com.bodik.words

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bodik.words.screens.FolderScreen
import com.bodik.words.screens.MainScreen
import com.bodik.words.screens.StudyScreen
import com.bodik.words.ui.theme.WordsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordsTheme {
                val navController = rememberNavController() // Переместил navController сюда

                NavHost(
                    navController = navController,
                    startDestination = "main",
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    composable("main") {
                        MainScreen(navController = navController)
                    }
                    composable(
                        route = "folder/{folderId}",
                        arguments = listOf(navArgument("folderId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
                        FolderScreen(
                            navController = navController,
                            folderId = folderId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = "study/{folderId}",
                        arguments = listOf(navArgument("folderId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
                        StudyScreen(
                            folderId = folderId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}