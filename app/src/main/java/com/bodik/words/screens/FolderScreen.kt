package com.bodik.words.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.bodik.words.R
import com.bodik.words.components.BottomSheets.AddItemBottomSheet
import com.bodik.words.components.FolderScreenFloatingButton
import com.bodik.words.components.FolderScreenList
import com.bodik.words.ui.theme.MyFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(folderId: String, onBack: () -> Unit) {
    var showAddItemBottomSheet by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Папка $folderId", maxLines = 1,
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
                }
            )
        },
        floatingActionButton = {
            FolderScreenFloatingButton(onAddItemClick = {
                showAddItemBottomSheet = true
            })
        }
    ) { paddingValues ->
        FolderScreenList(paddingValues)
    }

    if (showAddItemBottomSheet) {
        AddItemBottomSheet(
            onDismiss = { showAddItemBottomSheet = false },
        )
    }
}