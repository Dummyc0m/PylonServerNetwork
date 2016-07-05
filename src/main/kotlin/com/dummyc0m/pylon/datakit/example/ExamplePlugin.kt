package com.dummyc0m.pylon.datakit.example

import com.dummyc0m.pylon.datakit.bukkit.DataKitBukkit
import org.bukkit.plugin.java.JavaPlugin

/**
 * Created by Dummy on 7/5/16.
 */
class ExamplePlugin: JavaPlugin() {
    override fun onEnable() {
        server.pluginManager.registerEvents(ExampleListener(DataKitBukkit.Companion.instance), this)
    }

    override fun onDisable() {

    }
}