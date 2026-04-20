package com.bodik.words.components.BottomSheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.bodik.words.ui.theme.MyFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingDialog(
    name: String,
    description: String,
    example: String?,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        title = null, // Заголовок не нужен, так как имя будет первым элементом
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 22.sp,
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        fontSize = 20.sp,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!example.isNullOrBlank()) {
                    Text(
                        text = example,
                        fontSize = 18.sp,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        confirmButton = {}
    )
}