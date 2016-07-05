package com.dummyc0m.pylon.datakit.example

import com.dummyc0m.pylon.datakit.bukkit.DataKitBukkit
import com.dummyc0m.pylon.datakit.bukkit.DataReceiveEvent
import com.dummyc0m.pylon.datakit.extlib.com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.dummyc0m.pylon.datakit.network.message.DeltaMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

/**
 * Created by Dummy on 7/5/16.
 */
class ExampleListener(private val dataKitBukkit: DataKitBukkit): Listener {
    private val stampedSet: MutableSet<UUID> = HashSet()
    @EventHandler
    fun onDataReceive(dataReceiveEvent: DataReceiveEvent) {
        val dataNode = dataReceiveEvent.jsonData.get("example")
        if (dataNode === null) {
            val msg = DeltaMessage()
            msg.offlineUUID = dataReceiveEvent.player.uniqueId
            msg.setDelta("example.timestamp", factory.textNode(System.currentTimeMillis().toString()))
            dataKitBukkit.send(msg)
        } else {
            dataReceiveEvent.player.sendMessage("Last Login: ${Date(dataNode.get("timestamp").textValue().toLong()).toString()}")
            val msg = DeltaMessage()
            msg.offlineUUID = dataReceiveEvent.player.uniqueId
            msg.setDelta("example.timestamp", factory.textNode(System.currentTimeMillis().toString()))
            dataKitBukkit.send(msg)
        }
        synchronized(stampedSet) {
            stampedSet.add(dataReceiveEvent.player.uniqueId)
        }
    }

    @EventHandler
    fun onPlayerDisconnect(playerQuitEvent: PlayerQuitEvent) {
        val uuid = playerQuitEvent.player.uniqueId
        synchronized(stampedSet) {
            if (stampedSet.contains(uuid)) {
                val msg = DeltaMessage()
                msg.dereference = true
                msg.offlineUUID = uuid
                dataKitBukkit.send(msg)
                stampedSet.remove(uuid)
            }
        }
    }

    companion object {
        private val factory = JsonNodeFactory(false)
    }
}