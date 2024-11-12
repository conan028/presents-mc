package com.conan.mods.presents.fabric.commands

import com.conan.mods.presents.fabric.Presents.config
import com.conan.mods.presents.fabric.screenhandler.presentScreenHandlerFactory
import com.conan.mods.presents.fabric.util.PM
import com.conan.mods.presents.fabric.util.PermUtil
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

object PresentCommand {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val presentCommand = literal("presents")
            .requires { ctx -> ctx.hasPermissionLevel(2) || PermUtil.commandRequiresPermission(ctx, "present.admin") }
            .executes { ctx ->
                val player = ctx.source.player ?: run {
                    return@executes 0.also {
                        ctx.source.sendFeedback( { PM.returnStyledText("You are not a player!") }, false)
                    }
                }
                presentScreenHandlerFactory(player)
                1
            }

        val givePresentCommand = literal("give")
            .then(argument("identifier", StringArgumentType.word())
                .suggests(PresentSuggestions())
                .executes(::givePresent)
            )

        val reloadCommand = literal("reload")
            .executes { ctx ->

                config.reload()

                ctx.source.sendFeedback(
                    { PM.returnStyledText("<red>[<dark_green>Presents<red>] <dark_gray>» <gray>Reloaded config.") },
                    false
                )
                1
            }

        presentCommand.then(givePresentCommand)

        presentCommand.then(reloadCommand)
        dispatcher.register(presentCommand)
    }

    private fun givePresent(context: CommandContext<ServerCommandSource>?) : Int {
        val player = context?.source?.player ?: run {
            return 0.also {
                context?.source?.sendFeedback({ PM.returnStyledText("You are not a player!") }, false)
            }
        }

        val identifier = StringArgumentType.getString(context, "identifier")

        val present = config.config.presents.find { it.identifier == identifier } ?: return 0.also {
            PM.sendText(player, "%prefix% <red>Could not find present with identifier <gray>$identifier</gray>.")
        }

        val presentItem = PM.returnPresentItem(present)
        PM.sendText(player, "%prefix% <gray>Successfully received the <gold>$identifier</gold> present.")
        player.inventory.offerOrDrop(presentItem)

        return Command.SINGLE_SUCCESS
    }

}

class PresentSuggestions() : SuggestionProvider<ServerCommandSource> {
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>?,
        builder: SuggestionsBuilder?
    ): CompletableFuture<Suggestions> {
        config.config.presents.forEach {
            builder?.suggest(it.identifier)
        }
        return builder!!.buildFuture()
    }

}