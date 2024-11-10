package com.conan.mods.presents.fabric.screenhandler

import com.cobblemon.mod.common.util.server
import com.conan.mods.presents.fabric.CobblePresent.config
import com.conan.mods.presents.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.conan.mods.presents.fabric.util.PM
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

class PresentScreenHandlerFactory(
    syncId: Int,
    player: ServerPlayerEntity
) : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, player.inventory, SimpleInventory(9 * 6), 6) {

    private var currentIndex = 0

    init {
        populateInventory(currentIndex)

        player.playSound(
            SoundEvent.of(Identifier("cobblemon", "pc.on")),
            SoundCategory.MASTER,
            1.0f,
            1.0f
        )
    }

    private fun populateInventory(cIndex: Int) {
        for (i in 0 until inventory.size()) {
            inventory.setStack(i, ItemStack(Items.GRAY_STAINED_GLASS_PANE).setCustomName(PM.returnStyledText("<gray> ")))
        }

        val activePresents = dbHandler!!.getPresents()

        for ((index, activePresent) in activePresents.drop(cIndex).withIndex()) {
            val present = config.config.presents.find { it.identifier == activePresent.identifier }
            if (present != null) {
                val presentItem = PM.returnPresentItem(present)

                val pos = BlockPos.fromLong(activePresent.pos)

                presentItem.orCreateNbt.putLong("location", activePresent.pos)
                presentItem.orCreateNbt.putString("dimension", activePresent.dimension)
                PM.setLore(presentItem, listOf(
                    PM.returnStyledText("<green>Dimension: <gray>${activePresent.dimension}"),
                    PM.returnStyledText("<green>Location: <gray>${pos.x}</gray>, <gray>${pos.y}</gray>, <gray>${pos.z}</gray>")
                ))

                if (index < 54) {
                    inventory.setStack(index, presentItem)
                }
            }
        }

        for (i in 45 until inventory.size()) {
            inventory.setStack(i, ItemStack(Items.BLACK_STAINED_GLASS_PANE).setCustomName(PM.returnStyledText("<gray> ")))
        }

        if (currentIndex != 0) {
            inventory.setStack(45, ItemStack(Items.ARROW))
        }

        if (currentIndex < (activePresents.size - 1) / 45) {
            inventory.setStack(53, ItemStack(Items.ARROW))
        }

        inventory.setStack(49, ItemStack(Items.BARRIER).setCustomName(PM.returnStyledText("<red>Close")))
    }

    override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity?) {
        val clickedStack = inventory.getStack(slotIndex)
        val activePresents = dbHandler!!.getPresents()

        player?.playSound(
            SoundEvent.of(Identifier("cobblemon", "pc.click")),
            SoundCategory.MASTER,
            1.0f,
            1.0f
        )

        if (slotIndex in 0 until 46 && player is ServerPlayerEntity && clickedStack.nbt?.contains("present") == true) {
            val location = clickedStack.nbt!!.getLong("location")
            val dimension = clickedStack.nbt!!.getString("dimension")
            val pos = BlockPos.fromLong(location)

            val dimensionKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(dimension))
            val world = server()!!.getWorld(dimensionKey)

            player.teleport(
                world,
                pos.x.toDouble() + 0.5,
                pos.y.toDouble() + 2f,
                pos.z.toDouble() + 0.5,
                0f,
                0f
            )

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


    override fun onClosed(player: PlayerEntity?) {
        player?.playSound(
            SoundEvent.of(Identifier("cobblemon", "pc.off")),
            SoundCategory.MASTER,
            1.0f,
            1.0f
        )

        super.onClosed(player)
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