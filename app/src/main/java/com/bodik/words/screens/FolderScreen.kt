package com.bodik.words.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bodik.words.R
import com.bodik.words.components.BottomSheets.AddItemBottomSheet
import com.bodik.words.components.BottomSheets.FolderBottomSheet
import com.bodik.words.components.FolderScreenFloatingButton
import com.bodik.words.components.FolderScreenList
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.utils.FolderManager
import com.bodik.words.utils.ItemManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    folderId: String,
    onBack: () -> Unit,
    navController: NavHostController? = null
) {
    var showAddItemBottomSheet by remember { mutableStateOf(false) }
    var showFolderBottomSheet by remember { mutableStateOf(false) }
    var showEditBottomSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<com.bodik.words.data.Item?>(null) }

    // Состояние для обновления списка
    var refreshTrigger by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val folderManager = remember { FolderManager(context) }
    val itemManager = remember { ItemManager(context) }

    // Получаем информацию о текущей папке
    val folder = remember(folderId) {
        folderManager.getFolders().find { it.id == folderId }
    }

    val folderName = folder?.name ?: "Папка"

    // Функция для удаления папки
    val deleteFolder = {
        folderManager.deleteFolder(folderId)
        onBack()
    }

    // Функция для обновления списка слов
    val refreshWords = {
        refreshTrigger++
    }

    // Функция для перемещения элемента
    val moveItemToFolder: (String, String?) -> Unit = { itemId, newFolderId ->
        itemManager.moveItemToFolder(itemId, newFolderId)
        refreshWords()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        folderName,
                        maxLines = 1,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = MyFontFamily,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showFolderBottomSheet = true },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ellipsis_vertical),
                            contentDescription = "Menu",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        floatingActionButton = {
            FolderScreenFloatingButton(onAddItemClick = {
                showAddItemBottomSheet = true
            })
        }
    ) { paddingValues ->
        FolderScreenList(
            paddingValues = paddingValues,
            folderId = folderId,
            refreshTrigger = refreshTrigger,
            onEditItem = { item ->
                editingItem = item
                showEditBottomSheet = true
            },
            onMoveItemCallback = moveItemToFolder  // Используем новое имя
        )
    }

    // BottomSheet для создания нового элемента
    if (showAddItemBottomSheet) {
        AddItemBottomSheet(
            onDismiss = {
                showAddItemBottomSheet = false
                refreshWords()
            },
            folderId = folderId,
            onItemSaved = {
                refreshWords()
            },
            onMoveItem = moveItemToFolder
        )
    }

    // BottomSheet для редактирования элемента
    if (showEditBottomSheet && editingItem != null) {
        AddItemBottomSheet(
            onDismiss = {
                showEditBottomSheet = false
                editingItem = null
            },
            folderId = folderId,
            editingItem = editingItem,
            onItemSaved = {
                refreshWords()
                showEditBottomSheet = false
                editingItem = null
            },
            onMoveItem = moveItemToFolder
        )
    }

    if (showFolderBottomSheet) {
        FolderBottomSheet(
            onDismiss = { showFolderBottomSheet = false },
            folderName = folderName,
            onDeleteFolder = deleteFolder
        )
    }
}