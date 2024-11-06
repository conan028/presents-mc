package com.conan.mods.tasks.fabric.util

import com.cobblemon.mod.common.util.server
import com.conan.mods.tasks.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.conan.mods.tasks.fabric.enums.CobblemonEvent
import com.conan.mods.tasks.fabric.models.MenuItem
import com.conan.mods.tasks.fabric.models.Task
import com.mojang.brigadier.ParseResults
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object PM {

    fun parseMessageWithStyles(text: String, placeholder: String): Component {
        var mm = MiniMessage.miniMessage();
        return mm.deserialize(text.replace("{placeholder}", placeholder)).decoration(TextDecoration.ITALIC, false)
    }

    fun returnStyledText(text: String): Text {
        val component = parseMessageWithStyles(text, "placeholder")
        val gson = GsonComponentSerializer.gson()
        val json = gson.serialize(component)
        return Text.Serializer.fromJson(json) as Text
    }

    fun getMenuItem(item: MenuItem) : ItemStack {
        val menuItem = ItemStack(Registries.ITEM.get(Identifier.tryParse(item.material)))
        menuItem.setCustomName(returnStyledText(item.name))

        menuItem.count = item.amount

        if (item.nbt != null) {
            menuItem.nbt = NbtHelper.fromNbtProviderString(item.nbt)
        }

        return menuItem
    }
    fun returnTime(task: Task): String {
        val currentTimeMillis = System.currentTimeMillis()

        val timeBetween = (task.time ?: return "Time not available") - currentTimeMillis

        val days = timeBetween / (24 * 3600 * 1000)
        val hours = (timeBetween % (24 * 3600 * 1000)) / (3600 * 1000)
        val minutes = (timeBetween % (3600 * 1000)) / (60 * 1000)
        val seconds = (timeBetween % (60 * 1000)) / 1000

        return "${days}d ${hours}hrs ${minutes}mins ${seconds}secs"
    }

    fun fetchTaskDescription(task: CobblemonEvent) : String {
        val description = when (task) {
            CobblemonEvent.CATCH -> "Catch Wild Pokémon"
            CobblemonEvent.KILL -> "Kill Wild Pokémon"
            CobblemonEvent.DEFEAT -> "Defeat Wild Pokémon"
            CobblemonEvent.EVOLVE -> "Evolve Pokémon"
            CobblemonEvent.TRADE -> "Trade Pokémon"
            CobblemonEvent.FISH -> "Fish Pokémon"
            CobblemonEvent.BERRY -> "Harvest Berries"
            CobblemonEvent.APRICORN -> "Harvest Apricorns"
        }

        return description
    }

    fun setLore(itemStack: ItemStack, lore: List<Text>) {
        val itemNbt = itemStack.getOrCreateSubNbt("display")
        val loreNbt = NbtList()

        for (line in lore) {
            loreNbt.add(NbtString.of(Text.Serializer.toJson(line)))
        }

        itemNbt.put("Lore", loreNbt)
    }

    fun runCommand(command: String) {
        try {
            val parseResults: ParseResults<ServerCommandSource> =
                server()!!.commandManager.dispatcher.parse(command, server()!!.commandSource)
            server()!!.commandManager.dispatcher.execute(parseResults)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkAndRewardCommunity(task: Task) {
        if (task.current != null && (task.current!! >= task.goal - 1)) {
            server()!!.playerManager.playerList.forEach { player ->
                task.rewards.forEach { reward ->
                    runCommand(reward.replace("%player%", player.entityName))
                }

                sendText(player, "<green>Cobble Tasks <dark_gray>>> <gray>The <gold>${task.difficulty} <gray>Community Task has been reached!")

                dbHandler!!.refreshTask(task.difficulty)
            }
        }
    }

    fun sendText(player: PlayerEntity, text: String) {
        val component = returnStyledText(text)
        player.sendMessage(component, false)
    }
}