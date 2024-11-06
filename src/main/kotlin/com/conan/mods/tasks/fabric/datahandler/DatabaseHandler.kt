package com.conan.mods.tasks.fabric.datahandler

import com.conan.mods.tasks.fabric.enums.CobblemonEvent
import com.conan.mods.tasks.fabric.enums.Difficulty
import com.conan.mods.tasks.fabric.models.Task

interface DatabaseHandler {
    fun initializeTasksIfNotExists()
    fun fetchTasks() : MutableList<Task>
    fun fetchTask(difficulty: Difficulty) : Task
    fun refreshTask(difficulty: Difficulty)
    fun checkTaskAndUpdate(event: CobblemonEvent)
    fun updateTask(task: Task)
}