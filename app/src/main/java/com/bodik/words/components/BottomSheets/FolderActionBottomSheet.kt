package com.bodik.words.components.BottomSheets

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodik.words.ui.components.ITEM_SPACING
import com.bodik.words.ui.components.RADIUS_INNER
import com.bodik.words.ui.components.RADIUS_OUTER
import com.bodik.words.ui.components.WordTextField
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.ui.theme.Orange80
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderActionBottomSheet(
    initialName: String = "",
    titleText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val closeSheet = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    // Логика: кнопка активна если текст не пуст И (для переименования) текст изменился
    val isChanged = name.isNotBlank() && name.trim() != initialName
    val isEnabled = if (initialName.isEmpty()) name.isNotBlank() else isChanged

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
            Text(
                text = titleText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                fontFamily = MyFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = RADIUS_OUTER, topEnd = RADIUS_OUTER,
                    bottomStart = RADIUS_INNER, bottomEnd = RADIUS_INNER
                ),
                color = MaterialTheme.colorScheme.onSecondary,
            ) {
                WordTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Название папки",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    fontSize = 20.sp,
                    fontFamily = MyFontFamily
                )
            }

            Spacer(Modifier.height(ITEM_SPACING))

            Button(
                onClick = {
                    if (isEnabled) {
                        onConfirm(name.trim()); closeSheet()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(
                    topStart = RADIUS_INNER, topEnd = RADIUS_INNER,
                    bottomStart = RADIUS_OUTER, bottomEnd = RADIUS_OUTER
                ),
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange80,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
            ) {
                Text("Сохранить", fontFamily = MyFontFamily, fontSize = 18.sp)
            }
        }
    }
}