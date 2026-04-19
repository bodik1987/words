package com.bodik.words.components.BottomSheets

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodik.words.R
import com.bodik.words.data.Item
import com.bodik.words.ui.components.WordTextField
import com.bodik.words.ui.theme.Blue80
import com.bodik.words.ui.theme.MyFontFamily
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemBottomSheet(
    item: Item,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
) {
    val context = LocalContext.current
    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.forLanguageTag(item.targetLanguage ?: "pl")
            }
        }
        ttsInstance
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 12.dp)
                .padding(bottom = 20.dp, top = 12.dp),
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
                        value = item.name,
                        onValueChange = {},
                        placeholder = "Слово/фраза",
                        fontSize = 20.sp,
                        maxLines = 6,
                        readOnly = true,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.Medium
                    )

                    if (item.description.isNotBlank()) {
                        Spacer(
                            Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                        )

                        WordTextField(
                            value = item.description,
                            onValueChange = {},
                            placeholder = "Перевод/значение",
                            fontSize = 18.sp,
                            maxLines = 15,
                            readOnly = true,
                            fontFamily = MyFontFamily,
                            isLinkHighlightingEnabled = true
                        )
                    }

                    if (!item.example.isNullOrBlank()) {
                        Spacer(
                            Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                        )

                        WordTextField(
                            value = item.example,
                            onValueChange = {},
                            placeholder = "Пример",
                            fontSize = 16.sp,
                            maxLines = 6,
                            readOnly = true,
                            fontFamily = MyFontFamily,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onEditClick() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                ) {
                    Text(
                        "Редактировать",
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }

                if (item.isAudioCard) {
                    Button(
                        onClick = {
                            tts.language = Locale.forLanguageTag(item.targetLanguage ?: "pl")
                            tts.speak(item.name, TextToSpeech.QUEUE_FLUSH, null, null)
                        },
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            contentColor = Blue80
                        ),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.volume),
                            contentDescription = "Speak",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}