package com.conan.mods.presents.fabric.models

data class Present (
    val identifier: String,
    val item: PresentItem,
    val rewards: MutableList<String>
)

data class PresentItem (
    val name: String,
    val material: String,
    val nbt: String?,
)

data class PresentData (
    val identifier: String,
    val dimension: String,
    val pos: Long
)