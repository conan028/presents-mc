package com.conan.mods.tasks.fabric.datahandler

import com.cobblemon.mod.common.util.server
import com.conan.mods.tasks.fabric.CobbleTasks.config
import com.conan.mods.tasks.fabric.enums.CobblemonEvent
import com.conan.mods.tasks.fabric.enums.Difficulty
import com.conan.mods.tasks.fabric.models.Task
import com.conan.mods.tasks.fabric.util.PM
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration

class JsonDBHandler : DatabaseHandler {

    private val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
    private val tasksFile = File("config/CobbleTasks/storage/tasks.json")

    private var tasks: MutableList<Task>

    init {
        tasks = if (tasksFile.exists()) {
            gson.fromJson(tasksFile.reader(), object : TypeToken<MutableList<Task>>() {}.type)
        } else {
            tasksFile.parentFile.mkdirs()
            tasksFile.createNewFile()
            mutableListOf()
        }
    }

    override fun fetchTasks() : MutableList<Task> {
        return tasks
    }

    override fun checkTaskAndUpdate(event: CobblemonEvent) {
        val completedTasks = mutableListOf<Task>()

        tasks.forEach { task ->
            if (task.sort == event) {
                val updatedTask = task.copy(current = (task.current ?: 0) + 1)
                updateTask(updatedTask)

                if ((updatedTask.current ?: 0) >= updatedTask.goal) {
                    server()!!.execute {
                        server()?.playerManager?.playerList?.forEach { player ->
                            val rewards: Any? = if (config.config.randomReward) {
                                updatedTask.rewards.randomOrNull()
                            } else {
                                updatedTask.rewards
                            }

                            when (rewards) {
                                is String -> {
                                    PM.runCommand(rewards.replace("%player%", player.entityName))
                                }
                                is MutableList<*> -> {
                                    rewards.filterIsInstance<String>().forEach { reward ->
                                        PM.runCommand(reward.replace("%player%", player.entityName))
                                    }
                                }
                            }

                            PM.sendText(player, config.config.messages.taskCompleted.replace("%difficulty%", "${updatedTask.difficulty}"))
                        }
                    }

                    completedTasks.add(updatedTask)
                }
            }
        }

        if (completedTasks.isNotEmpty()) {
            completedTasks.forEach { task ->
                tasks.remove(task)
                tasks.add(generateTask(task.difficulty))
            }
            saveTasks()
        }
    }



    override fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.difficulty == task.difficulty }

        if (index != -1) {
            tasks[index] = task
        }

        saveTasks()
    }

    override fun initializeTasksIfNotExists() {
        if (tasks.isEmpty()) {
            Difficulty.values().forEach { task ->
                tasks.add(generateTask(task))
            }
            saveTasks()
        }
    }

    override fun fetchTask(difficulty: Difficulty): Task {
        val task = tasks.find { it.difficulty == difficulty }

        return task ?: generateTask(difficulty).also { tasks.add(it) }
    }

    override fun refreshTask(difficulty: Difficulty) {
        tasks.removeIf { it.difficulty == difficulty }
        generateTask(difficulty).let { tasks.add(it) }
        saveTasks()
    }

    private fun generateTask(difficulty: Difficulty): Task {
        val task = config.config.tasks.filter { it.difficulty == difficulty }.randomOrNull() ?: generateTask(difficulty).also { tasks.add(it) }
        val time = when (difficulty) {
            Difficulty.EASY -> config.config.timeConfig.easyTask
            Difficulty.MEDIUM -> config.config.timeConfig.mediumTask
            Difficulty.HARD -> config.config.timeConfig.hardTask
        }

        task.time = System.currentTimeMillis() + Duration.ofMinutes(time.toLong()).toMillis()
        return task
    }

    private fun saveTasks() {
        tasksFile.writeText(gson.toJson(tasks))
    }

}