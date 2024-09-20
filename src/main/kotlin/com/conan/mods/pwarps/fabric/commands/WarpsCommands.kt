package com.conan.mods.pwarps.fabric.commands

import com.conan.mods.pwarps.fabric.CobblePWarps.config
import com.conan.mods.pwarps.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.conan.mods.pwarps.fabric.eco.EconomyHandler.economy
import com.conan.mods.pwarps.fabric.models.Warp
import com.conan.mods.pwarps.fabric.screenhandler.warpSelectionMenuScreenHandlerFactory
import com.conan.mods.pwarps.fabric.suggestions.WarpSuggestions
import com.conan.mods.pwarps.fabric.util.PM
import com.conan.mods.pwarps.fabric.util.PermUtil
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

object WarpsCommands {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val warpsCommand = literal("pwarps")
            .executes { ctx ->
                openWarpsMenu(ctx)
                1
            }

        val warpCommand = literal("pwarp")
            .then(argument("name", StringArgumentType.word())
                .suggests(WarpSuggestions())
                    .executes { ctx ->
                        teleportToWarp(ctx)
                        1
                    })

        val setWarpCommand = literal("setpwarp")
            .then(argument("name", StringArgumentType.word())
                .executes { ctx ->
                    setWarp(ctx)
                    1
                })

        val delWarpCommand = literal("delpwarp")
            .then(argument("name", StringArgumentType.word())
                .suggests(WarpSuggestions())
                .executes { ctx ->
                    delWarp(ctx)
                    1
                })

        val reloadCommand = literal("reload")
            .requires { ctx -> ctx.hasPermissionLevel(2) || PermUtil.commandRequiresPermission(ctx, "pwarps.admin")}
            .executes { ctx ->

                config.reload()

                ctx.source.sendFeedback( { PM.returnStyledText("<green>Player Warps > Reloaded config!") }, false)
                1
            }

        warpsCommand.then(reloadCommand)

        dispatcher.register(warpCommand)
        dispatcher.register(setWarpCommand)
        dispatcher.register(delWarpCommand)
        dispatcher.register(warpsCommand)
    }

    private fun openWarpsMenu(ctx: CommandContext<ServerCommandSource>) {
        val player = PM.getPlayerOrNull(ctx) ?: return
        warpSelectionMenuScreenHandlerFactory(player)
    }

    private fun setWarp(ctx: CommandContext<ServerCommandSource>) {
        val player = PM.getPlayerOrNull(ctx) ?: return

        val warpName = StringArgumentType.getString(ctx, "name").lowercase()

        val warpData = dbHandler!!.getWarpData()
        if (warpData.any { it.name == warpName }) {
            PM.sendText(player, "<red>This warp already exists!")
            return
        }

        if (config.config.economyConfig.takeMoney) {
            val playerBalance = economy.getBalance(player.uuid)
            if (playerBalance < config.config.economyConfig.price) {
                PM.sendText(player, "<red>You do not have sufficient funds!")
                return
            }

            economy.withdraw(player.uuid, config.config.economyConfig.price)
        }

        dbHandler!!.addWarp(
            Warp(
                player.uuid,
                player.entityName ?: "Unknown",
                "minecraft:dirt",
                warpName,
                player.world.registryKey.value.path,
                player.blockPos.asLong()
            )
        )
        PM.sendText(player, "<green>You've successfully made a new warp named: <gold>$warpName<green>!")
    }
    private fun delWarp(ctx: CommandContext<ServerCommandSource>) {
        val player = PM.getPlayerOrNull(ctx) ?: return

        val warpName = StringArgumentType.getString(ctx, "name").lowercase()

        val warpData = dbHandler!!.getWarpData()

        val warp = warpData.find { it.name == warpName && it.playerUUID == player.uuid }
        if (warp == null) {
            PM.sendText(player, "<red>You have no warp called <gold>$warpName<green>!")
            return
        }

        dbHandler!!.delWarp(warp)
        PM.sendText(player, "<red>You've successfully removed your warp named: <gold>$warpName<red>!")
    }

    private fun teleportToWarp(ctx: CommandContext<ServerCommandSource>) {
        val player = PM.getPlayerOrNull(ctx) ?: return

        val warpName = StringArgumentType.getString(ctx, "name").lowercase()

        val warpData = dbHandler!!.getWarpData()

        val warp = warpData.find { it.name == warpName }
        if (warp == null) {
            PM.sendText(player, "<red>Could not find warp with the name <gold>$warpName<red>!")
            return
        }

        PM.teleportPlayer(player, warp)
        PM.sendText(player, "<green>You've been teleported to <gold>$warpName<green>!")
    }
}