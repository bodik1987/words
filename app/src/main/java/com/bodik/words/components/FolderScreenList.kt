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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.bodik.words.R
import com.bodik.words.ui.components.IslandListItem
import com.bodik.words.ui.components.ReorderableIslandColumn
import com.bodik.words.utils.ItemManager
import java.util.Locale

@Composable
fun FolderScreenList(
    paddingValues: PaddingValues,
    folderId: String,
    refreshTrigger: Int = 0,
    navController: NavHostController
) {
    val context = LocalContext.current
    val itemManager = remember { ItemManager(context) }

    var items by remember { mutableStateOf(itemManager.getItemsInFolder(folderId)) }

    androidx.compose.runtime.LaunchedEffect(refreshTrigger, folderId) {
        items = itemManager.getItemsInFolder(folderId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (items.isEmpty()) {
            item {
                Text(
                    text = "Нет записей. Нажмите на кнопку + чтобы добавить",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
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

                val menuItems = items.map { wordItem ->
                    val reminderLabel = wordItem.reminderTime?.let { time ->
                        val sdf = java.text.SimpleDateFormat(
                            "dd MMM, HH:mm",
                            java.util.Locale.getDefault()
                        )
                        val prefix = if (time < System.currentTimeMillis()) "Истекло: " else ""
                        prefix + sdf.format(java.util.Date(time))
                    }
                    val exampleText =
                        listOfNotNull(wordItem.example, reminderLabel).joinToString("\n")
                            .ifBlank { null }

                    IslandListItem(
                        id = wordItem.id,
                        label = wordItem.name,
                        supportingText = wordItem.description,
                        leadingContent = if (wordItem.isAudioCard) {
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
                                                    wordItem.targetLanguage ?: "pl"
                                                )
                                            tts.speak(
                                                wordItem.name,
                                                TextToSpeech.QUEUE_FLUSH,
                                                null,
                                                null
                                            )
                                        }
                                )
                            }
                        } else null,
                        example = exampleText,
                        onClick = { id ->
                            navController.navigate("item/edit/$id")
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
                        items = itemManager.getItemsInFolder(folderId)
                    }
                )
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}