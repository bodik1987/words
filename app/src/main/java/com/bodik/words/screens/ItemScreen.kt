package com.bodik.words.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodik.words.R
import com.bodik.words.components.BottomSheets.MoveItemBottomSheet
import com.bodik.words.data.Item
import com.bodik.words.data.Language
import com.bodik.words.ui.components.CustomSwitch
import com.bodik.words.ui.components.WordTextField
import com.bodik.words.ui.theme.Blue80
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.ui.theme.Orange80
import com.bodik.words.utils.ItemManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    editingItem: Item? = null,
    folderId: String? = null,
    onBack: () -> Unit,
    onItemSaved: () -> Unit = {},
    onItemDeleted: (() -> Unit)? = null,
    onMoveItem: ((String, String?) -> Unit)? = null
) {
    val context = LocalContext.current
    val itemManager = remember { ItemManager(context) }
    val isEditMode = editingItem != null

    var name by remember { mutableStateOf(editingItem?.name ?: "") }
    var description by remember { mutableStateOf(editingItem?.description ?: "") }
    var example by remember { mutableStateOf(editingItem?.example ?: "") }
    var isAudioCard by remember { mutableStateOf(editingItem?.isAudioCard ?: false) }
    var selectedLanguage by remember {
        mutableStateOf(
            Language.entries.find { it.code == (editingItem?.targetLanguage ?: "pl") }
                ?: Language.PL
        )
    }
    var showMoveBottomSheet by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val titleText = if (isEditMode) "Редактировать" else "Добавить"

    val hasChanges = if (isEditMode) {
        name != (editingItem?.name ?: "") ||
                description != (editingItem?.description ?: "") ||
                example != (editingItem?.example ?: "") ||
                isAudioCard != (editingItem?.isAudioCard ?: false)
    } else {
        name.isNotBlank() || description.isNotBlank() || example.isNotBlank()
    }

    // Перехватываем назад если есть несохранённые изменения
    BackHandler(enabled = hasChanges) {
        showDiscardDialog = true
    }

    val saveItem = {
        if (name.isNotBlank() && description.isNotBlank()) {
            if (isEditMode) {
                itemManager.updateItem(
                    editingItem.copy(
                        name = name,
                        description = description,
                        example = example.takeIf { it.isNotBlank() },
                        isAudioCard = isAudioCard,
                        targetLanguage = if (isAudioCard) selectedLanguage.code else "pl",
                    )
                )
            } else {
                itemManager.addItem(
                    Item(
                        id = System.currentTimeMillis().toString(),
                        name = name,
                        description = description,
                        example = example.takeIf { it.isNotBlank() },
                        isAudioCard = isAudioCard,
                        targetLanguage = if (isAudioCard) selectedLanguage.code else "pl",
                        folderId = folderId
                    )
                )
            }
            onBack()
        }
    }

    // Диалог удаления
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            title = {
                Text(
                    text = "Удалить карточку?",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "Это действие нельзя будет отменить. Вы уверены?",
                    fontFamily = MyFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    if (isEditMode) {
                        itemManager.deleteItem(editingItem.id)
                        onItemDeleted?.invoke()
                        onBack()
                    }
                }) {
                    Text(
                        "Удалить",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        "Отмена",
                        fontFamily = MyFontFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Диалог подтверждения выхода
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            title = {
                Text(
                    text = "Отменить изменения?",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "Несохранённые изменения будут потеряны.",
                    fontFamily = MyFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onBack()
                }) {
                    Text(
                        "Закрыть",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(
                        "Продолжить",
                        fontFamily = MyFontFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleText,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    Button(
                        onClick = {
                            if (hasChanges) showDiscardDialog = true else onBack()
                        },
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Назад",
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Удалить",
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))

                    if (editingItem != null) {
                        Button(
                            onClick = { showMoveBottomSheet = true },
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.folder),
                                contentDescription = "Move to folder",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 12.dp)
                .padding(bottom = 20.dp, top = 8.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    WordTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Слово/фраза",
                        fontSize = 20.sp,
                        maxLines = 6,
                        readOnly = false,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(
                        Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    )

                    WordTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Перевод/значение",
                        fontSize = 18.sp,
                        maxLines = 15,
                        readOnly = false,
                        fontFamily = MyFontFamily,
                        isLinkHighlightingEnabled = true
                    )

                    Spacer(
                        Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    )

                    WordTextField(
                        value = example,
                        onValueChange = { example = it },
                        placeholder = "Пример (необязательно)",
                        fontSize = 16.sp,
                        maxLines = 6,
                        readOnly = false,
                        fontFamily = MyFontFamily,
                    )

                    Spacer(
                        Modifier
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAudioCard) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showLanguageMenu = true }
                            ) {
                                Text(
                                    text = selectedLanguage.displayName,
                                    fontFamily = MyFontFamily,
                                    fontSize = 18.sp,
                                    color = Orange80
                                )
                                DropdownMenu(
                                    expanded = showLanguageMenu,
                                    onDismissRequest = { showLanguageMenu = false }
                                ) {
                                    Language.entries.forEach { language ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    language.displayName,
                                                    fontFamily = MyFontFamily
                                                )
                                            },
                                            onClick = {
                                                selectedLanguage = language
                                                showLanguageMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Озвучивать",
                                fontFamily = MyFontFamily,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        CustomSwitch(
                            checked = isAudioCard,
                            onCheckedChange = { isAudioCard = it },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { saveItem() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(34.dp),
                enabled = name.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (name.isNotBlank() && description.isNotBlank()) Blue80
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = Color.White
                ),
            ) {
                Text(
                    text = "Сохранить",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }
    }

    if (showMoveBottomSheet && editingItem != null) {
        MoveItemBottomSheet(
            onDismiss = { showMoveBottomSheet = false },
            itemId = editingItem.id,
            currentFolderId = editingItem.folderId,
            onMove = { itemId, newFolderId ->
                val itemManager = ItemManager(context)
                itemManager.moveItemToFolder(itemId, newFolderId)
                showMoveBottomSheet = false
                onItemSaved() // Вызываем callback, чтобы обновить список и закрыть экран
            }
        )
    }
}