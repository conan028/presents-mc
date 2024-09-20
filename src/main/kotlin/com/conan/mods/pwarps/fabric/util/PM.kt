package com.conan.mods.pwarps.fabric.util

import com.cobblemon.mod.common.util.server
import com.conan.mods.pwarps.fabric.CobblePWarps.config
import com.conan.mods.pwarps.fabric.models.Warp
import com.mojang.brigadier.context.CommandContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.DefaultParticleType
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

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

    fun setLore(itemStack: ItemStack, lore: List<Text>) {
        val itemNbt = itemStack.getOrCreateSubNbt("display")
        val loreNbt = NbtList()

        for (line in lore) {
            loreNbt.add(NbtString.of(Text.Serializer.toJson(line)))
        }

        itemNbt.put("Lore", loreNbt)
    }

    fun getPlayerOrNull(ctx: CommandContext<ServerCommandSource>): ServerPlayerEntity? {
        return ctx.source.playerOrThrow ?: run {
            println("This command can only be run in-game.")
            null
        }
    }

    fun teleportPlayer(player: ServerPlayerEntity, warp: Warp) {
        val dimensionKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(warp.dimension))
        val world = server()!!.getWorld(dimensionKey)

        val pos = BlockPos.fromLong(warp.coords)

        player.teleport(world, pos.x.toDouble() + .5, pos.y.toDouble(), pos.z.toDouble() + .5, -0f, 0f)
        player.playSound(Registries.SOUND_EVENT.get(Identifier.tryParse(config.config.teleportConfig.sound)), SoundCategory.MASTER, 2f, 2f)
        val particle = Registries.PARTICLE_TYPE.get(Identifier.tryParse(config.config.teleportConfig.particle)) as DefaultParticleType?
        ParticleS2CPacket(
            particle,
            false,
            player.x,
            player.y + .5,
            player.z + 1,
            0.0f,
            0.0f,
            0.0f,
            config.config.teleportConfig.particleSpeed,
            config.config.teleportConfig.particleCount
        ).also {
            (player.world as ServerWorld).server.playerManager.sendToAround(
                null, player.x, player.y, player.z,
                1.0, player.world.registryKey, it
            )
        }
    }

    fun sendText(player: PlayerEntity, text: String) {
        val component = returnStyledText(text)
        player.sendMessage(component, false)
    }
}