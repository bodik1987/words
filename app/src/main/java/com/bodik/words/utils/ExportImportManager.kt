package com.bodik.words.utils

import android.content.Context
import android.net.Uri
import com.bodik.words.data.Folder
import com.bodik.words.data.Item

object ExportImportManager {

    fun exportToString(folders: List<Folder>, items: List<Item>): String {
        val sb = StringBuilder()
        folders.forEach { folder ->
            sb.appendLine("#FOLDER:${folder.id}|${folder.name}")
        }
        items.forEach { item ->
            sb.appendLine(
                "#ITEM:${item.id}|${item.name}|${item.description}|" +
                        "${item.example ?: ""}|${item.isAudioCard}|${item.targetLanguage}|${item.folderId ?: ""}"
            )
        }
        return sb.toString()
    }

    fun importFromString(content: String): Pair<List<Folder>, List<Item>> {
        val folders = mutableListOf<Folder>()
        val items = mutableListOf<Item>()

        content.lines().forEach { line ->
            when {
                line.startsWith("#FOLDER:") -> {
                    val parts = line.removePrefix("#FOLDER:").split("|")
                    if (parts.size >= 2) {
                        folders.add(Folder(id = parts[0], name = parts[1]))
                    }
                }

                line.startsWith("#ITEM:") -> {
                    val parts = line.removePrefix("#ITEM:").split("|")
                    if (parts.size >= 6) {
                        items.add(
                            Item(
                                id = parts[0],
                                name = parts[1],
                                description = parts[2],
                                example = parts[3].takeIf { it.isNotBlank() },
                                isAudioCard = parts[4].toBooleanStrictOrNull() ?: false,
                                targetLanguage = parts[5],
                                folderId = parts.getOrNull(6)?.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }
            }
        }
        return Pair(folders, items)
    }

    fun writeToUri(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray())
        }
    }

    fun readFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: ""
    }
}