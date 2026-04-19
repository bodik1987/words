package com.bodik.words.screens

import android.annotation.SuppressLint
import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
        mutableStateOf(itemManager.getItemsInFolder(folderId).toMutableList())
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (total > 0) "Осталось: $total" else "Готово!",
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Назад"
                        )
                    }
                }
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
                            text = "Все слова изучены!",
                            fontFamily = MyFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = onBack,
                            shape = RoundedCornerShape(34.dp),
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
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = item.description,
                                    fontFamily = MyFontFamily,
                                    fontSize = 22.sp,
                                    lineHeight = 32.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )

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
                                        textAlign = TextAlign.Center
                                    )

                                    if (!item.example.isNullOrBlank()) {
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            text = item.example,
                                            fontFamily = MyFontFamily,
                                            fontSize = 14.sp,
                                            lineHeight = 24.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onBackground.copy(
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
                                                containerColor = MaterialTheme.colorScheme.background,
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
                        shape = RoundedCornerShape(34.dp),
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            shape = RoundedCornerShape(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                "Не помню",
                                fontFamily = MyFontFamily,
                                fontWeight = FontWeight.Medium,
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
                            shape = RoundedCornerShape(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF62BE63),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "Помню",
                                fontFamily = MyFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}