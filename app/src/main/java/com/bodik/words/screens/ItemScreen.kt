package com.bodik.words.screens

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodik.words.R
import com.bodik.words.components.BottomSheets.MoveItemBottomSheet
import com.bodik.words.data.Item
import com.bodik.words.data.Language
import com.bodik.words.ui.components.CustomSwitch
import com.bodik.words.ui.components.WordTextField
import com.bodik.words.ui.theme.Blue80
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.ui.theme.Orange80
import com.bodik.words.utils.ItemManager
import com.bodik.words.utils.NotificationReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    editingItem: Item? = null,
    folderId: String? = null,
    onBack: () -> Unit,
    onItemSaved: () -> Unit = {},
    onItemDeleted: (() -> Unit)? = null,
    onMoveItem: ((String, String?) -> Unit)? = null
) {
    val context = LocalContext.current
    val itemManager = remember { ItemManager(context) }
    val isEditMode = editingItem != null

    var name by remember { mutableStateOf(editingItem?.name ?: "") }
    var description by remember { mutableStateOf(editingItem?.description ?: "") }
    var example by remember { mutableStateOf(editingItem?.example ?: "") }
    var isAudioCard by remember { mutableStateOf(editingItem?.isAudioCard ?: false) }
    var selectedLanguage by remember {
        mutableStateOf(
            Language.entries.find { it.code == (editingItem?.targetLanguage ?: "pl") }
                ?: Language.PL
        )
    }
    var showMoveBottomSheet by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    var reminderTime by remember { mutableStateOf(editingItem?.reminderTime) }
    var showTimeDialog by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    // Если в режиме редактирования, можно оставить заголовок пустым или написать "Правка"
    val titleText = if (isEditMode) "" else "Добавить"

    val hasChanges = if (isEditMode) {
        name != (editingItem?.name ?: "") ||
                description != (editingItem?.description ?: "") ||
                example != (editingItem?.example ?: "") ||
                isAudioCard != (editingItem?.isAudioCard ?: false)
    } else {
        name.isNotBlank() || description.isNotBlank() || example.isNotBlank()
    }

    BackHandler(enabled = hasChanges) {
        showDiscardDialog = true
    }

    val saveItem = {
        if (name.isNotBlank() && description.isNotBlank()) {
            val finalReminderTime = reminderTime // берем из нашего нового состояния

            if (isEditMode) {
                val updatedItem = editingItem.copy(
                    name = name,
                    description = description,
                    example = example.takeIf { it.isNotBlank() },
                    isAudioCard = isAudioCard,
                    targetLanguage = if (isAudioCard) selectedLanguage.code else "pl",
                    reminderTime = finalReminderTime // Сохраняем время
                )
                itemManager.updateItem(updatedItem)

                // Планируем или отменяем уведомление
                handleNotificationScheduling(context, updatedItem)
            } else {
                val newItem = Item(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    description = description,
                    example = example.takeIf { it.isNotBlank() },
                    isAudioCard = isAudioCard,
                    targetLanguage = if (isAudioCard) selectedLanguage.code else "pl",
                    folderId = folderId,
                    reminderTime = finalReminderTime // Сохраняем время
                )
                itemManager.addItem(newItem)

                // Планируем уведомление
                handleNotificationScheduling(context, newItem)
            }
            onBack()
        }
    }

    //region Dialogs
    if (showDeleteDialog) {
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
                    "Это действие нельзя будет отменить. Вы уверены?",
                    fontFamily = MyFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    if (isEditMode) {
                        itemManager.deleteItem(editingItem.id)
                        onItemDeleted?.invoke()
                        onBack()
                    }
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

    if (showTimeDialog) {
        var showTimePicker by remember { mutableStateOf(false) }
        var isTimeSelected by remember { mutableStateOf(false) }
        var selectedTimeDialogText by remember { mutableStateOf("Укажите время") }

        DatePickerDialog(
            onDismissRequest = { showTimeDialog = false },
            confirmButton = {},
            dismissButton = {}
        ) {
            Column {
                DatePicker(
                    state = datePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false
                )
                HorizontalDivider()
                ListItem(
                    modifier = Modifier.clickable { showTimePicker = true },
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.clock),
                            null,
                            Modifier.size(24.dp)
                        )
                    },
                    headlineContent = { Text(selectedTimeDialogText) },
                    trailingContent = {
                        if (isTimeSelected) {
                            IconButton(onClick = {
                                selectedTimeDialogText = "Укажите время"
                                isTimeSelected = false
                            }) { Icon(painterResource(R.drawable.x), null, Modifier.size(20.dp)) }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showTimeDialog = false }) { Text("Отмена") }
                    TextButton(onClick = {
                        // 1. Берем дату из календаря
                        datePickerState.selectedDateMillis?.let { calendar.timeInMillis = it }

                        if (isTimeSelected) {
                            // 2. Берем время из таймпикера
                            calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            calendar.set(Calendar.MINUTE, timePickerState.minute)
                            calendar.set(Calendar.SECOND, 0)

                            // --- ПРОВЕРКА НА ПРОШЛОЕ ВРЕМЯ ---
                            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                                Toast.makeText(
                                    context,
                                    "Нельзя выбрать время в прошлом!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                reminderTime = calendar.timeInMillis
                                showTimeDialog = false
                            }
                        } else {
                            showTimeDialog = false
                        }
                    }) { Text("Готово") }
                }
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedTimeDialogText =
                            String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        isTimeSelected = true
                        showTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showTimePicker = false
                    }) { Text("Отмена") }
                },
                text = { TimePicker(state = timePickerState) }
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            title = {
                Text(
                    "Отменить изменения?",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = { Text("Несохранённые изменения будут потеряны.", fontFamily = MyFontFamily) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onBack()
                }) {
                    Text(
                        "Закрыть",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(
                        "Продолжить",
                        fontFamily = MyFontFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
    //endregion

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = titleText,
                        fontFamily = MyFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    Button(
                        onClick = { if (hasChanges) showDiscardDialog = true else onBack() },
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Удалить",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    if (editingItem != null) {
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = { showMoveBottomSheet = true },
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.folder),
                                contentDescription = "Move",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { saveItem() },
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        enabled = name.isNotBlank() && description.isNotBlank(),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue80,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Black.copy(alpha = 0.1f)
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = "Save",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        WordTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Слово/фраза",
                            fontSize = 20.sp,
                            maxLines = 6,
                            readOnly = false,
                            fontFamily = MyFontFamily,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(
                            Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                        )

                        WordTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Перевод/значение",
                            fontSize = 18.sp,
                            maxLines = 30,
                            readOnly = false,
                            fontFamily = MyFontFamily,
                            isLinkHighlightingEnabled = true
                        )

                        Spacer(
                            Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                        )

                        WordTextField(
                            value = example,
                            onValueChange = { example = it },
                            placeholder = "Пример (необязательно)",
                            fontSize = 16.sp,
                            maxLines = 6,
                            readOnly = false,
                            fontFamily = MyFontFamily,
                        )

                        Spacer(
                            Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                        )

                        val formattedDate = reminderTime?.let {
                            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(it))
                        } ?: "Добавить дату и время"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showTimeDialog = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(id = R.drawable.clock),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = formattedDate,
                                fontFamily = MyFontFamily
                            )
                            Spacer(Modifier.width(16.dp))
                            if (reminderTime != null) {
                                IconButton(
                                    modifier = Modifier.size(24.dp),
                                    onClick = { reminderTime = null }) {
                                    Icon(
                                        painterResource(id = R.drawable.x),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        Spacer(
                            Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAudioCard) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showLanguageMenu = true }) {
                                    Text(
                                        text = selectedLanguage.displayName,
                                        fontFamily = MyFontFamily,
                                        fontSize = 18.sp,
                                        color = Orange80
                                    )
                                    DropdownMenu(
                                        expanded = showLanguageMenu,
                                        onDismissRequest = { showLanguageMenu = false }) {
                                        Language.entries.forEach { language ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        language.displayName,
                                                        fontFamily = MyFontFamily
                                                    )
                                                },
                                                onClick = {
                                                    selectedLanguage = language; showLanguageMenu =
                                                    false
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Озвучивать",
                                    fontFamily = MyFontFamily,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            CustomSwitch(
                                checked = isAudioCard,
                                onCheckedChange = { isAudioCard = it })
                        }
                    }
                }
            }
        }
    }

    if (showMoveBottomSheet && editingItem != null) {
        MoveItemBottomSheet(
            onDismiss = { showMoveBottomSheet = false },
            itemId = editingItem.id,
            currentFolderId = editingItem.folderId,
            onMove = { itemId, newFolderId ->
                itemManager.moveItemToFolder(itemId, newFolderId)
                showMoveBottomSheet = false
                onItemSaved()
            }
        )
    }
}

private fun handleNotificationScheduling(context: Context, item: Item) {
    val id = item.id.hashCode()

    // Сначала отменяем старое уведомление (если было)
    cancelNotification(context, id)

    if (item.reminderTime != null && item.reminderTime > System.currentTimeMillis()) {
        scheduleNotification(
            context = context,
            timeInMillis = item.reminderTime,
            message = item.name,
            id = id
        )
    }
}

fun scheduleNotification(context: Context, timeInMillis: Long, message: String, id: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("EXTRA_MESSAGE", message)
        putExtra("EXTRA_ID", id)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        return
    }

    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
}

fun cancelNotification(context: Context, id: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("EXTRA_ID", id) // Важно: добавляем тот же ключ
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, id, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
    )
    pendingIntent?.let {
        alarmManager.cancel(it)
        it.cancel()
    }
}