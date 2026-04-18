package com.bodik.words.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.bodik.words.data.Item
import kotlinx.serialization.json.Json

class ItemManager(context: Context) {
    private val prefs = context.getSharedPreferences("words_app", MODE_PRIVATE)
    private val ITEMS_KEY = "all_items"

    fun getAllItems(): List<Item> {
        val json = prefs.getString(ITEMS_KEY, null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveItems(items: List<Item>) {
        val json = Json.encodeToString(items)
        prefs.edit { putString(ITEMS_KEY, json) }
    }

    fun saveUnassignedItems(unassignedItems: List<Item>) {
        val itemsInFolders = getAllItems().filter { it.folderId != null }
        saveItems(itemsInFolders + unassignedItems)
    }

    fun addItem(item: Item): Item {
        val items = getAllItems().toMutableList()
        items.add(item)
        saveItems(items)
        return item
    }

    fun deleteItem(itemId: String) {
        val items = getAllItems().filter { it.id != itemId }
        saveItems(items)
    }

    fun updateItem(updatedItem: Item) {
        val items = getAllItems().map {
            if (it.id == updatedItem.id) updatedItem else it
        }
        saveItems(items)
    }

    fun getItemsInFolder(folderId: String): List<Item> {
        return getAllItems().filter { it.folderId == folderId }
    }

    fun getUnassignedItems(): List<Item> {
        return getAllItems().filter { it.folderId == null }
    }

    fun moveItemToFolder(itemId: String, folderId: String?) {
        val items = getAllItems().map {
            if (it.id == itemId) it.copy(folderId = folderId) else it
        }
        saveItems(items)
    }

    fun reorderItemsInFolder(folderId: String, reorderedItems: List<Item>) {
        val allItems = getAllItems().toMutableList()
        allItems.removeAll { it.folderId == folderId }
        allItems.addAll(reorderedItems) // ✅ ок
        saveItems(allItems)
    }

    fun deleteAllItemsInFolder(folderId: String) {
        val items = getAllItems().filter { it.folderId != folderId }
        saveItems(items)
    }

    fun searchItems(query: String, folders: List<com.bodik.words.data.Folder>): List<Item> {
        if (query.isBlank()) return emptyList()
        return getAllItems().filter { item ->
            item.name.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
        }
    }
}