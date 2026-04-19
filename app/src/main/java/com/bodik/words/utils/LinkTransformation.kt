package com.bodik.words.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import java.util.regex.Pattern

class LinkTransformation(val linkColor: Color) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = buildAnnotatedStringWithLinks(text.text, linkColor),
            offsetMapping = OffsetMapping.Identity
        )
    }
}

// Регулярное выражение для поиска ссылок
private val urlPattern = Pattern.compile(
    "(?:^|[\\s])((https?://|www\\.)[\\w-]+(?:\\.[\\w-]+)+(?:[\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?)",
    Pattern.CASE_INSENSITIVE
)

fun buildAnnotatedStringWithLinks(text: String, color: Color): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        val matcher = urlPattern.matcher(text)
        while (matcher.find()) {
            val url = matcher.group(1) ?: ""
            val fullUrl = if (url.startsWith("http")) url else "https://$url"

            // 1. Добавляем стиль (цвет и подчеркивание)
            addStyle(
                style = SpanStyle(
                    color = color,
                    textDecoration = TextDecoration.Underline
                ),
                start = matcher.start(1),
                end = matcher.end(1)
            )

            // 2. Добавляем аннотацию ссылки (чтобы по ней можно было кликнуть)
            addStringAnnotation(
                tag = "URL",
                annotation = fullUrl,
                start = matcher.start(1),
                end = matcher.end(1)
            )
        }
    }
}