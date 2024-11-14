package com.conan.mods.presents.fabric.screenhandler

import com.conan.mods.presents.fabric.Presents.config
import com.conan.mods.presents.fabric.Presents.server
import com.conan.mods.presents.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.conan.mods.presents.fabric.util.PM
import com.conan.mods.presents.fabric.util.PM.setCustomData
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

class PresentScreenHandlerFactory(
    syncId: Int,
    player: ServerPlayerEntity
) : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, player.inventory, SimpleInventory(9 * 6), 6) {

    private var currentIndex = 0

    init {
        populateInventory(currentIndex)
    }

    private fun populateInventory(cIndex: Int) {
        for (i in 0 until inventory.size()) {
            inventory.setStack(i, PM.returnMenuItem(config.config.menu.fillItem))
        }

        val activePresents = dbHandler!!.getPresents()

        for ((index, presentData) in activePresents.drop(cIndex).withIndex()) {
            val present = config.config.presents.find { it.identifier == presentData.identifier } ?: continue
            val presentItem = PM.returnPresentItem(present)

            val pos = BlockPos.fromLong(presentData.pos)

            val data = presentItem.get(DataComponentTypes.CUSTOM_DATA)
            val compound = data?.copyNbt() ?: NbtCompound()
            compound.putLong("location", presentData.pos)
            compound.putString("dimension", presentData.dimension)
            presentItem.setCustomData(compound)

            PM.setLore(presentItem, config.config.menu.presentLore.map {
                it.replace("%dimension%", presentData.dimension)
                    .replace("%x%", "${pos.x}")
                    .replace("%y%", "${pos.y}")
                    .replace("%z%", "${pos.z}")
            })

            if (index < 54) {
                inventory.setStack(index, presentItem)
            }
        }

        for (i in 45 until inventory.size()) {
            inventory.setStack(i, PM.returnMenuItem(config.config.menu.barItem))
        }

        if (currentIndex != 0) {
            inventory.setStack(45, PM.returnMenuItem(config.config.menu.lastPageItem))
        }

        if (currentIndex < (activePresents.size - 1) / 45) {
            inventory.setStack(53, PM.returnMenuItem(config.config.menu.nextPageItem))
        }

        inventory.setStack(49, PM.returnMenuItem(config.config.menu.closeItem))
    }

    private fun teleportPlayer(player: ServerPlayerEntity, dimension: String, pos: BlockPos) {
        val dimensionKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(dimension))
        val world = server?.getWorld(dimensionKey)

        player.teleport(
            world,
            pos.x.toDouble() + 0.5,
            pos.y.toDouble() + 2f,
            pos.z.toDouble() + 0.5,
            0f,
            0f
        )

    }

    override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity?) {
        val clickedStack = inventory.getStack(slotIndex)
        val activePresents = dbHandler!!.getPresents()

        if (slotIndex in 0 until 46 && player is ServerPlayerEntity && clickedStack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.contains("present") == true) {
            val location = clickedStack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.getLong("location") ?: return
            val dimension = clickedStack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.getString("dimension") ?: return
            val pos = BlockPos.fromLong(location) ?: return

            teleportPlayer(player, dimension, pos)

            player.closeHandledScreen()
        }

        if (slotIndex == 45 && currentIndex != 0) {
            currentIndex -= 45
            populateInventory(currentIndex)
        }

        if (slotIndex == 53 && currentIndex < (activePresents.size - 1) / 45) {
            currentIndex += 45
            populateInventory(currentIndex)
        }

        if (slotIndex == 49) {
            player?.closeHandledScreen()
        }
    }

}

fun presentScreenHandlerFactory(
    player: PlayerEntity
) {
    player.openHandledScreen(SimpleNamedScreenHandlerFactory(
        { syncId, _, _ ->
            PresentScreenHandlerFactory(
                syncId,
                player as ServerPlayerEntity
            )
        },
        PM.returnStyledText("<gold>Presents")
    ))
}