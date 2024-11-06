package com.conan.mods.tasks.fabric.models

data class MenuItem (
    val name: String,
    val material: String,
    val nbt: String? = null,
    val amount: Int,
    val slot: Int
)