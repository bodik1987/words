package com.bodik.words.components

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.bodik.words.R
import com.bodik.words.data.Folder
import com.bodik.words.data.Item
import com.bodik.words.ui.components.ClickableIslandColumn
import com.bodik.words.ui.components.IslandListItem
import com.bodik.words.ui.components.LabelText
import com.bodik.words.ui.components.ReorderableIslandColumn
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.utils.ItemManager
import java.util.Locale

@Composable
fun MainScreenList(
    paddingValues: PaddingValues,
    navController: NavHostController,
    folders: List<Folder>,
    unassignedItems: List<Item>,
    onReorderFolders: (List<Folder>) -> Unit,
    onReorderItems: (List<Item>) -> Unit,
    onDeleteItem: (String) -> Unit,
    onMoveItem: (String, String?) -> Unit,
    searchQuery: String = "",
    searchResults: List<Item> = emptyList()
) {
    val context = LocalContext.current
    val itemManager = remember { ItemManager(context) }

    val filteredItems = remember(unassignedItems, searchQuery) {
        if (searchQuery.isBlank()) unassignedItems
        else unassignedItems.filter { item ->
            item.name.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
        }
    }

    val showSearch = searchQuery.isNotBlank()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (showSearch) {
            item { LabelText("Результаты поиска") }

            if (searchResults.isEmpty()) {
                item {
                    Text(
                        text = "Ничего не найдено",
                        modifier = Modifier.padding(16.dp),
                        fontFamily = MyFontFamily,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                item {
                    val menuItems = searchResults.map { item ->
                        val folderName = folders.find { it.id == item.folderId }?.name
                        IslandListItem(
                            id = item.id,
                            label = item.name,
                            supportingText = item.description,
                            example = if (folderName != null) "📁 $folderName" else item.example,
                            onClick = { id ->
                                // Переход на экран редактирования через navController
                                navController.navigate("item/edit/$id")
                            }
                        )
                    }
                    ClickableIslandColumn(items = menuItems)
                }
            }
        } else {
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

            if (filteredItems.isNotEmpty()) {
                item { Spacer(Modifier.height(24.dp)) }
                item {
                    val context = LocalContext.current
                    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

                    val tts = remember {
                        var ttsInstance: TextToSpeech? = null
                        ttsInstance = TextToSpeech(context) { status ->
                            if (status == TextToSpeech.SUCCESS) {
                                ttsInstance?.language = Locale.forLanguageTag("pl")
                            }
                        }
                        ttsInstance
                    }

                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_PAUSE) {
                                tts.stop()
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                            tts.stop()
                            tts.shutdown()
                        }
                    }

                    val itemMenuItems = filteredItems.map { item ->
                        IslandListItem(
                            id = item.id,
                            label = item.name,
                            supportingText = item.description,
                            leadingContent = if (item.isAudioCard) {
                                {
                                    Icon(
                                        painter = painterResource(id = R.drawable.volume),
                                        contentDescription = "Speak",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                tts.language =
                                                    Locale.forLanguageTag(
                                                        item.targetLanguage ?: "pl"
                                                    )
                                                tts.speak(
                                                    item.name,
                                                    TextToSpeech.QUEUE_FLUSH,
                                                    null,
                                                    null
                                                )
                                            }
                                    )
                                }
                            } else null,
                            example = item.example,
                            onClick = { id ->
                                // Переход на экран редактирования через navController
                                navController.navigate("item/edit/$id")
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
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}