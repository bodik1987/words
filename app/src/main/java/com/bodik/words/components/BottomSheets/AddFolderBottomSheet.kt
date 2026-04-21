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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodik.words.ui.components.RADIUS_OUTER
import com.bodik.words.ui.components.WordTextField
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.ui.theme.Orange80
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFolderBottomSheet(
    onDismiss: () -> Unit,
    onFolderAdded: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                .padding(bottom = 20.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Добавить папку",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(RADIUS_OUTER),
                color = MaterialTheme.colorScheme.onSecondary,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    WordTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Название папки",
                        fontSize = 20.sp,
                        maxLines = 3,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onFolderAdded(title)
                        closeSheet()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(RADIUS_OUTER),
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (title.isNotBlank()) Orange80
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = if (title.isNotBlank()) Color.White
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                ),
            ) {
                Text(
                    "Сохранить",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
        }
    }
}

