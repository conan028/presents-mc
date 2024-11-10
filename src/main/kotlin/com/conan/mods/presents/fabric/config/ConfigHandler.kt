package com.conan.mods.presents.fabric.config

import com.conan.mods.presents.fabric.datahandler.DatabaseHandlerSingleton
import com.conan.mods.presents.fabric.datahandler.JsonDBHandler
import com.conan.mods.presents.fabric.models.Present
import com.conan.mods.presents.fabric.models.PresentItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

object ConfigHandler {

    private val configFile = File("config/Presents/config.json")

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

    data class Config (
        val messages: ConfigurableMessages = ConfigurableMessages(),
        val presents: MutableList<Present> = mutableListOf(
            Present(
                "christmas",
                PresentItem(
                    "Christmas Present",
                    "minecraft:player_head",
                    "{display:{Name:'{\"text\":\"Present\",\"color\":\"gold\",\"underlined\":true,\"bold\":true,\"italic\":false}',Lore:['{\"text\":\"Custom Head ID: 69151\",\"color\":\"gray\",\"italic\":false}','{\"text\":\"www.minecraft-heads.com\",\"color\":\"blue\",\"italic\":false}']},SkullOwner:{Id:[I;-2002873815,1388858326,-1525192356,172033830],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmIxZWM3ZGM3NTMwNjFjYTE3NDQyNGVhNDVjZjk0OTBiMzljZDVkY2NhNDc3ZDEzOGE2MDNlNmJlNzU1ZWM3MiJ9fX0=\"}]}}}"
                ),
                mutableListOf(
                    "give %player% diamond 1",
                    "give %player% emerald 1"
                )
            )
        )
    )

    data class ConfigurableMessages(
        val prefix: String = "<red>[<dark_green>Presents<red>] <dark_gray>»",
        val alreadyFoundPresent: String = "%prefix% <red>You've already found this present!",
        val foundPresent: String = "You successfully found a present"
    )

}