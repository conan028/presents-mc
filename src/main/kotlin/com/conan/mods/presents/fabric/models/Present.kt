package com.conan.mods.presents.fabric.models

import com.conan.mods.presents.fabric.Presents.config
import com.conan.mods.presents.fabric.util.PM
import net.minecraft.server.network.ServerPlayerEntity

data class Present (
    val identifier: String,
    val item: PresentItem,
    val randomReward: Boolean? = false,
    val rewards: MutableList<String?>
) {
    fun awardRewards(player: ServerPlayerEntity) {
        if (rewards.isNotEmpty()) {
            val reward = if (randomReward == true) {
                listOfNotNull(rewards.randomOrNull())
            } else {
                rewards.filterNotNull()
            }

            reward.forEach { PM.runCommand(it.replace("%player%", player.name.string)) }
        }

        PM.sendText(player, config.config.messages.foundPresent)
    }
}

data class PresentItem (
    val name: String?,
    val material: String,
    val nbt: String?,
)

data class PresentData (
    val identifier: String,
    val dimension: String,
    val pos: Long
)