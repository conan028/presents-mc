package com.conan.mods.pwarps.fabric.models

import java.util.*

data class Warp (
    val playerUUID: UUID,
    val playerName: String,
    val category: String,
    val name: String,
    val dimension: String,
    val coords: Long
)