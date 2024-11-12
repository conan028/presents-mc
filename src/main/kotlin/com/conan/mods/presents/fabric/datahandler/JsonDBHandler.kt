package com.conan.mods.presents.fabric.datahandler

import com.conan.mods.presents.fabric.models.PresentData
import com.conan.mods.presents.fabric.util.ConfigFileHandler
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class JsonDBHandler : DatabaseHandler {

    private val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
    private val playersFile = File("config/Presents/storage/player_data.json")
    private val presentsFile = File("config/Presents/storage/present_data.json")

    private var players: MutableMap<String, MutableSet<Long>>
    private var presents: MutableSet<PresentData>

    init {
        players = ConfigFileHandler.loadOrInitializeFile(
            playersFile,
            object : TypeToken<MutableMap<String, MutableSet<Long>>>() {}.type,
            mutableMapOf()
        )

        presents = ConfigFileHandler.loadOrInitializeFile(
            presentsFile,
            object : TypeToken<MutableSet<PresentData>>() {}.type,
            mutableSetOf()
        )
    }

    private fun initializePlayerIfNotExists(uuid: String) {
        players.putIfAbsent(uuid, mutableSetOf())
        savePlayers()
    }

    override fun addPresentToPlayer(uuid: String, pos: Long) {
        initializePlayerIfNotExists(uuid)
        players[uuid]?.add(pos)
        savePlayers()
    }

    override fun fetchFoundPresents(uuid: String): MutableSet<Long> {
        initializePlayerIfNotExists(uuid)
        return players.getOrPut(uuid) { mutableSetOf() }
    }

    override fun getPresents(): MutableSet<PresentData> {
        return presents
    }

    override fun getPresentByLong(pos: Long): PresentData? {
        return presents.find { it.pos == pos }
    }

    override fun addPresent(data: PresentData) {
        presents.add(data)
        savePresents()
    }

    override fun removePrevent(pos: Long) {
        presents.removeIf { it.pos == pos }
        savePresents()
    }

    private fun savePlayers() {
        playersFile.writeText(gson.toJson(players))
    }

    private fun savePresents() {
        presentsFile.writeText(gson.toJson(presents))
    }

}