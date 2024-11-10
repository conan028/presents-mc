package com.conan.mods.presents.fabric.util

import com.cobblemon.mod.common.util.server
import com.conan.mods.presents.fabric.CobblePresent.config
import com.conan.mods.presents.fabric.models.Present
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

    private fun parseMessageWithStyles(text: String, placeholder: String): Component {
        var mm = MiniMessage.miniMessage();
        return mm.deserialize(text.replace("{placeholder}", placeholder)).decoration(TextDecoration.ITALIC, false)
    }

    fun returnStyledText(text: String): Text {
        val component = parseMessageWithStyles(text, "placeholder")
        val gson = GsonComponentSerializer.gson()
        val json = gson.serialize(component)
        return Text.Serializer.fromJson(json) as Text
    }

    fun returnPresentItem(present: Present) : ItemStack {
        val presentItem = ItemStack(Registries.ITEM.get(Identifier.tryParse(present.item.material)))

        presentItem.setCustomName(returnStyledText(present.item.name))

        if (present.item.nbt != null) {
            presentItem.nbt = NbtHelper.fromNbtProviderString(present.item.nbt)
        }

        presentItem.orCreateNbt.putString("present", present.identifier)

        return presentItem
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

    fun sendText(player: PlayerEntity, text: String) {
        val component = returnStyledText(text.replace("%prefix%", config.config.messages.prefix))
        player.sendMessage(component, false)
    }
}