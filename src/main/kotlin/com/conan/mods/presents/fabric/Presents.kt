package com.conan.mods.presents.fabric

import com.conan.mods.presents.fabric.commands.PresentCommand
import com.conan.mods.presents.fabric.config.ConfigHandler
import com.conan.mods.presents.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.conan.mods.presents.fabric.models.PresentData
import com.conan.mods.presents.fabric.util.PM
import com.conan.mods.presents.fabric.util.PermUtil
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.*
import net.fabricmc.api.ModInitializer
import net.minecraft.component.DataComponentTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import org.apache.logging.log4j.LogManager

object Presents : ModInitializer {

    val LOGGER = LogManager.getLogger()

    var server: MinecraftServer? = null
    var config = ConfigHandler

    override fun onInitialize() {

        LOGGER.info("[Presents] - Enabled!")

        CommandRegistrationEvent.EVENT.register { dispatcher, _, _ ->
            PresentCommand.register(dispatcher)
        }

        LifecycleEvent.SERVER_STARTING.register { server = it }

        InteractionEvent.RIGHT_CLICK_BLOCK.register { player, _, pos, _ ->
            val potentialPresent = dbHandler!!.getPresentByLong(pos.asLong())

            if (potentialPresent != null) {
                val foundPresents = dbHandler!!.fetchFoundPresents(player.uuidAsString)

                if (foundPresents.contains(pos.asLong())) {
                    PM.sendText(player, config.config.messages.alreadyFoundPresent)
                    return@register EventResult.interruptFalse()
                }

                val present = config.config.presents.find { it.identifier == potentialPresent.identifier } ?: return@register  EventResult.interruptFalse()
                present.awardRewards(player as ServerPlayerEntity)
                dbHandler!!.addPresentToPlayer(player.uuidAsString, pos.asLong())

                return@register EventResult.interruptFalse()
            }
            EventResult.pass()
        }

        BlockEvent.PLACE.register { world, pos, _, player ->
            if (player is ServerPlayerEntity) {
                player.mainHandStack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.let { nbt ->
                    val presentIdentifier = nbt.getString("present")
                    val presentData = PresentData(presentIdentifier, world.registryKey.value.path, pos.asLong())
                    dbHandler?.addPresent(presentData)
                    PM.sendText(player, "%prefix% <gray>Successfully added a new present.")
                }
            }
            EventResult.pass()
        }

        BlockEvent.BREAK.register { _, pos, _, player, _ ->
            if (player is ServerPlayerEntity) {
                val data = dbHandler!!.getPresentByLong(pos.asLong()) ?: return@register EventResult.pass()
                dbHandler!!.removePresent(data.pos)
                PM.sendText(player, "%prefix% <gray>Successfully removed present.")
            }
            EventResult.pass()
        }

        PlayerEvent.PLAYER_JOIN.register { player ->
            if (player.hasPermissionLevel(2) || PermUtil.commandRequiresPermission(player.commandSource, "present.admin")) {
                val identifierCounts = config.config.presents.groupBy { it.identifier }
                val duplicateIdentifiers = identifierCounts.filter { it.value.size > 1 }

                if (duplicateIdentifiers.isNotEmpty()) {
                    PM.sendText(player, "%prefix% <red>There are multiple presents with the same identifier in the config.json!")
                }
            }
        }

    }

}