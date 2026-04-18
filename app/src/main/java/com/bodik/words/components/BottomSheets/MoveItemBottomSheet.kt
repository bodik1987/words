package com.bodik.words.components.BottomSheets

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.ui.theme.Orange80
import com.bodik.words.utils.FolderManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveItemBottomSheet(
    onDismiss: () -> Unit,
    itemId: String,
    currentFolderId: String?,
    onMove: (String, String?) -> Unit
) {
    val context = LocalContext.current
    val folderManager = remember { FolderManager(context) }
    val folders = remember { folderManager.getFolders() }

    var selectedFolderId by remember { mutableStateOf<String?>(currentFolderId) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val closeSheet = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Переместить в папку",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(34.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Опция "Без папки"
                    Button(
                        onClick = {
                            selectedFolderId = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedFolderId == null)
                                Orange80
                            else
                                MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        Text(
                            "📁 Без папки (на главном экране)",
                            fontFamily = MyFontFamily,
                            color = if (selectedFolderId == null)
                                androidx.compose.ui.graphics.Color.White
                            else
                                MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Список папок
                    folders.forEach { folder ->
                        Button(
                            onClick = {
                                selectedFolderId = folder.id
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedFolderId == folder.id)
                                    Orange80
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        ) {
                            Text(
                                "📁 ${folder.name}",
                                fontFamily = MyFontFamily,
                                color = if (selectedFolderId == folder.id)
                                    androidx.compose.ui.graphics.Color.White
                                else
                                    MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedFolderId != currentFolderId) {
                        onMove(itemId, selectedFolderId)
                    }
                    closeSheet()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(34.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange80,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
            ) {
                Text(
                    "Переместить",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }
    }
}