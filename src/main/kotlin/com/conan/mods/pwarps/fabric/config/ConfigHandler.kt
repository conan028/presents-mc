package com.conan.mods.pwarps.fabric.config

import com.conan.mods.pwarps.fabric.datahandler.DatabaseHandlerSingleton
import com.conan.mods.pwarps.fabric.datahandler.JsonDBHandler
import com.conan.mods.pwarps.fabric.models.MenuItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

object ConfigHandler {

    private val configFile = File("config/player-warps/config.json")

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
        val economyConfig: EconomyConfig = EconomyConfig(),
        val teleportConfig: TeleportationConfig = TeleportationConfig()
    )

    data class MenuConfig(
        val title: String = "<gold>Player Warps",
        val warpItemLore: MutableList<String> = mutableListOf("<green>Owner: <gray>%player%"),
        val navigationItems: NavigationItems = NavigationItems(),
        val closeItem: MenuItem = MenuItem("<red>Close", "minecraft:barrier", null, mutableListOf(), 1)
    )

    data class NavigationItems(
        val backItem: MenuItem = MenuItem("<gold>Back", "minecraft:arrow", null, mutableListOf(), 1),
        val nextItem: MenuItem = MenuItem("<gold>Next", "minecraft:arrow", null, mutableListOf(), 1)
    )

    data class TeleportationConfig(
        val sound: String = "entity.villager.celebrate",
        val particle: String = "heart",
        val particleSpeed: Float = 1f,
        val particleCount: Int = 10
    )

    data class EconomyConfig(
        val takeMoney: Boolean = true,
        val price: Double = 750.0
    )

}