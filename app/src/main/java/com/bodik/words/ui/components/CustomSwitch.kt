package com.bodik.words.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bodik.words.ui.theme.Green

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 74.dp,
    height: Dp = 34.dp
) {
    val thumbWidth = 42.dp
    val thumbHeight = height - 6.dp
    val padding = 3.dp

    val animOffset by animateDpAsState(
        targetValue = if (checked) width - thumbWidth - padding else padding,
        animationSpec = tween(durationMillis = 100)
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(
                elevation = 6.dp,
                shape = (RoundedCornerShape(height)),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(height))
            .background(if (checked) Green else Color(0xFFC5C5C7))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = animOffset)
                .width(thumbWidth)
                .height(thumbHeight)
                .shadow(
                    elevation = 6.dp,
                    shape = (RoundedCornerShape(50)),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        )
    }
}