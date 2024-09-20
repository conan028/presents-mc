package com.conan.mods.pwarps.fabric.suggestions

import com.conan.mods.pwarps.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

class WarpSuggestions : SuggestionProvider<ServerCommandSource> {
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>?,
        builder: SuggestionsBuilder?
    ): CompletableFuture<Suggestions> {

        val data = dbHandler!!.getWarpData()
        data.forEach { warp ->
            builder?.suggest(warp.name)
        }

        return builder!!.buildFuture()
    }
}