package com.conan.mods.pwarps.fabric.datahandler

import com.conan.mods.pwarps.fabric.models.Warp
import java.util.*

interface DatabaseHandler {
    fun initializeUserIfNotExists(uuid: UUID)
    fun getWarpData(): MutableList<Warp>
    fun addWarp(warp: Warp)
    fun delWarp(warp: Warp)
}