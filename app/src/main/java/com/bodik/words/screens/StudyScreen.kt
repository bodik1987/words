package com.bodik.words.screens

import android.annotation.SuppressLint
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bodik.words.R
import com.bodik.words.ui.components.ITEM_SPACING
import com.bodik.words.ui.components.RADIUS_INNER
import com.bodik.words.ui.components.RADIUS_OUTER
import com.bodik.words.ui.theme.Green
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.utils.ItemManager
import java.util.Locale

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    folderId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val itemManager = remember { ItemManager(context) }

    var queue by remember {
        val saved = itemManager.getSavedStudySession(folderId)
        val isContinuation = saved.isNotEmpty()

        val initialList = if (isContinuation) {
            // Показываем Toast только если сессия реально была найдена
            Toast.makeText(context, "Игра продолжена", Toast.LENGTH_SHORT).show()
            saved
        } else {
            itemManager.getItemsInFolder(folderId).filter { it.isAudioCard }
        }
        mutableStateOf(initialList.toMutableList())
    }

    var showAnswer by remember { mutableStateOf(false) }
    var direction by remember { mutableStateOf(1) } // 1 = вправо, -1 = влево

    val current = queue.firstOrNull()
    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.forLanguageTag("pl")
            }
        }
        ttsInstance
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) tts.stop()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            tts.stop()
            tts.shutdown()
        }
    }
    val total = queue.size

    val lottieCelebration by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.celebration))
    val progressLottieCelebration by animateLottieCompositionAsState(
        composition = lottieCelebration,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        speed = 1f
    )

    val lottieSpeaker by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.speaker))
    val progressLottieSpeaker by animateLottieCompositionAsState(
        composition = lottieSpeaker,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        speed = 0.6f
    )

    var showDeleteDialog by remember { mutableStateOf(false) }

    DisposableEffect(folderId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) tts.stop()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            tts.stop()
            tts.shutdown()

            // Если в очереди что-то осталось — сохраняем и уведомляем
            if (queue.isNotEmpty()) {
                itemManager.saveStudySession(folderId, queue)
                Toast.makeText(context, "Игра приостановлена", Toast.LENGTH_SHORT).show()
            } else {
                itemManager.clearStudySession(folderId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (total > 0) "Осталось $total" else "",
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    if (current != null) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Delete",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (current == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (lottieCelebration != null) {
                            LottieAnimation(
                                composition = lottieCelebration,
                                progress = { progressLottieCelebration },
                                modifier = Modifier.size(250.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Нет слов!",
                            fontFamily = MyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = onBack,
                            shape = RoundedCornerShape(RADIUS_OUTER),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Text(
                                "Вернуться в папку",
                                fontFamily = MyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            } else {
                AnimatedContent(
                    targetState = current,
                    transitionSpec = {
                        (slideInHorizontally { direction * it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -direction * it } + fadeOut())
                    },
                    modifier = Modifier.weight(1f)
                ) { item ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(RADIUS_OUTER),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                item.description?.let {
                                    Text(
                                        text = it,
                                        fontFamily = MyFontFamily,
                                        fontSize = 22.sp,
                                        lineHeight = 32.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (showAnswer) {
                                    Spacer(Modifier.height(20.dp))

                                    Spacer(
                                        Modifier
                                            .height(1.dp)
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background)
                                    )
                                    Spacer(Modifier.height(20.dp))

                                    Text(
                                        text = item.name,
                                        fontFamily = MyFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 22.sp,
                                        lineHeight = 32.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (!item.example.isNullOrBlank()) {
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            text = item.example,
                                            fontFamily = MyFontFamily,
                                            fontSize = 14.sp,
                                            lineHeight = 24.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    }

                                    if (showAnswer && current.isAudioCard) {
                                        Spacer(Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                tts.language =
                                                    Locale.forLanguageTag(current.targetLanguage)
                                                tts.speak(
                                                    current.name,
                                                    TextToSpeech.QUEUE_FLUSH,
                                                    null,
                                                    null
                                                )
                                            },
                                            modifier = Modifier.size(52.dp),
                                            shape = CircleShape,
                                            contentPadding = PaddingValues(0.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.onSecondary,
                                                contentColor = MaterialTheme.colorScheme.onBackground
                                            )
                                        ) {
                                            if (lottieSpeaker != null) {
                                                LottieAnimation(
                                                    composition = lottieSpeaker,
                                                    progress = { progressLottieSpeaker },
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }
                                        }

                                    }

                                }
                            }
                        }
                    }
                }


                Spacer(Modifier.height(16.dp))

                if (!showAnswer) {
                    Button(
                        onClick = {
                            showAnswer = true
                            if (current.isAudioCard) {
                                tts.language = Locale.forLanguageTag(current.targetLanguage)
                                tts.speak(current.name, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(RADIUS_OUTER),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(
                            "Показать",
                            fontFamily = MyFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ITEM_SPACING)
                    ) {
                        // Не помню — в конец очереди
                        Button(
                            onClick = {
                                tts.stop()
                                direction = 1
                                val item = queue.removeAt(0)
                                queue = (queue + item).toMutableList()
                                showAnswer = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(
                                topStart = RADIUS_OUTER,
                                topEnd = RADIUS_INNER,
                                bottomStart = RADIUS_OUTER,
                                bottomEnd = RADIUS_INNER
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                "Не помню",
                                fontFamily = MyFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        // Помню — убираем из очереди
                        Button(
                            onClick = {
                                tts.stop()
                                direction = -1
                                queue = queue.drop(1).toMutableList()
                                showAnswer = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(
                                topStart = RADIUS_INNER,
                                topEnd = RADIUS_OUTER,
                                bottomStart = RADIUS_INNER,
                                bottomEnd = RADIUS_OUTER
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "Помню",
                                fontFamily = MyFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
    if (showDeleteDialog && current != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            title = {
                Text(
                    "Удалить карточку?",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    "Слово будет полностью удалено из приложения. Вы уверены?",
                    fontFamily = MyFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // 1. Удаляем из хранилища
                    itemManager.deleteItem(current.id)
                    // 2. Удаляем из текущей очереди экрана
                    queue = queue.drop(1).toMutableList()
                    // 3. Сбрасываем состояния
                    showAnswer = false
                    showDeleteDialog = false
                    tts.stop()
                }) {
                    Text(
                        "Удалить",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        "Отмена",
                        fontFamily = MyFontFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

