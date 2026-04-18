package com.bodik.words.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.bodik.words.components.BottomSheets.AddFolderBottomSheet
import com.bodik.words.components.BottomSheets.EditItemBottomSheet
import com.bodik.words.components.BottomSheets.SettingsBottomSheet
import com.bodik.words.components.MainScreenFloatingButtons
import com.bodik.words.components.MainScreenList
import com.bodik.words.components.TopBar
import com.bodik.words.utils.FolderManager
import com.bodik.words.utils.ItemManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    var showAddFolderBottomSheet by remember { mutableStateOf(false) }
    var showAddItemBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val folderManager = remember { FolderManager(context) }
    val itemManager = remember { ItemManager(context) }

    // Состояние для папок
    var folders by remember { mutableStateOf(folderManager.getFolders()) }

    // Состояние для элементов без папки
    var unassignedItems by remember { mutableStateOf(itemManager.getUnassignedItems()) }

    fun refreshFolders() {
        folders = folderManager.getFolders()
    }

    fun refreshUnassignedItems() {
        unassignedItems = itemManager.getUnassignedItems()
    }

    val moveItemToFolder = { itemId: String, folderId: String? ->
        itemManager.moveItemToFolder(itemId, folderId)
        refreshUnassignedItems()
    }

    Scaffold(
        topBar = { TopBar(onMenuClick = { showSettingsBottomSheet = true }) },
        floatingActionButton = {
            MainScreenFloatingButtons(
                onAddFolderClick = { showAddFolderBottomSheet = true },
                onAddItemClick = { showAddItemBottomSheet = true }
            )
        }
    ) { paddingValues ->
        MainScreenList(
            paddingValues = paddingValues,
            navController = navController,
            folders = folders,
            unassignedItems = unassignedItems,
            onReorderFolders = { reorderedFolders ->
                folderManager.saveFolders(reorderedFolders)
                refreshFolders()
            },
            onReorderItems = { reorderedItems ->
                itemManager.saveUnassignedItems(reorderedItems) // ← было saveItems
                refreshUnassignedItems()
            },
            onDeleteItem = { itemId ->
                itemManager.deleteItem(itemId)
                refreshUnassignedItems()
            },
            onMoveItem = moveItemToFolder
        )
    }

    if (showSettingsBottomSheet) {
        SettingsBottomSheet(
            onDismiss = { showSettingsBottomSheet = false },
        )
    }

    if (showAddFolderBottomSheet) {
        AddFolderBottomSheet(
            onDismiss = {
                showAddFolderBottomSheet = false
                refreshFolders()
            },
            onFolderAdded = { folderName ->
                folderManager.addFolder(folderName)
                refreshFolders()
            }
        )
    }

    if (showAddItemBottomSheet) {
        EditItemBottomSheet(
            onDismiss = {
                showAddItemBottomSheet = false
            },
            folderId = null,
            editingItem = null, // Явно передаем null для режима создания
            onItemSaved = {
                refreshUnassignedItems()
                showAddItemBottomSheet = false // Закрываем после сохранения
            },
            onMoveItem = moveItemToFolder
        )
    }
}