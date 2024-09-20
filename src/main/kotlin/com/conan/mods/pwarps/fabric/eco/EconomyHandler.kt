package com.conan.mods.pwarps.fabric.eco
object EconomyHandler {

    lateinit var economy: EconomyInterface

    init {
        reload()
    }

    fun reload() {
        try {
            economy = ImpactorEconomy()
        } catch (e: NoClassDefFoundError) {
            println("Failed to load economy, please configure economy in config file.")
            throw e
        }
    }
}