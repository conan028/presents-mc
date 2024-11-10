package com.conan.mods.presents.fabric.util

import com.cobblemon.mod.common.util.server
import com.conan.mods.presents.fabric.CobblePresent.config
import com.conan.mods.presents.fabric.models.Present
import com.mojang.brigadier.ParseResults
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
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
        return Text.Serialization.fromJson(json, server()!!.registryManager) as Text
    }

    fun returnPresentItem(present: Present) : ItemStack {
        val presentItem = ItemStack(Registries.ITEM.get(Identifier.tryParse(present.item.material)))

        presentItem.set(DataComponentTypes.CUSTOM_NAME, returnStyledText(present.item.name))

        if (present.item.nbt != null) {
            presentItem.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(NbtHelper.fromNbtProviderString(present.item.nbt)))
        }

        val compound = NbtCompound()
        compound.putString("present", present.identifier)
        presentItem.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound))

        return presentItem
    }

    fun setLore(itemStack: ItemStack, lore: List<Text>) {
        val itemNbt = itemStack.get(DataComponentTypes.LORE)

        val loreNbt = NbtList()

        for (line in lore) {
            loreNbt.add(NbtString.of(Text.Serialization.toJsonString(line, server()!!.registryManager)))
        }

        itemStack.set(DataComponentTypes.LORE, itemNbt)
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