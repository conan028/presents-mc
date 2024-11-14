package com.conan.mods.presents.fabric.util

import com.conan.mods.presents.fabric.Presents.LOGGER
import com.conan.mods.presents.fabric.Presents.config
import com.conan.mods.presents.fabric.Presents.server
import com.conan.mods.presents.fabric.models.MenuItem
import com.conan.mods.presents.fabric.models.Present
import com.mojang.brigadier.ParseResults
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.component.ComponentChanges
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringNbtReader
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
        return Text.Serialization.fromJson(json, server?.registryManager) as Text
    }

    fun returnPresentItem(present: Present): ItemStack {
        val presentItem = ItemStack(Registries.ITEM.get(Identifier.tryParse(present.item.material)))

        val nameData = presentItem.get(DataComponentTypes.CUSTOM_NAME)
        val nameNbt = nameData?.copy().apply { present.item.name?.let { returnStyledText(it) } }
        presentItem.set(DataComponentTypes.CUSTOM_NAME, nameNbt)

        if (present.item.nbt != null) {
            val nbt = StringNbtReader.parse(present.item.nbt)
            val componentChanges = ComponentChanges.CODEC.parse(NbtOps.INSTANCE, nbt).orThrow ?: return ItemStack(Items.BARRIER)
            presentItem.applyChanges(componentChanges)
        }

        val data = presentItem.get(DataComponentTypes.CUSTOM_DATA)
        val compound = data?.copyNbt() ?: NbtCompound()
        compound.putString("present", present.identifier)
        presentItem.setCustomData(compound)

        return presentItem
    }

    fun ItemStack.setCustomData(compound: NbtCompound) {
        this.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound))
    }

    fun setLore(itemStack: ItemStack, lore: List<String>) {
        var itemLore = itemStack.components.get(DataComponentTypes.LORE)

        if (itemLore == null) {
            itemLore = LoreComponent(emptyList())
        }

        val allLoreLines: MutableList<Text> = itemLore.lines.toMutableList()

        for (line in lore) {
            allLoreLines.add(returnStyledText(line))
        }

        itemLore = LoreComponent(allLoreLines)

        itemStack.set(DataComponentTypes.LORE, itemLore)
    }

    fun returnMenuItem(item: MenuItem) : ItemStack {
        val itemStack = ItemStack(Registries.ITEM.get(Identifier.tryParse(item.material)))

        val nameData = itemStack.get(DataComponentTypes.CUSTOM_NAME)
        val nameNbt = nameData?.copy().apply { returnStyledText(item.name) } ?: returnStyledText(item.name)
        itemStack.set(DataComponentTypes.CUSTOM_NAME, nameNbt)

        if (item.nbt != null) {
            val nbt = StringNbtReader.parse(item.nbt)
            val componentChanges = ComponentChanges.CODEC.parse(NbtOps.INSTANCE, nbt).orThrow ?: return ItemStack(Items.BARRIER)
            itemStack.applyChanges(componentChanges)
        }

        if (item.lore?.isNotEmpty() == true) {
            setLore(itemStack, item.lore)
        }

        itemStack.count = item.amount ?: 1

        return itemStack
    }

    fun runCommand(command: String) {
        try {
            val parseResults: ParseResults<ServerCommandSource> = server?.commandManager?.dispatcher?.parse(command, server?.commandSource) ?: return run {
                LOGGER.error("Could not parse for command string: $command")
            }
            server?.commandManager?.dispatcher?.execute(parseResults)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendText(player: PlayerEntity, text: String) {
        val component = returnStyledText(text.replace("%prefix%", config.config.messages.prefix))
        player.sendMessage(component, false)
    }

}