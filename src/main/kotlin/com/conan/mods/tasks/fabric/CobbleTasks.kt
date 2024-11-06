package com.conan.mods.tasks.fabric

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.server
import com.conan.mods.tasks.fabric.commands.TaskCommands
import com.conan.mods.tasks.fabric.config.ConfigHandler
import com.conan.mods.tasks.fabric.datahandler.DatabaseHandlerSingleton.dbHandler
import com.conan.mods.tasks.fabric.enums.CobblemonEvent
import com.conan.mods.tasks.fabric.util.PM
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.events.common.LifecycleEvent
import kotlinx.coroutines.*
import net.fabricmc.api.ModInitializer
import net.minecraft.server.network.ServerPlayerEntity

object CobbleTasks : ModInitializer {

    var config = ConfigHandler

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onInitialize() {

        LOGGER.info("Cobblemon Tasks has been enabled!")

        CommandRegistrationEvent.EVENT.register { dispatcher, _, _ ->
            TaskCommands.register(dispatcher)
        }

        CobblemonEvents.POKEMON_CAPTURED.subscribe { _ ->
            CoroutineScope(Dispatchers.IO).launch {
                dbHandler!!.checkTaskAndUpdate(CobblemonEvent.CATCH)
            }
        }

        CobblemonEvents.EVOLUTION_COMPLETE.subscribe { _ ->
            CoroutineScope(Dispatchers.IO).launch {
                dbHandler!!.checkTaskAndUpdate(CobblemonEvent.EVOLVE)
            }
        }

        CobblemonEvents.BERRY_HARVEST.subscribe { _ ->
            CoroutineScope(Dispatchers.IO).launch {
                dbHandler!!.checkTaskAndUpdate(CobblemonEvent.BERRY)
            }
        }

        CobblemonEvents.APRICORN_HARVESTED.subscribe { _ ->
            CoroutineScope(Dispatchers.IO).launch {
                dbHandler!!.checkTaskAndUpdate(CobblemonEvent.APRICORN)
            }
        }

        CobblemonEvents.TRADE_COMPLETED.subscribe { _ ->
            CoroutineScope(Dispatchers.IO).launch {
                dbHandler!!.checkTaskAndUpdate(CobblemonEvent.TRADE)
            }
        }

        CobblemonEvents.POKEMON_FAINTED.subscribe { event ->
            if (event.pokemon.isWild() && event.pokemon.entity != null && event.pokemon.entity!!.killer is ServerPlayerEntity) {
                val dmgSource = event.pokemon.entity?.recentDamageSource
                if (dmgSource != null && dmgSource.attacker is ServerPlayerEntity) return@subscribe
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Default) {
                        server()!!.execute {
                            dbHandler!!.checkTaskAndUpdate(CobblemonEvent.DEFEAT)
                        }
                    }
                }
            }
        }

        EntityEvent.LIVING_DEATH.register { entity, _ ->
            if (entity is PokemonEntity && entity.pokemon.isWild()) {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Default) {
                        server()!!.execute {
                            dbHandler!!.checkTaskAndUpdate(CobblemonEvent.KILL)
                        }
                    }
                }
            }
            EventResult.pass()
        }


    }

}