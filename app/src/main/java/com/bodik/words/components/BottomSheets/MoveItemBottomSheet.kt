package com.bodik.words.components.BottomSheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.bodik.words.ui.components.RADIUS_OUTER
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
                .padding(horizontal = 12.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(RADIUS_OUTER),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FolderIslandItem(
                        name = "Без папки",
                        isSelected = selectedFolderId == null,
                        isFirst = true,
                        isLast = folders.isEmpty(),
                        onClick = { selectedFolderId = null }
                    )

                    folders.forEachIndexed { index, folder ->
                        Spacer(
                            Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                        )
                        FolderIslandItem(
                            name = folder.name,
                            isSelected = selectedFolderId == folder.id,
                            isFirst = false,
                            isLast = index == folders.lastIndex,
                            onClick = { selectedFolderId = folder.id }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

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
                shape = RoundedCornerShape(RADIUS_OUTER),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange80,
                    contentColor = Color.White
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

@Composable
private fun FolderIslandItem(
    name: String,
    isSelected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.ListItem(
        headlineContent = {
            Text(
                text = name,
                fontFamily = MyFontFamily,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Orange80 else MaterialTheme.colorScheme.onBackground
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.folder),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) Orange80 else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = null,
                    tint = Orange80,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = androidx.compose.material3.ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}