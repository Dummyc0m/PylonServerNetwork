package com.dummyc0m.pylon.datakit.example

import com.dummyc0m.pylon.datakit.bukkit.DataKitBukkit
import org.bukkit.plugin.java.JavaPlugin

/**
 * Created by Dummy on 7/5/16.
 * Example plugin for Datakit
 * Rule of thumb:
 * 1. Make sure you depend on Datakit in plugin.yml.
 * 2. Listen to DataReceiveEvent and don't forget to register the listener.
 * 3. Make sure to send a DeltaMessage in the end with dereference set to true.
 * 4. If a player log in but its previous transaction be not complete, reject the player's login and dereference immediately!
 * 5. The server disconnects all players before shutting down.
 */
class ExamplePlugin: JavaPlugin() {
    override fun onEnable() {
        server.pluginManager.registerEvents(ExampleListener(DataKitBukkit.Companion.instance), this)
    }

    override fun onDisable() {

    }
}