package com.conan.mods.pwarps.fabric.datahandler

import com.cobblemon.mod.common.util.server
import com.conan.mods.pwarps.fabric.models.Warp
import com.google.gson.reflect.TypeToken
import com.google.gson.GsonBuilder
import java.io.File
import java.util.*

class JsonDBHandler : DatabaseHandler {

    private val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
    private val playerFile = File("config/player-warps/player_data.json")
    private val warpsFile = File("config/player-warps/warps.json")

    private val players: MutableMap<UUID, String>
    private val warps: MutableList<Warp>
    init {
        players = if (playerFile.exists()) {
            gson.fromJson(playerFile.reader(), object : TypeToken<MutableMap<UUID, String>>() {}.type)
        } else {
            playerFile.parentFile.mkdirs()
            playerFile.createNewFile()
            mutableMapOf()
        }

        warps = if (warpsFile.exists()) {
            gson.fromJson(warpsFile.reader(), object : TypeToken<MutableList<Warp>>() {}.type)
        } else {
            warpsFile.parentFile.mkdirs()
            warpsFile.createNewFile()
            mutableListOf()
        }
    }

    override fun initializeUserIfNotExists(uuid: UUID) {
        players.putIfAbsent(uuid, server()!!.playerManager.getPlayer(uuid)?.entityName ?: "Unknown")
        savePlayers()
    }

    override fun getWarpData(): MutableList<Warp> {
        return warps
    }

    override fun addWarp(warp: Warp) {
        warps.add(warp)
        saveWarps()
    }

    override fun delWarp(warp: Warp) {
        warps.remove(warp)
        saveWarps()
    }

    private fun saveWarps() {
        warpsFile.writeText(gson.toJson(warps))
    }

    private fun savePlayers() {
        playerFile.writeText(gson.toJson(players))
    }
}