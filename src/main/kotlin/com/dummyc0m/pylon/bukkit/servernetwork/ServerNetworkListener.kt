package com.dummyc0m.pylon.bukkit.servernetwork

import com.dummyc0m.pylon.datakit.bukkit.DataReceiveEvent
import com.dummyc0m.pylon.datakit.bukkitcommons.icon.Icon
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Created by Dummy on 7/5/16.
 * A DataKit plugin example that records and displays the timestamp of the player's previous access to the server
 */
class ServerNetworkListener(private val lobbyMode: Boolean,
                            private val clockWorkItem: Icon,
                            private val cosmeticsItem: Icon,
                            private val navigatorItem: Icon) : Listener {
    @EventHandler
    fun onDataReceive(dataReceiveEvent: DataReceiveEvent) {
        val player = dataReceiveEvent.player
        if (lobbyMode) {
            val inventory = player.inventory
            inventory.setItem(0, navigatorItem.getItem(player))
            inventory.setItem(2, cosmeticsItem.getItem(player))
            inventory.setItem(4, clockWorkItem.getItem(player))
        }
    }

//    /**
//     * Make sure to check if the player is loaded before cancelling any events!!!
//     */
//    @EventHandler
//    fun onPlayerDisconnect(playerQuitEvent: PlayerQuitEvent) {
//        val uuid = playerQuitEvent.player.uniqueId
//        synchronized(stampedSet) {
//            if (stampedSet.contains(uuid)) {
//                val msg = DeltaMessage()
//                msg.dereference = true
//                msg.offlineUUID = uuid
//                dataKitBukkit.send(msg)
//                stampedSet.remove(uuid)
////                buffer.drop(playerQuitEvent.player)
//            }
//        }
//    }

//    companion object {
//        private val factory = JsonNodeFactory(false)
//    }
}