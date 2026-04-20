package com.bodik.words.components.BottomSheets

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.utils.ExportImportManager
import com.bodik.words.utils.FolderManager
import com.bodik.words.utils.ItemManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    onImportDone: () -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val folderManager = remember { FolderManager(context) }
    val itemManager = remember { ItemManager(context) }

    // Лаунчер для выбора места сохранения файла
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            val folders = folderManager.getFolders()
            val items = itemManager.getAllItems()
            val content = ExportImportManager.exportToString(folders, items)
            ExportImportManager.writeToUri(context, uri, content)
            Toast.makeText(context, "Экспорт выполнен", Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val content = ExportImportManager.readFromUri(context, uri)
            val (folders, items) = ExportImportManager.importFromString(content)

            // Добавляем к существующим, избегая дубликатов по id
            val existingFolderIds = folderManager.getFolders().map { it.id }.toSet()
            val existingItemIds = itemManager.getAllItems().map { it.id }.toSet()

            val newFolders = folders.filter { it.id !in existingFolderIds }
            val newItems = items.filter { it.id !in existingItemIds }

            folderManager.saveFolders(folderManager.getFolders() + newFolders)
            itemManager.saveItems(itemManager.getAllItems() + newItems)

            onImportDone()
            Toast.makeText(
                context,
                "Импортировано: ${newFolders.size} папок, ${newItems.size} слов",
                Toast.LENGTH_LONG
            ).show()
        }
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Настройки",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    importLauncher.launch(arrayOf("text/plain"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(34.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
            ) {
                Text(
                    "Импортировать",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    exportLauncher.launch("notepad_backup.txt")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(34.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
            ) {
                Text(
                    "Экспортировать",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }
    }
}