package com.dummyc0m.pylon.bukkit.servernetwork.cosmetic

import com.dummyc0m.pylon.datakit.bukkit.DataKitBukkit
import com.dummyc0m.pylon.datakit.bukkitcommons.icon.Icon
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Dummy on 7/18/16.
 */
class CosmeticModule(plugin: JavaPlugin, lobbyMode: Boolean, dragonHeadIcon: Icon, elytraIcon: Icon) {
    private val cosmeticMap: MutableMap<UUID, CosmeticData> = ConcurrentHashMap()

    init {
        plugin.server.pluginManager.registerEvents(CosmeticListener(this, DataKitBukkit.Companion.instance, lobbyMode, dragonHeadIcon, elytraIcon), plugin)
    }

    fun getData(uuid: UUID): CosmeticData? {
        return cosmeticMap.get(uuid)
    }

    internal fun addData(cosmeticData: CosmeticData) {
        cosmeticMap.put(cosmeticData.uuid, cosmeticData)
    }

    internal fun drop(uuid: UUID) {
        cosmeticMap.remove(uuid)
    }
}

/*
announce
player prefix
flight
elytra
speed

double jump
dragonHead
chat color
 */

