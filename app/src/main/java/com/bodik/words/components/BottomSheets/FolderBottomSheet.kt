package com.bodik.words.components.BottomSheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodik.words.ui.components.ITEM_SPACING
import com.bodik.words.ui.components.RADIUS_INNER
import com.bodik.words.ui.components.RADIUS_OUTER
import com.bodik.words.ui.theme.MyFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderBottomSheet(
    onDismiss: () -> Unit,
    folderName: String,
    hasAudioCards: Boolean,
    onDeleteFolder: () -> Unit,
    onRenameFolder: (String) -> Unit,
    onStudy: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameSheet by remember { mutableStateOf(false) }

    val closeSheet = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            if (hasAudioCards) {
                Button(
                    onClick = { onStudy(); closeSheet() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(
                        topStart = RADIUS_OUTER, topEnd = RADIUS_OUTER,
                        bottomStart = RADIUS_INNER, bottomEnd = RADIUS_INNER
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondary,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                ) {
                    Text(
                        "Учить аудио карточки",
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }
                Spacer(Modifier.height(ITEM_SPACING))
            }

            Button(
                onClick = { showRenameSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(
                    // Если аудиокарт нет, эта кнопка первая -> верх OUTER. Иначе INNER.
                    topStart = if (!hasAudioCards) RADIUS_OUTER else RADIUS_INNER,
                    topEnd = if (!hasAudioCards) RADIUS_OUTER else RADIUS_INNER,
                    // Снизу всегда INNER, так как после нее точно есть кнопка удаления
                    bottomStart = RADIUS_INNER,
                    bottomEnd = RADIUS_INNER
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
            ) {
                Text(
                    "Переименовать папку",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(ITEM_SPACING))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(
                    topStart = RADIUS_INNER, topEnd = RADIUS_INNER,
                    bottomStart = RADIUS_OUTER, bottomEnd = RADIUS_OUTER
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                ),
            ) {
                Text(
                    "Удалить папку",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }
    }

    if (showRenameSheet) {
        FolderActionBottomSheet(
            initialName = folderName,
            titleText = "Переименовать папку",
            onDismiss = { showRenameSheet = false },
            onConfirm = { newName ->
                onRenameFolder(newName)
                showRenameSheet = false
                closeSheet() // Закрываем основную шторку после успешного переименования
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            title = {
                Text(
                    text = "Удалить папку?",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "Вы собираетесь удалить папку. Все слова в ней также будут удалены. Это действие нельзя отменить.",
                    fontFamily = MyFontFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteFolder()
                        closeSheet()
                    }
                ) {
                    Text(
                        "Удалить",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
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
}