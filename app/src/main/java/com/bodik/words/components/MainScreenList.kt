package com.bodik.words.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bodik.words.R
import com.bodik.words.components.BottomSheets.ItemBottomSheet
import com.bodik.words.components.BottomSheets.MoveItemBottomSheet
import com.bodik.words.data.Folder
import com.bodik.words.data.Item
import com.bodik.words.ui.components.IslandListItem
import com.bodik.words.ui.components.LabelText
import com.bodik.words.ui.components.ReorderableIslandColumn
import com.bodik.words.utils.ItemManager

@Composable
fun MainScreenList(
    paddingValues: PaddingValues,
    navController: NavHostController,
    folders: List<Folder>,
    unassignedItems: List<Item>,
    onReorderFolders: (List<Folder>) -> Unit,
    onReorderItems: (List<Item>) -> Unit,
    onDeleteItem: (String) -> Unit, // У вас уже есть этот коллбэк!
    onMoveItem: (String, String?) -> Unit
) {
    val context = LocalContext.current
    val itemManager = remember { ItemManager(context) }

    // Состояния для редактирования
    var showEditBottomSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }

    // Состояния для перемещения
    var showMoveBottomSheet by remember { mutableStateOf(false) }
    var movingItemId by remember { mutableStateOf<String?>(null) }
    var movingItemCurrentFolder by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Секция папок
        item { LabelText("Папки") }
        item {
            val menuItems = folders.map { folder ->
                IslandListItem(
                    id = folder.id,
                    label = folder.name,
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.folder),
                            contentDescription = folder.name,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = { id ->
                        navController.navigate("folder/$id")
                    }
                )
            }

            ReorderableIslandColumn(
                items = menuItems,
                onReorder = { reorderedItems ->
                    val reorderedFolders = reorderedItems.mapNotNull { item ->
                        folders.find { it.id == item.id }
                    }
                    onReorderFolders(reorderedFolders)
                }
            )
        }

        if (unassignedItems.isNotEmpty()) {
            item { Spacer(Modifier.height(24.dp)) }
            item { LabelText("Слова") }
            item {
                val itemMenuItems = unassignedItems.map { item ->
                    IslandListItem(
                        id = item.id,
                        label = item.name,
                        supportingText = item.description,
                        example = item.example,
                        onClick = { id ->
                            editingItem = unassignedItems.find { it.id == id }
                            showEditBottomSheet = true
                        },
                    )
                }

                ReorderableIslandColumn(
                    items = itemMenuItems,
                    onReorder = { reorderedItems ->
                        val reorderedItemObjects = reorderedItems.mapNotNull { item ->
                            unassignedItems.find { it.id == item.id }
                        }
                        onReorderItems(reorderedItemObjects)
                    }
                )
            }
        }

        item { Spacer(Modifier.height(10.dp)) }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showEditBottomSheet && editingItem != null) {
        ItemBottomSheet(
            onDismiss = {
                showEditBottomSheet = false
                editingItem = null
            },
            folderId = editingItem?.folderId,
            editingItem = editingItem,
            onItemSaved = {
                onReorderItems(itemManager.getUnassignedItems())
                showEditBottomSheet = false
                editingItem = null
            },
            onItemDeleted = { itemId: String ->
                onDeleteItem(itemId)
                showEditBottomSheet = false
                editingItem = null
            },
            onMoveItem = onMoveItem
        )
    }

    // BottomSheet для перемещения
    if (showMoveBottomSheet && movingItemId != null) {
        MoveItemBottomSheet(
            onDismiss = {
                showMoveBottomSheet = false
                movingItemId = null
                movingItemCurrentFolder = null
            },
            itemId = movingItemId!!,
            currentFolderId = movingItemCurrentFolder,
            onMove = { itemId, newFolderId ->
                onMoveItem(itemId, newFolderId)
            }
        )
    }
}