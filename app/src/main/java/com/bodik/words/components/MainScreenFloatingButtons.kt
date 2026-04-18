package com.bodik.words.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bodik.words.R
import com.bodik.words.ui.theme.Orange80


@Composable
fun MainScreenFloatingButtons(onAddFolderClick: () -> Unit = {}, onAddItemClick: () -> Unit = {}) {
    val listState = rememberLazyListState()
    val isFabVisible by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 || !listState.isScrollInProgress }
    }

    AnimatedVisibility(
        visible = isFabVisible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = onAddFolderClick,
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = Orange80,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.folder),
                    contentDescription = "Новая папка",
                    modifier = Modifier.size(22.dp),
                    tint = Orange80
                )
            }
            FloatingActionButton(
                onClick = onAddItemClick,
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.plus),
                    contentDescription = "Новое слово",
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}