package com.bodik.words.data

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: String,
    val name: String,                    // Слово/фраза
    val description: String,             // Перевод/значение
    val example: String? = null,         // Пример использования
    val isAudioCard: Boolean = false,    // Тип карточки
    val targetLanguage: String = "pl",   // Язык озвучивания
    val folderId: String? = null,        // ID папки (null = без папки)
)

// Языки для выбора
enum class Language(val code: String, val displayName: String) {
    PL("pl", "Польский"),
    EN("en", "Английский"),
    DE("de", "Немецкий"),
    FR("fr", "Французский"),
    ES("es", "Испанский"),
    RU("ru", "Русский")
}