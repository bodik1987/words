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
import androidx.compose.material3.DatePickerDefaults
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
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.bodik.words.components.ReadingDialog
import com.bodik.words.data.Item
import com.bodik.words.data.Language
import com.bodik.words.ui.components.CustomSwitch
import com.bodik.words.ui.components.RADIUS_OUTER
import com.bodik.words.ui.components.WordTextField
import com.bodik.words.ui.theme.Blue80
import com.bodik.words.ui.theme.MyFontFamily
import com.bodik.words.utils.ItemManager
import com.bodik.words.utils.NotificationReceiver
import com.bodik.words.utils.formatReminderDate
import java.util.Calendar

// ─── Переиспользуемые компоненты ────────────────────────────────────────────

@Composable
private fun SectionDivider() = Spacer(
    Modifier
        .height(1.dp)
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
)

@Composable
private fun SettingsRow(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ) else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            fontFamily = MyFontFamily,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f)
        )
        trailingContent?.invoke()
    }
}

// ─── Основной экран ──────────────────────────────────────────────────────────

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    editingItem: Item? = null,
    folderId: String? = null,
    onBack: () -> Unit,
    onItemSaved: () -> Unit = {},
    onItemDeleted: (() -> Unit)? = null,
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
    var showReadingDialog by remember { mutableStateOf(false) }
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

    val hasChanges = if (isEditMode) {
        editingItem.let { item ->
            name != item.name ||
                    description != (item.description ?: "") ||
                    example != (item.example ?: "") ||
                    isAudioCard != item.isAudioCard
        }
    } else {
        name.isNotBlank() || description.isNotBlank() || example.isNotBlank()
    }

    BackHandler(enabled = hasChanges) { showDiscardDialog = true }

    val saveItem = {
        if (name.isNotBlank()) {
            if (isEditMode) {
                val updatedItem = editingItem.copy(
                    name = name,
                    description = description.takeIf { it.isNotBlank() },
                    example = example.takeIf { it.isNotBlank() },
                    isAudioCard = isAudioCard,
                    targetLanguage = if (isAudioCard) selectedLanguage.code else "pl",
                    reminderTime = reminderTime
                )
                itemManager.updateItem(updatedItem)
                handleNotificationScheduling(context, updatedItem)
            } else {
                val newItem = Item(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    description = description.takeIf { it.isNotBlank() },
                    example = example.takeIf { it.isNotBlank() },
                    isAudioCard = isAudioCard,
                    targetLanguage = if (isAudioCard) selectedLanguage.code else "pl",
                    folderId = folderId,
                    reminderTime = reminderTime
                )
                itemManager.addItem(newItem)
                handleNotificationScheduling(context, newItem)
            }
            onBack()
        }
    }

    // ─── Диалоги ────────────────────────────────────────────────────────────

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(28.dp),
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
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    "Отменить изменения?",
                    fontFamily = MyFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    "Несохранённые изменения будут потеряны.",
                    fontFamily = MyFontFamily
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { showDiscardDialog = false; onBack() }) {
                        Text(
                            "Отменить изменения",
                            color = MaterialTheme.colorScheme.error,
                            fontFamily = MyFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text(
                            "Продолжить",
                            fontFamily = MyFontFamily,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        )
    }

    if (showTimeDialog) {
        var showTimePicker by remember { mutableStateOf(false) }
        var isTimeSelected by remember { mutableStateOf(false) }
        var selectedTimeDialogText by remember { mutableStateOf("Укажите время") }

        DatePickerDialog(
            onDismissRequest = { showTimeDialog = false },
            confirmButton = {},
            dismissButton = {},
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            Column {
                DatePicker(
                    state = datePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        selectedDayContainerColor = Blue80,
                        selectedDayContentColor = Color.White,
                        todayDateBorderColor = Blue80,
                        todayContentColor = Blue80,
                        navigationContentColor = MaterialTheme.colorScheme.onSurface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        headlineContentColor = MaterialTheme.colorScheme.onSurface
                    )
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
                    headlineContent = { Text(selectedTimeDialogText, fontFamily = MyFontFamily) },
                    trailingContent = {
                        if (isTimeSelected) {
                            IconButton(onClick = {
                                selectedTimeDialogText = "Укажите время"; isTimeSelected = false
                            }) {
                                Icon(painterResource(R.drawable.x), null, Modifier.size(20.dp))
                            }
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
                    TextButton(onClick = { showTimeDialog = false }) {
                        Text(
                            "Отмена",
                            fontFamily = MyFontFamily,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { calendar.timeInMillis = it }
                        if (isTimeSelected) {
                            calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            calendar.set(Calendar.MINUTE, timePickerState.minute)
                            calendar.set(Calendar.SECOND, 0)
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
                    }) {
                        Text(
                            "Готово",
                            fontFamily = MyFontFamily,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                confirmButton = {
                    TextButton(onClick = {
                        selectedTimeDialogText =
                            String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        isTimeSelected = true
                        showTimePicker = false
                    }) {
                        Text(
                            "OK",
                            fontFamily = MyFontFamily,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(
                            "Отмена",
                            fontFamily = MyFontFamily,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                text = {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MaterialTheme.colorScheme.background,
                            clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.background,
                            selectorColor = Blue80,
                            timeSelectorSelectedContainerColor = Blue80.copy(alpha = 0.2f),
                            timeSelectorSelectedContentColor = Blue80
                        )
                    )
                }
            )
        }
    }

    // ─── Scaffold ────────────────────────────────────────────────────────────

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = {
                    if (!isEditMode) Text(
                        "Добавить",
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
                        Icon(painterResource(R.drawable.back), contentDescription = "Назад")
                    }
                },
                actions = {
                    Button(
                        onClick = { saveItem() },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(44.dp),
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue80,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Black.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            "Сохранить",
                            fontFamily = MyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Блок текстовых полей ────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(RADIUS_OUTER),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    WordTextField(
                        value = name, onValueChange = { name = it },
                        placeholder = "Заголовок", fontSize = 22.sp,
                        maxLines = 6, readOnly = false,
                        fontFamily = MyFontFamily, fontWeight = FontWeight.Medium
                    )
                    WordTextField(
                        value = description, onValueChange = { description = it },
                        placeholder = "Описание", fontSize = 18.sp,
                        maxLines = 30, readOnly = false,
                        fontFamily = MyFontFamily, isLinkHighlightingEnabled = true
                    )
                    SectionDivider()
                    WordTextField(
                        value = example, onValueChange = { example = it },
                        placeholder = "Примечание", fontSize = 18.sp,
                        maxLines = 6, readOnly = false, fontFamily = MyFontFamily
                    )
                }
            }

            // ─── Блок настроек ───────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(RADIUS_OUTER),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Напоминание
                    SettingsRow(
                        iconRes = R.drawable.clock,
                        label = reminderTime?.let { formatReminderDate(it) }
                            ?: "Добавить дату и время",
                        onClick = { showTimeDialog = true },
                        trailingContent = {
                            if (reminderTime != null) {
                                IconButton(
                                    modifier = Modifier.size(24.dp),
                                    onClick = { reminderTime = null }) {
                                    Icon(
                                        painterResource(R.drawable.x), null, Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    )

                    SectionDivider()

                    // Аудио карточка
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAudioCard) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showLanguageMenu = true }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painterResource(R.drawable.volume), null,
                                        Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        selectedLanguage.displayName,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showLanguageMenu,
                                    shape = RoundedCornerShape(RADIUS_OUTER),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    onDismissRequest = { showLanguageMenu = false }
                                ) {
                                    Language.entries.forEach { language ->
                                        DropdownMenuItem(
                                            text = { Text(language.displayName, fontSize = 16.sp) },
                                            onClick = {
                                                selectedLanguage = language; showLanguageMenu =
                                                false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painterResource(R.drawable.volume), null,
                                    Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    "Аудио карточка", fontFamily = MyFontFamily,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }
                        CustomSwitch(checked = isAudioCard, onCheckedChange = { isAudioCard = it })
                    }

                    // Действия режима редактирования
                    if (isEditMode) {
                        SectionDivider()
                        SettingsRow(
                            R.drawable.folder,
                            "Переместить",
                            onClick = { showMoveBottomSheet = true })
                        SectionDivider()
                        SettingsRow(
                            R.drawable.reading,
                            "Режим чтения",
                            onClick = { showReadingDialog = true })
                        SectionDivider()
                        SettingsRow(
                            R.drawable.delete,
                            "Удалить",
                            onClick = { showDeleteDialog = true })
                    }
                }
            }

            Spacer(Modifier.height(0.dp)) // отступ снизу для scroll
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

    if (showReadingDialog && editingItem != null) {
        ReadingDialog(
            name = name,
            description = description,
            example = example.takeIf { it.isNotBlank() },
            onDismiss = { showReadingDialog = false }
        )
    }
}

// ─── Уведомления ─────────────────────────────────────────────────────────────

private fun handleNotificationScheduling(context: Context, item: Item) {
    val id = item.id.hashCode()
    cancelNotification(context, id)
    if (item.reminderTime != null && item.reminderTime > System.currentTimeMillis()) {
        scheduleNotification(context, item.reminderTime, item.name, id)
    }
}

fun scheduleNotification(context: Context, timeInMillis: Long, message: String, id: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("EXTRA_MESSAGE", message)
        putExtra("EXTRA_ID", id)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, id, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
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
        putExtra("EXTRA_ID", id)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, id, intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
    )
    pendingIntent?.let { alarmManager.cancel(it); it.cancel() }
}