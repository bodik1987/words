package com.bodik.words.components.BottomSheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.bodik.words.data.Item
import com.bodik.words.data.Language
import com.bodik.words.ui.components.CustomSwitch
import com.bodik.words.ui.components.WordTextField
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.ui.theme.Orange80
import com.bodik.words.utils.ItemManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemBottomSheet(
    onDismiss: () -> Unit,
    folderId: String? = null,
    editingItem: Item? = null,
    onItemSaved: () -> Unit = {},
    onItemDeleted: ((String) -> Unit)? = null,
    onMoveItem: ((String, String?) -> Unit)? = null
) {
    val context = LocalContext.current
    val itemManager = remember { ItemManager(context) }

    // Состояния формы
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

    // Состояние для модалки перемещения
    var showMoveBottomSheet by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    val isEditMode = editingItem != null
    val titleText = if (isEditMode) "Редактировать" else "Добавить"

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val closeSheet = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    val saveItem = {
        if (name.isNotBlank() && description.isNotBlank()) {
            if (isEditMode) {
                val updatedItem = editingItem.copy(
                    name = name,
                    description = description,
                    example = example.takeIf { it.isNotBlank() },
                    isAudioCard = isAudioCard,
                    targetLanguage = if (isAudioCard) selectedLanguage.code else "pl",
                    folderId = editingItem.folderId
                )
                itemManager.updateItem(updatedItem)
            } else {
                val newItem = Item(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    description = description,
                    example = example.takeIf { it.isNotBlank() },
                    isAudioCard = isAudioCard,
                    targetLanguage = if (isAudioCard) selectedLanguage.code else "pl",
                    folderId = folderId
                )
                itemManager.addItem(newItem)
            }
            onItemSaved()
            closeSheet()
        }
    }

    val deleteItem = {
        if (isEditMode) {
            val idToDelete = editingItem.id
            itemManager.deleteItem(idToDelete)
            onItemDeleted?.invoke(idToDelete)
            closeSheet()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            // Заголовок
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Левая кнопка - Переместить (только в режиме редактирования)
                if (isEditMode && onMoveItem != null) {
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
                            contentDescription = "Move",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }

                Text(
                    text = titleText,
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                CustomSwitch(
                    checked = isAudioCard,
                    onCheckedChange = { isAudioCard = it },
                )
            }

            Spacer(Modifier.height(24.dp))

            // Форма
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(34.dp),
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
                        maxLines = 3,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.Medium
                    )

                    WordTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Перевод/значение",
                        fontSize = 18.sp,
                        maxLines = 3,
                        fontFamily = MyFontFamily,
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
                        maxLines = 5,
                        fontFamily = MyFontFamily,
                    )

                    if (isAudioCard) {
                        Spacer(
                            Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLanguageMenu = true }
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Язык озвучивания",
                                fontFamily = MyFontFamily,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Box {
                                Text(
                                    text = selectedLanguage.displayName,
                                    fontFamily = MyFontFamily,
                                    fontSize = 16.sp,
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
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Кнопки: Удалить и Обновить/Сохранить
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditMode) {
                    Button(
                        onClick = deleteItem,
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Delete",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                // Кнопка Обновить/Сохранить
                Button(
                    onClick = { saveItem() },
                    modifier = Modifier
                        .weight(if (isEditMode) 1f else 1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(34.dp),
                    enabled = name.isNotBlank() && description.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (name.isNotBlank() && description.isNotBlank()) Orange80
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = if (name.isNotBlank() && description.isNotBlank()) Color.White
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(
                        if (isEditMode) "Обновить" else "Сохранить",
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }

    // BottomSheet для перемещения
    if (showMoveBottomSheet && editingItem != null) {
        MoveItemBottomSheet(
            onDismiss = { showMoveBottomSheet = false },
            itemId = editingItem.id,
            currentFolderId = editingItem.folderId,
            onMove = { itemId, newFolderId ->
                onMoveItem?.invoke(itemId, newFolderId)
                showMoveBottomSheet = false
                closeSheet()
            }
        )
    }
}