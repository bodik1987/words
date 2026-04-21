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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.bodik.words.components.BottomSheets.FolderBottomSheet
import com.bodik.words.components.FolderScreenFloatingButton
import com.bodik.words.components.FolderScreenList
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.utils.FolderManager
import com.bodik.words.utils.ItemManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    navController: NavHostController,
    folderId: String,
    onBack: () -> Unit,
) {
    var showFolderBottomSheet by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val folderManager = remember { FolderManager(context) }

    var folderName by remember(folderId) {
        mutableStateOf(folderManager.getFolders().find { it.id == folderId }?.name ?: "Папка")
    }

    val itemManager = remember { ItemManager(context) }

    val hasAudioCards = remember(folderId, refreshTrigger) {
        itemManager.getItemsInFolder(folderId).any { it.isAudioCard }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
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
                        modifier = Modifier.size(44.dp),
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
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            )
        },
        floatingActionButton = {
            FolderScreenFloatingButton(
                onAddItemClick = { navController.navigate("item/add/$folderId") }
            )
        }
    ) { paddingValues ->
        FolderScreenList(
            paddingValues = paddingValues,
            folderId = folderId,
            refreshTrigger = refreshTrigger,
            navController = navController
        )
    }

    if (showFolderBottomSheet) {
        FolderBottomSheet(
            onDismiss = { showFolderBottomSheet = false },
            folderName = folderName,
            hasAudioCards = hasAudioCards,
            onDeleteFolder = {
                folderManager.deleteFolder(folderId)
                onBack()
            },
            onRenameFolder = { newName ->
                folderManager.renameFolder(folderId, newName)
                folderName = newName
                showFolderBottomSheet = false
            },
            onStudy = { navController.navigate("study/$folderId") },
        )
    }
}