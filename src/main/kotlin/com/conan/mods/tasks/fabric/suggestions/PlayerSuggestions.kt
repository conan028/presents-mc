package com.conan.mods.tasks.fabric.suggestions

import com.cobblemon.mod.common.util.server
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

class PlayerSuggestions : SuggestionProvider<ServerCommandSource> {
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>?,
        builder: SuggestionsBuilder?
    ): CompletableFuture<Suggestions> {

        server()!!.playerManager.playerList.forEach { player ->
            builder?.suggest(player.entityName)
        }

        return builder!!.buildFuture()
    }
}