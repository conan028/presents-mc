package com.conan.mods.pwarps.fabric

import com.conan.mods.pwarps.fabric.commands.WarpsCommands
import com.conan.mods.pwarps.fabric.config.ConfigHandler
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.conan.mods.pwarps.fabric.eco.EconomyHandler
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.LifecycleEvent
import net.fabricmc.api.ModInitializer

object CobblePWarps : ModInitializer {

    var config = ConfigHandler

    override fun onInitialize() {

        LOGGER.info("Player Warps has been enabled!")

        LifecycleEvent.SERVER_STARTED.register {
            EconomyHandler
        }

        CommandRegistrationEvent.EVENT.register { dispatcher, _, _ ->
            WarpsCommands.register(dispatcher)
        }

    }

}