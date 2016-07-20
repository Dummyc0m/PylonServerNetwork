package com.dummyc0m.pylon.bukkit.servernetwork.app

import com.dummyc0m.pylon.bukkit.servernetwork.PriceConfig
import com.dummyc0m.pylon.bukkit.servernetwork.app.convert.ConversionMenu
import com.dummyc0m.pylon.bukkit.servernetwork.app.cosmetic.CosmeticMenu
import com.dummyc0m.pylon.bukkit.servernetwork.cosmetic.CosmeticModule
import com.dummyc0m.pylon.datakit.bukkitcommons.app.App
import com.dummyc0m.pylon.datakit.bukkitcommons.conversation.ChatTriggerModule
import com.dummyc0m.pylon.datakit.bukkitcommons.econ.EconModule
import org.bukkit.plugin.java.JavaPlugin

/**
 * Created by Dummy on 7/17/16.
 */
class ServerNetworkApp(plugin: JavaPlugin,
                       priceConfig: PriceConfig,
                       chatTriggerModule: ChatTriggerModule,
                       econModule: EconModule,
                       cosmeticModule: CosmeticModule) : App(plugin) {
    init {
        router.default(MainMenu(router, chatTriggerModule, econModule))
        router.addRoute("/convert", ConversionMenu(router, priceConfig, econModule, cosmeticModule))
        router.addRoute("/cosmetic", CosmeticMenu(router, cosmeticModule, priceConfig))
    }
}