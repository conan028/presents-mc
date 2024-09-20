package com.conan.mods.pwarps.fabric.screenhandler

import com.conan.mods.pwarps.fabric.CobblePWarps.config
import com.conan.mods.pwarps.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.conan.mods.pwarps.fabric.models.MenuItem
import com.conan.mods.pwarps.fabric.models.Warp
import com.conan.mods.pwarps.fabric.util.PM
import com.conan.mods.pwarps.fabric.util.UnvalidatedSound
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtHelper
import net.minecraft.registry.Registries
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier

class PlayerWarpsMenu(
    syncId: Int,
    player: ServerPlayerEntity
) : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, player.inventory, SimpleInventory(9 * 6), 6) {

    private var currentIndex = 0

    init {
        populateInventory(currentIndex)

        UnvalidatedSound.playToPlayer(
            Identifier("cobblemon", "pc.on"),
            SoundCategory.MASTER,
            1.0f,
            1.0f,
            player.blockPos,
            player.world,
            2.0,
            player
        )
    }

    private fun getWarpItem(warp: Warp) : ItemStack {
        val warpItem = ItemStack(Registries.ITEM.get(Identifier.tryParse(warp.category)))
        warpItem.setCustomName(PM.returnStyledText(warp.name))
        PM.setLore(warpItem, config.config.menuConfig.warpItemLore.map { PM.returnStyledText(it.replace("%player%", warp.playerName)) })
        return warpItem
    }

    private fun getMenuItem(menuItem: MenuItem) : ItemStack {
        val item = ItemStack(Registries.ITEM.get(Identifier.tryParse(menuItem.material)))
        item.count = menuItem.amount
        if (menuItem.nbt != null) {
            item.nbt = NbtHelper.fromNbtProviderString(menuItem.nbt)
        }
        item.setCustomName(PM.returnStyledText(menuItem.name))
        PM.setLore(item, menuItem.lore.map { PM.returnStyledText(it!!) })
        return item
    }

    private fun populateInventory(currentIndex: Int) {
        for ( i in 0 until inventory.size()) {
            inventory.setStack(i, ItemStack(Items.GRAY_STAINED_GLASS_PANE).setCustomName(PM.returnStyledText("<gray> ")))
        }

        val warps = dbHandler!!.getWarpData()
        for ((index, warp) in warps.drop(currentIndex).withIndex()) {
            val warpItem = getWarpItem(warp)
            inventory.setStack(index, warpItem)
        }

        for (i in 45 until inventory.size()) {
            inventory.setStack(i, ItemStack(Items.BLACK_STAINED_GLASS_PANE).setCustomName(PM.returnStyledText("<gray> ")))
        }

        inventory.setStack(49, getMenuItem(config.config.menuConfig.closeItem))

        if (currentIndex != 0) {
            inventory.setStack(45, getMenuItem(config.config.menuConfig.navigationItems.backItem))
        }

        if (currentIndex < (warps.size - 1) / 45) {
            inventory.setStack(53, getMenuItem(config.config.menuConfig.navigationItems.nextItem))
        }
    }

    override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity?) {
        val clickedStack = inventory.getStack(slotIndex)

        val data = dbHandler!!.getWarpData()
        if (slotIndex in 0 until 44) {
            val warp = data.find { it.name == clickedStack.name.string.lowercase() }

            if (warp != null) {
                PM.teleportPlayer(player as ServerPlayerEntity, warp)
                player.closeHandledScreen()
            }
        }

        if (slotIndex == 45 && currentIndex != 0) {
            currentIndex -= 45
            populateInventory(currentIndex)
        }

        if (slotIndex == 53 && currentIndex < (data.size - 1) / 45) {
            currentIndex += 45
            populateInventory(currentIndex)
        }

        if (slotIndex == 49) {
            player?.closeHandledScreen()
        }
    }

    override fun onClosed(player: PlayerEntity?) {
        UnvalidatedSound.playToPlayer(
            Identifier("cobblemon", "pc.off"),
            SoundCategory.MASTER,
            1.0f,
            1.0f,
            player!!.blockPos,
            player.world,
            2.0,
            player as ServerPlayerEntity
        )
        super.onClosed(player)
    }
}

fun warpSelectionMenuScreenHandlerFactory(
    player: PlayerEntity
) {
    player.openHandledScreen(SimpleNamedScreenHandlerFactory(
        { syncId, _, _ ->
            PlayerWarpsMenu(
                syncId,
                player as ServerPlayerEntity
            )
        },
        PM.returnStyledText(config.config.menuConfig.title)
    ))
}