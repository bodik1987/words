package com.bodik.words.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.ui.theme.Orange80
import sh.calvin.reorderable.ReorderableColumn

val RADIUS_OUTER = 16.dp
val RADIUS_INNER = 4.dp
val ITEM_SPACING = 2.dp

fun rowShape(index: Int, lastIndex: Int) = when {
    lastIndex == 0 -> RoundedCornerShape(RADIUS_OUTER)
    index == 0 -> RoundedCornerShape(
        topStart = RADIUS_OUTER, bottomStart = RADIUS_OUTER,
        topEnd = RADIUS_INNER, bottomEnd = RADIUS_INNER
    )

    index == lastIndex -> RoundedCornerShape(
        topStart = RADIUS_INNER, bottomStart = RADIUS_INNER,
        topEnd = RADIUS_OUTER, bottomEnd = RADIUS_OUTER
    )

    else -> RoundedCornerShape(RADIUS_INNER)
}

fun columnShape(index: Int, lastIndex: Int) = when {
    lastIndex == 0 -> RoundedCornerShape(RADIUS_OUTER)
    index == 0 -> RoundedCornerShape(
        topStart = RADIUS_OUTER, topEnd = RADIUS_OUTER,
        bottomStart = RADIUS_INNER, bottomEnd = RADIUS_INNER
    )

    index == lastIndex -> RoundedCornerShape(
        topStart = RADIUS_INNER, topEnd = RADIUS_INNER,
        bottomStart = RADIUS_OUTER, bottomEnd = RADIUS_OUTER
    )

    else -> RoundedCornerShape(RADIUS_INNER)
}

data class IslandListItem(
    val id: String,
    val label: String,
    val supportingText: String? = null,
    val example: String? = null,
    val leadingContent: (@Composable () -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null,
    val onClick: (String) -> Unit = {},
)

@Composable
fun IslandRow(
    items: List<IslandListItem>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val availableWidth = maxWidth - 32.dp - (ITEM_SPACING * (items.size - 1))
        val itemWidth = if (items.isNotEmpty())
            (availableWidth / items.size).coerceAtLeast(48.dp)
        else 0.dp

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ITEM_SPACING),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(items) { index, item ->
                ListItem(
                    headlineContent = {
                        Text(
                            item.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = MyFontFamily
                        )
                    },
                    supportingContent = item.supportingText?.let {
                        {
                            Text(
                                it,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = MyFontFamily
                            )
                        }
                    },
                    leadingContent = item.leadingContent,
                    trailingContent = item.trailingContent,
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    modifier = Modifier
                        .width(itemWidth)
                        .heightIn(min = 72.dp)
                        .clip(rowShape(index, items.lastIndex))
                )
            }
        }
    }
}

@Composable
fun IslandColumn(
    items: List<IslandListItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING)
    ) {
        items.forEachIndexed { index, item ->
            ListItem(
                headlineContent = {
                    Text(
                        text = item.label,
                        fontFamily = MyFontFamily,
                    )
                },
                supportingContent = {
                    Column {
                        if (!item.supportingText.isNullOrBlank()) {
                            Text(
                                text = item.supportingText,
                                fontFamily = MyFontFamily,
                            )
                        }
                        if (!item.example.isNullOrBlank()) {
                            Text(
                                text = item.example,
                                fontFamily = MyFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                leadingContent = item.leadingContent,
                trailingContent = item.trailingContent,
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.clip(columnShape(index, items.lastIndex))
            )
        }
    }
}

@Composable
fun ClickableIslandColumn(
    items: List<IslandListItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING)
    ) {
        items.forEachIndexed { index, item ->
            ListItem(
                headlineContent = {
                    Text(
                        item.label, fontFamily = MyFontFamily,
                    )
                },
                supportingContent = item.supportingText?.let {
                    {
                        Text(
                            it, fontFamily = MyFontFamily,
                        )
                    }
                },
                leadingContent = item.leadingContent,
                trailingContent = item.trailingContent,
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier
                    .clip(columnShape(index, items.lastIndex))
                    .clickable { item.onClick(item.id) },
            )
        }
    }
}

@Composable
fun ReorderableIslandColumn(
    items: List<IslandListItem>,
    onReorder: (List<IslandListItem>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    ReorderableColumn(
        list = items,
        onSettle = { fromIndex, toIndex ->
            val mutable = items.toMutableList()
            mutable.add(toIndex, mutable.removeAt(fromIndex))
            onReorder(mutable)
        },
        onMove = {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        },
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) { index, item, isDragging ->
        key(item.id) {
            ReorderableItem {
                val shape = if (isDragging) RoundedCornerShape(RADIUS_INNER) else columnShape(
                    index,
                    items.lastIndex
                )

                ListItem(
                    headlineContent = {
                        Text(
                            item.label,
                            fontWeight = FontWeight.Medium,
                            fontFamily = MyFontFamily,
                            fontSize = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    },
                    supportingContent = {
                        Column {
                            if (!item.supportingText.isNullOrBlank()) {
                                Text(
                                    text = item.supportingText,
                                    fontFamily = MyFontFamily,
                                    fontSize = 16.sp,
                                    maxLines = 6,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (!item.example.isNullOrBlank()) {
                                Text(
                                    text = item.example,
                                    fontFamily = MyFontFamily,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    },
                    leadingContent = item.leadingContent,
                    trailingContent = item.trailingContent,
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    modifier = Modifier
                        .longPressDraggableHandle(
                            onDragStarted = {
                                haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                            },
                            onDragStopped = {
                                haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                            },
                        )
                        .clip(shape)
                        .then(
                            if (isDragging) Modifier.border(
                                width = 2.dp,
                                color = Orange80,
                                shape = shape,
                            ) else Modifier
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            item.onClick(item.id)
                        }
                )
            }
        }
    }
}