package com.bodik.words.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bodik.words.ui.components.IslandListItem
import com.bodik.words.ui.components.LabelText
import com.bodik.words.ui.components.ReorderableIslandColumn

@Composable
fun FolderScreenList(
    paddingValues: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item { LabelText("Элементы") }
        item {
            var menuItems by remember {
                mutableStateOf(
                    listOf(
                        IslandListItem(
                            id = "1",
                            label = "Элемент 1",
                            supportingText = "Субтекст"
                        ),
                    )
                )
            }

            ReorderableIslandColumn(
                items = menuItems,
                onReorder = { newItems ->
                    menuItems = newItems
                }
            )
        }
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}