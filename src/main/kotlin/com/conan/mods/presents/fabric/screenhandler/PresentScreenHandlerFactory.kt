package com.conan.mods.presents.fabric.screenhandler

import com.cobblemon.mod.common.util.server
import com.conan.mods.presents.fabric.CobblePresent.config
import com.conan.mods.presents.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.conan.mods.presents.fabric.util.PM
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
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

    var currentIndex = 0

    init {
        populateInventory(currentIndex)

        player.playSound(
            SoundEvent.of(Identifier.tryParse("cobblemon", "pc.on")),
            1.0f,
            1.0f
        )
    }

    private fun populateInventory(cIndex: Int) {
        val fillItem = ItemStack(Items.GRAY_STAINED_GLASS_PANE)
        fillItem.set(DataComponentTypes.CUSTOM_NAME, PM.returnStyledText("<gray> "))
        for (i in 0 until inventory.size()) {
            inventory.setStack(i, fillItem)
        }

        val activePresents = dbHandler!!.getPresents()

        for ((index, activePresent) in activePresents.drop(cIndex).withIndex()) {
            val present = config.config.presents.find { it.identifier == activePresent.identifier }
            if (present != null) {
                val presentItem = PM.returnPresentItem(present)

                val pos = BlockPos.fromLong(activePresent.pos)

                val compound = NbtCompound()
                compound.putLong("location", activePresent.pos)
                compound.putString("dimension", activePresent.dimension)
                presentItem.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound))

                PM.setLore(presentItem, listOf(
                    PM.returnStyledText("<green>Dimension: <gray>${activePresent.dimension}"),
                    PM.returnStyledText("<green>Location: <gray>${pos.x}</gray>, <gray>${pos.y}</gray>, <gray>${pos.z}</gray>")
                ))

                if (index < 54) {
                    inventory.setStack(index, presentItem)
                }
            }
        }

        val fillBGItem = ItemStack(Items.BLACK_STAINED_GLASS_PANE)
        fillBGItem.set(DataComponentTypes.CUSTOM_NAME, PM.returnStyledText("<gray> "))
        for (i in 45 until inventory.size()) {
            inventory.setStack(i, fillBGItem)
        }

        if (currentIndex != 0) {
            inventory.setStack(45, ItemStack(Items.ARROW))
        }

        if (currentIndex < (activePresents.size - 1) / 45) {
            inventory.setStack(53, ItemStack(Items.ARROW))
        }

        val closeItem = ItemStack(Items.BARRIER)
        closeItem.set(DataComponentTypes.CUSTOM_NAME, PM.returnStyledText("<red>Close"))
        inventory.setStack(49, closeItem)
    }

    override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity?) {
        val clickedStack = inventory.getStack(slotIndex)
        val activePresents = dbHandler!!.getPresents()

        player?.playSound(
            SoundEvent.of(Identifier.tryParse("cobblemon", "pc.click")),
            1.0f,
            1.0f
        )

        if (slotIndex in 0 until 46 && player is ServerPlayerEntity && clickedStack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.contains("present") == true) {
            val location = clickedStack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.getLong("location") ?: return
            val dimension = clickedStack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.getString("dimension") ?: return
            val pos = BlockPos.fromLong(location) ?: return

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
            SoundEvent.of(Identifier.tryParse("cobblemon", "pc.off")),
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