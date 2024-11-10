package com.conan.mods.presents.fabric

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.conan.mods.presents.fabric.commands.PresentCommand
import com.conan.mods.presents.fabric.config.ConfigHandler
import com.conan.mods.presents.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.conan.mods.presents.fabric.models.PresentData
import com.conan.mods.presents.fabric.util.PM
import com.conan.mods.presents.fabric.util.PermUtil
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.BlockEvent
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.InteractionEvent
import dev.architectury.event.events.common.PlayerEvent
import net.fabricmc.api.ModInitializer
import net.minecraft.component.DataComponentTypes
import net.minecraft.server.network.ServerPlayerEntity

object CobblePresent : ModInitializer {

    var config = ConfigHandler

    override fun onInitialize() {

        LOGGER.info("[Presents] - Enabled!")

        CommandRegistrationEvent.EVENT.register { dispatcher, _, _ ->
            PresentCommand.register(dispatcher)
        }

        InteractionEvent.RIGHT_CLICK_BLOCK.register { player, _, pos, _ ->
            val present = dbHandler!!.getPresentByLong(pos.asLong())

            if (present != null) {
                val foundPresents = dbHandler!!.fetchFoundPresents(player.uuidAsString)

                if (foundPresents.contains(pos.asLong())) {
                    PM.sendText(player, config.config.messages.alreadyFoundPresent)
                    return@register EventResult.interruptFalse()
                }

                val configPresent = config.config.presents.find { it.identifier == present.identifier }
                configPresent?.rewards?.forEach { PM.runCommand(it.replace("%player%", player.name.string)) }

                dbHandler!!.addPresentToPlayer(player.uuidAsString, pos.asLong())
                PM.sendText(player, config.config.messages.foundPresent)
            }

            EventResult.pass()
        }


        BlockEvent.PLACE.register { world, pos, _, player ->
            if (player is ServerPlayerEntity && player.mainHandStack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.contains("present") == true) {
                val presentIdentifier = player.mainHandStack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.getString("present")

                if (presentIdentifier != null) {
                    PresentData(presentIdentifier, world.registryKey.value.path, pos.asLong()).let { dbHandler?.addPresent(it) }
                    PM.sendText(player, "%prefix% <green>Successfully added a new present.")
                }
            }
            EventResult.pass()
        }


        BlockEvent.BREAK.register { _, pos, _, player, _ ->
            if (player is ServerPlayerEntity) {
                val data = dbHandler!!.getPresentByLong(pos.asLong())
                if (data != null) {
                    dbHandler!!.removePrevent(pos.asLong())
                    PM.sendText(player, "%prefix% <red>Successfully removed present.")
                }

            }
            EventResult.pass()
        }


        PlayerEvent.PLAYER_JOIN.register { player ->
            if (player.hasPermissionLevel(2) || PermUtil.commandRequiresPermission(player.commandSource, "presents.admin")) {
                val identifierCounts = config.config.presents.groupBy { it.identifier }
                val duplicateIdentifiers = identifierCounts.filter { it.value.size > 1 }

                if (duplicateIdentifiers.isNotEmpty()) {
                    PM.sendText(player, "%prefix% <red>There are multiple presents with the same identifier in the config.json!")
                }
            }
        }


    }

}