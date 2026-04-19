package com.bodik.words.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.bodik.words.ui.theme.Blue80
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.utils.LinkTransformation
import com.bodik.words.utils.buildAnnotatedStringWithLinks

@Composable
fun WordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    fontSize: TextUnit,
    maxLines: Int = 1,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    isLinkHighlightingEnabled: Boolean = false
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val uriHandler = LocalUriHandler.current

    // Состояние диалога — храним URL, по которому кликнули (null = диалог закрыт)
    var pendingUrl by remember { mutableStateOf<String?>(null) }

    // Диалог показывается поверх всего компонента
    pendingUrl?.let { url ->
        AlertDialog(
            onDismissRequest = { pendingUrl = null },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            title = {
                Text(
                    "Перейти по ссылке",
                    fontFamily = MyFontFamily,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    uriHandler.openUri(url)
                    pendingUrl = null
                }) {
                    Text(
                        "Перейти",
                        fontFamily = MyFontFamily,
                        color = Blue80
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingUrl = null }) {
                    Text(
                        "Отменить",
                        fontFamily = MyFontFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )
    }

    // Вспомогательная лямбда — открывает диалог вместо прямого перехода
    fun onLinkClick(url: String) {
        pendingUrl = url
    }

    if (readOnly) {
        val annotatedString = remember(value) {
            if (isLinkHighlightingEnabled) buildAnnotatedStringWithLinks(value, linkColor)
            else buildAnnotatedString { append(value) }
        }

        val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .then(
                    if (isLinkHighlightingEnabled) {
                        Modifier.pointerInput(annotatedString) {
                            detectTapGestures { offset ->
                                layoutResult.value?.let { layout ->
                                    val position = layout.getOffsetForPosition(offset)
                                    annotatedString
                                        .getStringAnnotations("URL", position, position)
                                        .firstOrNull()
                                        ?.let { onLinkClick(it.item) }
                                }
                            }
                        }
                    } else Modifier
                )
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = fontSize,
                    fontFamily = fontFamily ?: FontFamily.Default,
                    fontWeight = fontWeight ?: FontWeight.Normal,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            } else {
                SelectionContainer {
                    Text(
                        text = annotatedString,
                        style = TextStyle(
                            fontSize = fontSize,
                            fontFamily = fontFamily ?: FontFamily.Default,
                            fontWeight = fontWeight ?: FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult.value = it }
                    )
                }
            }
        }
    } else {
        val visualTransformation = remember(isLinkHighlightingEnabled, linkColor) {
            if (isLinkHighlightingEnabled) LinkTransformation(linkColor = linkColor)
            else VisualTransformation.None
        }

        val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
        val annotatedString = remember(value) {
            if (isLinkHighlightingEnabled) buildAnnotatedStringWithLinks(value, linkColor)
            else buildAnnotatedString { append(value) }
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = false,
            enabled = enabled,
            visualTransformation = visualTransformation,
            textStyle = TextStyle(
                fontSize = fontSize,
                fontFamily = fontFamily ?: FontFamily.Default,
                fontWeight = fontWeight ?: FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            onTextLayout = { layoutResult.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .then(
                    if (isLinkHighlightingEnabled) {
                        Modifier.pointerInput(annotatedString) {
                            detectTapGestures { offset ->
                                layoutResult.value?.let { layout ->
                                    val position = layout.getOffsetForPosition(offset)
                                    annotatedString
                                        .getStringAnnotations("URL", position, position)
                                        .firstOrNull()
                                        ?.let { onLinkClick(it.item) }
                                }
                            }
                        }
                    } else Modifier
                ),
            maxLines = maxLines,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = fontSize,
                            fontFamily = fontFamily ?: FontFamily.Default,
                            fontWeight = fontWeight ?: FontWeight.Normal,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}