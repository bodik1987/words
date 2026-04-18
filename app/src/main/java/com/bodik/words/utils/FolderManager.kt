package com.bodik.words.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.bodik.words.data.Folder
import kotlinx.serialization.json.Json

class FolderManager(private val context: Context) {  // Сразу сохраняем как private val
    private val prefs = context.getSharedPreferences("words_app", MODE_PRIVATE)
    private val FOLDERS_KEY = "folders"

    fun getFolders(): List<Folder> {
        val json = prefs.getString(FOLDERS_KEY, null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveFolders(folders: List<Folder>) {
        val json = Json.encodeToString(folders)
        prefs.edit { putString(FOLDERS_KEY, json) }
    }

    fun addFolder(name: String): Folder {
        val folders = getFolders().toMutableList()
        val newFolder = Folder(
            id = System.currentTimeMillis().toString(),
            name = name
        )
        folders.add(newFolder)
        saveFolders(folders)
        return newFolder
    }

    fun deleteFolder(folderId: String) {
        val folders = getFolders().filter { it.id != folderId }
        saveFolders(folders)
        // Исправлено: используем context напрямую
        val itemManager = ItemManager(context)  // context доступен как свойство класса
        itemManager.deleteAllItemsInFolder(folderId)
    }

    fun getFolderName(folderId: String): String {
        return getFolders().find { it.id == folderId }?.name ?: "Папка"
    }
}