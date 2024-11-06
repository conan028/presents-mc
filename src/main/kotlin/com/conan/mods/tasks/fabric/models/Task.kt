package com.conan.mods.tasks.fabric.models

import com.conan.mods.tasks.fabric.enums.CobblemonEvent
import com.conan.mods.tasks.fabric.enums.Difficulty

data class Task (
    val difficulty: Difficulty,
    val sort: CobblemonEvent,
    var time: Long?,
    var current: Int?,
    val goal: Int,
    val rewards: MutableList<String>
)