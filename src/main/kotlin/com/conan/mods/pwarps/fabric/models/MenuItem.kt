package com.conan.mods.pwarps.fabric.models

data class MenuItem (
    val name: String,
    val material: String,
    val nbt: String? = null,
    val lore: MutableList<String?>,
    val amount: Int
)