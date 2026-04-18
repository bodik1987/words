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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bodik.words.R
import com.bodik.words.data.Folder
import com.bodik.words.ui.components.IslandListItem
import com.bodik.words.ui.components.LabelText
import com.bodik.words.ui.components.ReorderableIslandColumn

@Composable
fun MainScreenList(
    paddingValues: PaddingValues,
    navController: NavHostController,
    folders: List<Folder>,
    onReorder: (List<Folder>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
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
                    onReorder(reorderedFolders)
                }
            )
        }
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}