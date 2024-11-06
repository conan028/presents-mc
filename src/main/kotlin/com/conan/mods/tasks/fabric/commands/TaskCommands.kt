package com.conan.mods.tasks.fabric.commands

import com.conan.mods.tasks.fabric.CobbleTasks.config
import com.conan.mods.tasks.fabric.screenhandler.taskMenuScreenHandlerFactory
import com.conan.mods.tasks.fabric.util.PM
import com.conan.mods.tasks.fabric.util.PermUtil
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

object TaskCommands {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val cobbleTasksCommand = literal("cobbletasks")
            .executes { ctx ->
                openTaskMenu(ctx)
                1
            }

        val pokeTasksCommand = literal("poketasks")
            .executes { ctx ->
                openTaskMenu(ctx)
                1
            }

        val tasksCommand = literal("tasks")
            .executes { ctx ->
                openTaskMenu(ctx)
                1
            }

        val reloadCommand = literal("reload")
            .requires { ctx -> ctx.hasPermissionLevel(2) || PermUtil.commandRequiresPermission(ctx, "cobbletasks.admin")}
            .executes { ctx ->

                config.reload()

                ctx.source.sendFeedback( { PM.returnStyledText("<green>Cobble Tasks > Reloaded config!") }, false)
                1
            }

        cobbleTasksCommand.then(reloadCommand)
        dispatcher.register(cobbleTasksCommand)

        pokeTasksCommand.then(reloadCommand)
        dispatcher.register(pokeTasksCommand)

        tasksCommand.then(reloadCommand)
        dispatcher.register(tasksCommand)
    }

    private fun openTaskMenu(ctx: CommandContext<ServerCommandSource>) {
        val player = ctx.source.playerOrThrow ?: return
        taskMenuScreenHandlerFactory(player)
    }

}