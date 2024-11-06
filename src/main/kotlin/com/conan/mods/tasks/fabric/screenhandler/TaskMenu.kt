package com.conan.mods.tasks.fabric.screenhandler

import com.cobblemon.mod.common.util.server
import com.conan.mods.tasks.fabric.CobbleTasks.config
import com.conan.mods.tasks.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.conan.mods.tasks.fabric.enums.Difficulty
import com.conan.mods.tasks.fabric.util.PM
import com.conan.mods.tasks.fabric.util.UnvalidatedSound
import kotlinx.coroutines.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier

class TaskMenu(
    syncId: Int,
    player: ServerPlayerEntity
) : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, player.inventory, SimpleInventory(9 * 3), 3) {

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        dbHandler!!.initializeTasksIfNotExists()

        scope.launch {
            while (isActive) {
                val tasks = dbHandler!!.fetchTasks().toList()
                tasks.forEach { task ->
                    if (task.time != null && System.currentTimeMillis() >= task.time!!) {
                        dbHandler!!.refreshTask(task.difficulty)
                    }
                }

                withContext(Dispatchers.Default) {
                    server()!!.execute { populateInventory() }
                }

                delay(5000)
            }
        }

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

    private fun returnTaskItem(difficulty: Difficulty) {
        val task = when (difficulty) {
            Difficulty.EASY -> dbHandler!!.fetchTask(Difficulty.EASY)
            Difficulty.MEDIUM -> dbHandler!!.fetchTask(Difficulty.MEDIUM)
            Difficulty.HARD -> dbHandler!!.fetchTask(Difficulty.HARD)
        }

        val taskConfigItem = when (difficulty) {
            Difficulty.EASY -> config.config.menuConfig.easyTaskItem
            Difficulty.MEDIUM -> config.config.menuConfig.mediumTaskItem
            Difficulty.HARD -> config.config.menuConfig.hardTaskItem
        }

        val taskSlot = when (difficulty) {
            Difficulty.EASY -> config.config.menuConfig.easyTaskItem.slot
            Difficulty.MEDIUM -> config.config.menuConfig.mediumTaskItem.slot
            Difficulty.HARD -> config.config.menuConfig.hardTaskItem.slot
        }

        val taskItem = PM.getMenuItem(taskConfigItem)
        PM.setLore(taskItem, listOf(
            PM.returnStyledText(" "),
            PM.returnStyledText("<green>Task: <gray>${PM.fetchTaskDescription(task.sort)}"),
            PM.returnStyledText("<green>Goal: <gray>${task.current ?: 0}<dark_gray>/<gray>${task.goal}"),
            PM.returnStyledText(" "),
            PM.returnStyledText("<gold>Time remaining: <gray>${PM.returnTime(task)}"))
        )

        inventory.setStack(taskSlot, taskItem)
    }

    private fun populateInventory() {
        for (i in 0 until inventory.size()) {
            inventory.setStack(i, ItemStack(Items.GRAY_STAINED_GLASS_PANE).setCustomName(PM.returnStyledText("<gray> ")))
        }

        // Task Items
        Difficulty.values().forEach { task -> returnTaskItem(task) }

        for (i in 18 until inventory.size()) {
            inventory.setStack(i, ItemStack(Items.BLACK_STAINED_GLASS_PANE).setCustomName(PM.returnStyledText("<gray> ")))
        }

        inventory.setStack(config.config.menuConfig.closeItem.slot, PM.getMenuItem(config.config.menuConfig.closeItem))
    }

    override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity?) {
        if (slotIndex == config.config.menuConfig.closeItem.slot) {
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

        scope.cancel()

        super.onClosed(player)
    }
}

fun taskMenuScreenHandlerFactory(
    player: PlayerEntity
) {
    player.openHandledScreen(SimpleNamedScreenHandlerFactory(
        { syncId, _, _ ->
            TaskMenu(
                syncId,
                player as ServerPlayerEntity
            )
        },
        PM.returnStyledText(config.config.menuConfig.title)
    ))
}