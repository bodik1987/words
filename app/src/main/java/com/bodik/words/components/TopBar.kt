package com.bodik.words.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodik.words.R
import com.bodik.words.ui.theme.MyFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onMenuClick: () -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {}
) {
    TopAppBar(
        title = {},
        actions = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Блокнот",
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = MyFontFamily,
                    fontSize = 24.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(20.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontFamily = MyFontFamily,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .height(44.dp)

                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .padding(start = 16.dp, end = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Поиск",
                                    fontFamily = MyFontFamily,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    innerTextField()
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchQueryChange("") },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.x),
                                            contentDescription = "Очистить",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onMenuClick,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ellipsis_vertical),
                        contentDescription = "Menu",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    )
}