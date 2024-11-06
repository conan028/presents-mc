package com.conan.mods.tasks.fabric.config

import com.conan.mods.tasks.fabric.datahandler.DatabaseHandlerSingleton
import com.conan.mods.tasks.fabric.datahandler.JsonDBHandler
import com.conan.mods.tasks.fabric.enums.CobblemonEvent
import com.conan.mods.tasks.fabric.enums.Difficulty
import com.conan.mods.tasks.fabric.models.MenuItem
import com.conan.mods.tasks.fabric.models.Task
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

object ConfigHandler {

    private val configFile = File("config/CobbleTasks/config.json")

    private val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    var config = Config()

    init {
        if (configFile.exists()) {
            val configString = configFile.readText()
            config = gson.fromJson(configString, Config::class.java)
            DatabaseHandlerSingleton.dbHandler = JsonDBHandler()
        } else {
            configFile.parentFile.mkdirs()
            configFile.createNewFile()
            val configString = gson.toJson(config)
            configFile.writeText(configString)
        }
    }

    fun reload() {
        val configString = configFile.readText()
        val updatedConfig = gson.fromJson(configString, Config::class.java)
        config = updatedConfig

        val updatedConfigString = gson.toJson(config)
        configFile.writeText(updatedConfigString)
    }

    data class Config(
        val menuConfig: MenuConfig = MenuConfig(),
        val timeConfig: TimeConfig = TimeConfig(),
        val messages: ConfigurableMessages = ConfigurableMessages(),
        val randomReward: Boolean = true,
        val tasks: MutableList<Task> = mutableListOf(
            Task(
                Difficulty.EASY,
                CobblemonEvent.CATCH,
                null,
                null,
                100,
                mutableListOf("give %player% iron_ingot 16")
            ),
            Task(
                Difficulty.MEDIUM,
                CobblemonEvent.CATCH,
                null,
                null,
                150,
                mutableListOf("give %player% iron_ingot 32")
            ),
            Task(
                Difficulty.HARD,
                CobblemonEvent.CATCH,
                null,
                null,
                250,
                mutableListOf("give %player% iron_ingot 64")
            )
        )
    )

    data class TimeConfig(
        val easyTask: Int = 15,
        val mediumTask: Int = 30,
        val hardTask: Int = 60,
    )

    data class ConfigurableMessages(
        val taskCompleted: String = "<green>Cobble Tasks<dark_gray> >> <gray> The <gold>%difficulty% Community Goal <gray>has been completed!"
    )

    data class MenuConfig(
        val title: String = "<gold>Cobble Tasks",
        val easyTaskItem: MenuItem = MenuItem("<green>Easy Task", "minecraft:green_concrete", null, 1, 11),
        val mediumTaskItem: MenuItem = MenuItem("<gold>Medium Task", "minecraft:orange_concrete", null, 1, 13),
        val hardTaskItem: MenuItem = MenuItem("<red>Hard Task", "minecraft:red_concrete", null, 1, 15),
        val closeItem: MenuItem = MenuItem("<red>Close", "minecraft:barrier", null, 1, 22)
    )

}