package com.conan.mods.presents.fabric.datahandler

import com.conan.mods.presents.fabric.models.PresentData

interface DatabaseHandler {
    fun addPresentToPlayer(uuid: String, pos: Long)
    fun fetchFoundPresents(uuid: String) : MutableSet<Long>
    fun getPresents() : MutableSet<PresentData>
    fun getPresentByLong(pos: Long) : PresentData?
    fun addPresent(data: PresentData)
    fun removePrevent(pos: Long)
}