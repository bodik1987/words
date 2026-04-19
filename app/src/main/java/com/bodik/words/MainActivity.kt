package com.bodik.words

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bodik.words.screens.FolderScreen
import com.bodik.words.screens.ItemScreen
import com.bodik.words.screens.MainScreen
import com.bodik.words.screens.StudyScreen
import com.bodik.words.ui.theme.WordsTheme
import com.bodik.words.utils.ItemManager

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Запрос POST_NOTIFICATIONS на Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()
        setContent {
            WordsTheme {
                val navController = rememberNavController()

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

                    composable(route = "item/add") {
                        ItemScreen(
                            editingItem = null,
                            folderId = null,
                            onBack = { navController.popBackStack() },
                            onItemSaved = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "item/add/{folderId}",
                        arguments = listOf(navArgument("folderId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
                        ItemScreen(
                            editingItem = null,
                            folderId = folderId,
                            onBack = { navController.popBackStack() },
                            onItemSaved = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "item/edit/{itemId}",
                        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                        val context = LocalContext.current
                        val itemManager = ItemManager(context)
                        val item = itemManager.getItemById(itemId)

                        if (item != null) {
                            ItemScreen(
                                editingItem = item,
                                folderId = item.folderId,
                                onBack = { navController.popBackStack() },
                                onItemSaved = { navController.popBackStack() },
                                onItemDeleted = { navController.popBackStack() },
                            )
                        } else {
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}