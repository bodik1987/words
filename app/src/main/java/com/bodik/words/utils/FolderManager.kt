package com.bodik.words.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.bodik.words.data.Folder
import kotlinx.serialization.json.Json

class FolderManager(context: Context) {
    private val prefs = context.getSharedPreferences("words_app", MODE_PRIVATE)

    fun getFolders(): List<Folder> {
        val json = prefs.getString("folders", null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveFolders(folders: List<Folder>) {
        val json = Json.encodeToString(folders)
        prefs.edit { putString("folders", json) }
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
    }
}