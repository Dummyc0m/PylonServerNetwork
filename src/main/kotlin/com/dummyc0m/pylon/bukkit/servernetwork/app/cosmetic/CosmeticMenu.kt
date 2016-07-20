package com.dummyc0m.pylon.bukkit.servernetwork.app.cosmetic

import com.dummyc0m.pylon.bukkit.servernetwork.I18N
import com.dummyc0m.pylon.bukkit.servernetwork.PriceConfig
import com.dummyc0m.pylon.bukkit.servernetwork.cosmetic.CosmeticModule
import com.dummyc0m.pylon.datakit.bukkitcommons.app.Menu
import com.dummyc0m.pylon.datakit.bukkitcommons.app.MenuView
import com.dummyc0m.pylon.datakit.bukkitcommons.app.Router
import com.dummyc0m.pylon.datakit.bukkitcommons.app.view.ContainerElement
import com.dummyc0m.pylon.datakit.bukkitcommons.app.view.RootElement
import com.dummyc0m.pylon.datakit.bukkitcommons.app.view.getItem
import com.dummyc0m.pylon.datakit.bukkitcommons.app.view.menu
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.meta.ItemMeta

/**
 * Created by Dummy on 7/17/16.
 */
class CosmeticMenu(private val router: Router,
                   private val cosmeticModule: CosmeticModule,
                   private val priceConfig: PriceConfig) : Menu() {
    //full view!!!
    override val enableBottom = true
    override val root: RootElement = menu {
        title = I18N.instance.translateKey("menu.cosmetic.title")
        topSize = 6
        //cosmetic content block
        container(ContainerElement()) {
            x = 1
            y = 1
            //blue
            static {
                material = Material.INK_SACK
                damage = 4
                displayName = I18N.instance.translateKey("menu.cosmetic.chatColor.blue.title")
                onRender { meta, he -> chatColorRender(meta, he, "Blue") }
                onClick { menuView, clickType, itemStack -> chatColorClick(menuView, "Blue", ChatColor.DARK_BLUE, x, y) }
            }
            //purple
            static {
                x = 1
                material = Material.INK_SACK
                damage = 5
                displayName = I18N.instance.translateKey("menu.cosmetic.chatColor.purple.title")
                onRender { meta, he -> chatColorRender(meta, he, "Purple") }
                onClick { menuView, clickType, itemStack -> chatColorClick(menuView, "Purple", ChatColor.DARK_PURPLE, x, y) }
            }
            //pink
            static {
                x = 2
                material = Material.INK_SACK
                damage = 9
                displayName = I18N.instance.translateKey("menu.cosmetic.chatColor.pink.title")
                onRender { meta, he -> chatColorRender(meta, he, "Pink") }
                onClick { menuView, clickType, itemStack -> chatColorClick(menuView, "Pink", ChatColor.LIGHT_PURPLE, x, y) }
            }
            //green
            static {
                x = 3
                material = Material.INK_SACK
                damage = 10
                displayName = I18N.instance.translateKey("menu.cosmetic.chatColor.green.title")
                onRender { meta, he -> chatColorRender(meta, he, "Green") }
                onClick { menuView, clickType, itemStack -> chatColorClick(menuView, "Green", ChatColor.GREEN, x, y) }
            }
            //lightblue
            static {
                x = 4
                material = Material.INK_SACK
                damage = 12
                displayName = I18N.instance.translateKey("menu.cosmetic.chatColor.lightBlue.title")
                onRender { meta, he -> chatColorRender(meta, he, "LightBlue") }
                onClick { menuView, clickType, itemStack -> chatColorClick(menuView, "LightBlue", ChatColor.BLUE, x, y) }
            }
            //orange
            static {
                x = 5
                material = Material.INK_SACK
                damage = 14
                displayName = I18N.instance.translateKey("menu.cosmetic.chatColor.orange.title")
                onRender { meta, he -> chatColorRender(meta, he, "Orange") }
                onClick { menuView, clickType, itemStack -> chatColorClick(menuView, "Orange", ChatColor.GOLD, x, y) }
            }
            //orange
            static {
                x = 6
                material = Material.INK_SACK
                damage = 15
                displayName = I18N.instance.translateKey("menu.cosmetic.chatColor.white.title")
                onRender { meta, he -> chatColorRender(meta, he, "White") }
                onClick { menuView, clickType, itemStack -> chatColorClick(menuView, "White", ChatColor.WHITE, x, y) }
            }
            //chat colors 4blue 5purple 9pink 10green 12lightblue 14orange 15white
        }
        //return button
        static {
            x = 8
            material = Material.STAINED_GLASS_PANE
            damage = 14
            displayName = I18N.instance.translateKey("menu.cosmetic.returnButton.title")
            onClick { menuView, clickType, cursor -> router.routeSafelyTo(menuView.player) }
        }
        //frame
        for (i in 0..7) {
            static {
                x = i
                material = Material.STAINED_GLASS_PANE
                displayName = ""
            }
        }
        for (i in 1..9) {
            static {
                x = 0
                y = i
                material = Material.STAINED_GLASS_PANE
                displayName = ""
            }
            static {
                x = 8
                y = i
                material = Material.STAINED_GLASS_PANE
                displayName = ""
            }
        }
        for (i in 1..7) {
            static {
                x = i
                y = 9
                material = Material.STAINED_GLASS_PANE
                displayName = ""
            }
        }
    }

    fun chatColorClick(menuView: MenuView, colorCode: String, chatColor: ChatColor, x: Int, y: Int) {
        val cosmeticData = cosmeticModule.getData(menuView.player.uniqueId)
        if (cosmeticData != null) {
            if (cosmeticData.getBoolean("chatColor$colorCode")) {
                cosmeticData.setChatColorValue(chatColor)
            } else if (cosmeticData.money.get() >= priceConfig.chatColorPrice) {
                cosmeticData.change(-priceConfig.chatColorPrice)
                cosmeticData.setBooleanSafely("chatColor$colorCode", true)
                cosmeticData.setChatColorValue(chatColor)
                val thisItem = menuView.getItem(x, y)
                if (thisItem !== null) {
                    val meta = thisItem.itemMeta
                    meta.lore = listOf(I18N.instance.translateKey("menu.cosmetic.chatColor.equip"))
                    thisItem.itemMeta = meta
                }
            }
        }
    }

    fun chatColorRender(meta: ItemMeta, he: HumanEntity, colorCode: String) {
        val cosmeticData = cosmeticModule.getData(he.uniqueId)
        if (cosmeticData != null) {
            if (cosmeticData.getBoolean("chatColor$colorCode")) {
                meta.lore = listOf(I18N.instance.translateKey("menu.cosmetic.chatColor.equip"))
            } else if (cosmeticData.money.get() >= priceConfig.chatColorPrice) {
                meta.lore = listOf(I18N.instance.translateKey("menu.cosmetic.chatColor.buy"))
            } else {
                meta.lore = listOf(I18N.instance.translateKeyFormat("menu.cosmetic.chatColor.insufficient", priceConfig.chatColorPrice))
            }
        }
    }
}