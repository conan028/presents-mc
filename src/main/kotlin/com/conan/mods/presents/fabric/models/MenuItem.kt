package com.conan.mods.presents.fabric.models

data class MenuItem (
    val name: String,
    val material: String,
    val lore: MutableList<String>,
    val nbt: String?,
    val amount: Int,
)