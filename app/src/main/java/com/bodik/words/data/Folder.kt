package com.bodik.words.data

import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val id: String,
    val name: String
)