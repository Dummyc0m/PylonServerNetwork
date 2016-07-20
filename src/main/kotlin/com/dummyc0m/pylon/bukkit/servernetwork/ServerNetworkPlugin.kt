package com.dummyc0m.pylon.bukkit.servernetwork

import com.dummyc0m.pylon.bukkit.servernetwork.app.ServerNetworkApp
import com.dummyc0m.pylon.bukkit.servernetwork.cosmetic.CosmeticModule
import com.dummyc0m.pylon.bukkit.servernetwork.icon.LobbyClockWork
import com.dummyc0m.pylon.bukkit.servernetwork.icon.StrictHandler
import com.dummyc0m.pylon.bukkit.servernetwork.icon.SurvivalClockWork
import com.dummyc0m.pylon.datakit.bukkitcommons.conversation.ChatTriggerModule
import com.dummyc0m.pylon.datakit.bukkitcommons.econ.EconModule
import com.dummyc0m.pylon.datakit.bukkitcommons.icon.IconModule
import com.dummyc0m.pylon.datakit.bukkitcommons.icon.handler.IconHandler
import com.dummyc0m.pylon.pyloncore.ConfigFile
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * Created by Dummy on 7/5/16.
 * Example plugin for Datakit and BukkitCommons
 * Rule of thumb:
 * 1. Make sure you depend on Datakit in plugin.yml.
 * 2. Listen to DataReceiveEvent and don't forget to register the listener.
 * 3. Make sure to send a DeltaMessage in the end with dereference set to true.
 * 4. If a player log in but its previous transaction be not complete, reject the player's login and dereference immediately!
 * 5. The server disconnects all players before shutting down.
 *
 * 6. For EconModule, handle dereferencing yourself
 */
class ServerNetworkPlugin : JavaPlugin() {
    val economy: EconModule
        get() = econModule
    private lateinit var config: Config
    private lateinit var priceConfig: PriceConfig
    private lateinit var iconModule: IconModule
    private lateinit var econModule: EconModule
    private lateinit var cosmeticModule: CosmeticModule
    private var lobbyMode = false

    override fun onEnable() {
        _instance = this
        config = ConfigFile<Config>(dataFolder, "config.json", Config::class.java).config
        priceConfig = ConfigFile<PriceConfig>(dataFolder, "price.json", PriceConfig::class.java).config
        if (config.langFile != "en_US") {
            I18N.instance.addLangFile(File(dataFolder, config.langFile + ".lang"))
        } else {
            I18N.instance.addLangStream(ServerNetworkPlugin::class.java.getResourceAsStream("/servernetwork/en_US.lang"))
        }
        lobbyMode = config.runMode.equals("lobby", true)
        iconModule = IconModule(this)
        econModule = EconModule(this)

        val elytraIcon = iconModule.newIcon("glider")
                .item {
                    material = Material.ELYTRA
                    flag(ItemFlag.HIDE_UNBREAKABLE)
                    onRender { itemMeta, humanEntity -> itemMeta.spigot().isUnbreakable = true }
                }.handler(StrictHandler()).register()

        val dragonHeadIcon = iconModule.newIcon("dragonHead")
                .item {
                    material = Material.SKULL_ITEM
                    damage = 5
                }.handler(StrictHandler()).register()

        cosmeticModule = CosmeticModule(this, lobbyMode, dragonHeadIcon, elytraIcon)
        val app = ServerNetworkApp(this, priceConfig, ChatTriggerModule(this), econModule, cosmeticModule)

        val clockwork = iconModule.newIcon("clockwork")
                .item {
                    material = Material.WATCH
                    displayName = I18N.instance.translateKey("item.clockwork.name")
                    lore(I18N.instance.translateKey("item.clockwork.desc"))
                }

        val cosmetics = iconModule.newIcon("cosmetics")
                .item {
                    material = Material.SKULL_ITEM
                    damage = 3
                    lore(I18N.instance.translateKey("item.cosmetics.desc"))
                    enchant(Enchantment.LUCK, 1)
                    flag(ItemFlag.HIDE_ENCHANTS)
                    onRender { itemMeta, humanEntity ->
                        if (itemMeta is SkullMeta) itemMeta.owner = humanEntity.name
                        itemMeta.displayName = I18N.instance.translateKeyFormat("item.cosmetics.name", humanEntity.name)
                    }
                }

        val navigator = iconModule.newIcon("navigator")
                .item {
                    material = Material.COMPASS
                    displayName = I18N.instance.translateKey("item.navigator.name")
                    lore(I18N.instance.translateKey("item.navigator.desc"))
                }

        server.pluginManager.registerEvents(
                ServerNetworkListener(
                        lobbyMode,
                        clockwork,
                        cosmetics,
                        navigator),
                this)

        if (lobbyMode) {
            clockwork.handler(LobbyClockWork(app, "/")).register()
            cosmetics.handler(LobbyClockWork(app, "/cosmetic")).register()
            navigator.handler(StrictHandler()).register()
        } else {
            clockwork.handler(SurvivalClockWork(app, "/")).register()
            cosmetics.handler(SurvivalClockWork(app, "/cosmetic")).register()
            navigator.handler(IconHandler()).register()
        }
    }

//    override fun onDisable() {
//
//    }

    companion object {
        @JvmStatic val instance: ServerNetworkPlugin
            get() = _instance
        private lateinit var _instance: ServerNetworkPlugin
    }
}