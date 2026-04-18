package com.bodik.words.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.bodik.words.components.BottomSheets.AddFolderBottomSheet
import com.bodik.words.components.BottomSheets.AddItemBottomSheet
import com.bodik.words.components.BottomSheets.SettingsBottomSheet
import com.bodik.words.components.MainScreenFloatingButtons
import com.bodik.words.components.MainScreenList
import com.bodik.words.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    var showAddFolderBottomSheet by remember { mutableStateOf(false) }
    var showAddItemBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopBar(onMenuClick = { showSettingsBottomSheet = true }) },
        floatingActionButton = {
            MainScreenFloatingButtons(onAddFolderClick = {
                showAddFolderBottomSheet = true
            }, onAddItemClick = {
                showAddItemBottomSheet = true
            })
        }
    ) { paddingValues ->
        MainScreenList(paddingValues, navController)
    }

    if (showSettingsBottomSheet) {
        SettingsBottomSheet(
            onDismiss = { showSettingsBottomSheet = false },
        )
    }

    if (showAddFolderBottomSheet) {
        AddFolderBottomSheet(
            onDismiss = { showAddFolderBottomSheet = false },
        )
    }

    if (showAddItemBottomSheet) {
        AddItemBottomSheet(
            onDismiss = { showAddItemBottomSheet = false },
        )
    }

}
