package com.dummyc0m.pylon.bukkit.servernetwork.icon

import com.dummyc0m.pylon.bukkit.servernetwork.app.ServerNetworkApp
import com.dummyc0m.pylon.datakit.bukkitcommons.icon.handler.IIconHandler
import com.dummyc0m.pylon.datakit.bukkitcommons.icon.handler.IconHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*

/**
 * Created by Dummy on 7/18/16.
 */
open class SurvivalClockWork(private val app: ServerNetworkApp, private val route: String) : IconHandler() {
    override fun onInteract(event: PlayerInteractEvent) {
        event.isCancelled = true
//        buffer.bufferedApp(event.player)?.render(event.player, route)
        app.render(event.player, route)
    }
}

class LobbyClockWork(app: ServerNetworkApp, route: String) : SurvivalClockWork(app, route) {
    override fun onDrop(event: PlayerDropItemEvent) {
        event.isCancelled = true
    }

    override fun onInteractEntity(event: PlayerInteractAtEntityEvent) {
        event.isCancelled = true
    }

    override fun onInventoryClick(event: InventoryClickEvent) {
        event.isCancelled = true
    }
}

class StrictHandler() : IIconHandler {
    override fun onSelect(event: PlayerItemHeldEvent) {
    }

    override fun onBreak(event: PlayerItemBreakEvent) {
    }

    override fun onConsume(event: PlayerItemConsumeEvent) {
    }

    override fun onDeselect(event: PlayerItemHeldEvent) {
        event.isCancelled = true
    }

    override fun onDrop(event: PlayerDropItemEvent) {
        event.isCancelled = true
    }

    override fun onInteract(event: PlayerInteractEvent) {
        event.isCancelled = true
    }

    override fun onInteractEntity(event: PlayerInteractAtEntityEvent) {
        event.isCancelled = true
    }

    override fun onInventoryClick(event: InventoryClickEvent) {
        event.isCancelled = true
    }

    override fun onPickUp(event: PlayerPickupItemEvent) {
        event.isCancelled = true
    }
}