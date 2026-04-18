package com.bodik.words.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bodik.words.components.BottomSheets.AddItemBottomSheet
import com.bodik.words.data.Item
import com.bodik.words.ui.components.IslandListItem
import com.bodik.words.ui.components.LabelText
import com.bodik.words.ui.components.ReorderableIslandColumn
import com.bodik.words.utils.ItemManager

@Composable
fun FolderScreenList(
    paddingValues: PaddingValues,
    folderId: String,
    refreshTrigger: Int = 0,
    onEditItem: ((Item) -> Unit)? = null,
    onMoveItemCallback: ((String, String?) -> Unit)? = null  // Переименовали
) {
    val context = LocalContext.current
    val itemManager = remember { ItemManager(context) }

    var items by remember { mutableStateOf(itemManager.getItemsInFolder(folderId)) }
    var showEditBottomSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }

    // Обновляем список при изменении refreshTrigger
    androidx.compose.runtime.LaunchedEffect(refreshTrigger, folderId) {
        items = itemManager.getItemsInFolder(folderId)
    }

    val refreshItems = {
        items = itemManager.getItemsInFolder(folderId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item { LabelText("Слова") }

        if (items.isEmpty()) {
            item {
                Text(
                    text = "Нет слов. Нажмите на кнопку + чтобы добавить",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            item {
                val menuItems = items.map { wordItem ->
                    IslandListItem(
                        id = wordItem.id,
                        label = wordItem.name,
                        supportingText = wordItem.description,
                        onClick = { id ->
                            onEditItem?.invoke(items.find { it.id == id }!!)
                        }
                    )
                }

                ReorderableIslandColumn(
                    items = menuItems,
                    onReorder = { reorderedItems ->
                        val reorderedWords = reorderedItems.mapNotNull { item ->
                            items.find { it.id == item.id }
                        }
                        itemManager.reorderItemsInFolder(folderId, reorderedWords)
                        refreshItems()
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
        AddItemBottomSheet(
            onDismiss = {
                showEditBottomSheet = false
                editingItem = null
            },
            folderId = folderId,
            editingItem = editingItem,
            onItemSaved = {
                refreshItems()
                showEditBottomSheet = false
                editingItem = null
            },
            onMoveItem = onMoveItemCallback
        )
    }
}