package com.dummyc0m.pylon.bukkit.servernetwork.app.convert

import com.dummyc0m.pylon.bukkit.servernetwork.I18N
import com.dummyc0m.pylon.bukkit.servernetwork.PriceConfig
import com.dummyc0m.pylon.bukkit.servernetwork.cosmetic.CosmeticModule
import com.dummyc0m.pylon.datakit.bukkitcommons.app.Menu
import com.dummyc0m.pylon.datakit.bukkitcommons.app.MenuView
import com.dummyc0m.pylon.datakit.bukkitcommons.app.Router
import com.dummyc0m.pylon.datakit.bukkitcommons.app.view.RootElement
import com.dummyc0m.pylon.datakit.bukkitcommons.app.view.menu
import com.dummyc0m.pylon.datakit.bukkitcommons.econ.EconModule
import com.dummyc0m.pylon.datakit.bukkitcommons.util.getItem
import com.dummyc0m.pylon.datakit.bukkitcommons.util.setItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Created by Dummy on 7/17/16.
 */
class ConversionMenu(private val router: Router,
                     private val priceConfig: PriceConfig,
                     private val econModule: EconModule,
                     private val cosmeticModule: CosmeticModule) : Menu() {
    override val enableBottom: Boolean = false
    override val root: RootElement = menu {
        title = I18N.instance.translateKey("menu.conversion.title")
        topSize = 5
        //frame
        for (i in 0..7) {
            static {
                x = i
                material = Material.STAINED_GLASS_PANE
                displayName = ""
            }
        }
        //return button
        static {
            x = 8
            material = Material.STAINED_GLASS_PANE
            damage = 14
            displayName = I18N.instance.translateKey("menu.conversion.returnButton.title")
            onClick { menuView, clickType, cursor ->
                dropAllItems(menuView)
                router.routeTo(menuView.player)
            }
        }
        //frame
        for (i in 1..3) {
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
        //conversion slots
        for (y in 1..3) {
            for (x in 1..7) {
                empty {
                    this.x = x
                    this.y = y
                    onClick(blankClickHandler)
                }
            }
        }
        //frame and confirm, cancel
        for (i in 0..8) {
            when (i) {
                2 -> static {
                    x = 2
                    y = 4
                    material = Material.STAINED_GLASS_PANE
                    damage = 5
                    displayName = I18N.instance.translateKey("menu.conversion.confirmButton.title")
                    lore(I18N.instance.translateKeyFormat("menu.conversion.confirmButton.desc", 0, 0))
                    enchant(Enchantment.LUCK, 1)
                    flag(ItemFlag.HIDE_ENCHANTS)
                    onClick { menuView, clickType, cursor ->
                        convert(menuView, clickType, cursor)
                        router.routeTo(menuView.player)
                    }
                }
                6 -> static {
                    x = 6
                    y = 4
                    material = Material.STAINED_GLASS_PANE
                    damage = 7
                    displayName = I18N.instance.translateKey("menu.conversion.cancelButton.title")
                    onClick { menuView, clickType, cursor ->
                        dropAllItems(menuView)
                        router.routeTo(menuView.player)
                    }
                }
                else -> static {
                    x = i
                    y = 4
                    material = Material.STAINED_GLASS_PANE
                    displayName = ""
                }
            }
        }
        //important!!!
        onClose { menuView -> dropAllItems(menuView) }
    }


    val blankClickHandler: (MenuView, ClickType, ItemStack) -> Boolean = { menuView, clickType, cursor ->
        when (cursor.type) {
            Material.NETHER_STAR, Material.DIAMOND, Material.EMERALD, Material.QUARTZ -> {
                val confirmItem = menuView.getItem(2, 4)
                if (confirmItem != null) {
                    resetConfirmItem(confirmItem, menuView, clickType, cursor)
                }
                false
            }
            Material.SKULL_ITEM -> {
                if (cursor.durability === 4.toShort()) {
                    val confirmItem = menuView.getItem(2, 4)
                    if (confirmItem != null) {
                        resetConfirmItem(confirmItem, menuView, clickType, cursor)
                    }
                    false
                } else {
                    true
                }
            }
            Material.GOLDEN_APPLE -> {
                if (cursor.durability === 1.toShort()) {
                    val confirmItem = menuView.getItem(2, 4)
                    if (confirmItem != null) {
                        resetConfirmItem(confirmItem, menuView, clickType, cursor)
                    }
                    false
                } else {
                    true
                }
            }
            else -> {
                true
            }
        }
    }

    fun convert(menuView: MenuView, clickType: ClickType, cursor: ItemStack) {
        val econData = econModule.getPlayerData(menuView.player.uniqueId)
        val cosmeticData = cosmeticModule.getData(menuView.player.uniqueId)
        if (econData !== null && cosmeticData !== null) {
            val (a, b) = calcAmount(menuView)
            consumeAllItems(menuView)
            if (a !== 0)
                econData.change(a)
            if (b !== 0)
                cosmeticData.change(b)
        }
    }

    fun consumeAllItems(menuView: MenuView) {
        for (y in 1..3) {
            for (x in 1..7) {
                val item = menuView.getItem(x, y)
                if (item != null) {
                    menuView.setItem(x, y, null)
                }
            }
        }
    }

    fun dropAllItems(menuView: MenuView) {
        val player = menuView.player
        for (y in 1..3) {
            for (x in 1..7) {
                val item = menuView.getItem(x, y)
                if (item != null) {
                    player.world.dropItem(player.location, item)
                }
            }
        }
    }

    fun calcAmount(menuView: MenuView): Pair<Int, Int> {
        var copper = 0
        var silver = 0
        for (y in 1..3) {
            for (x in 1..7) {
                val item = menuView.getItem(x, y)
                if (item != null) {
                    val copperVal = getCopperValue(item) * item.amount
                    if (copperVal === 0) {
                        val silverVal = getSilverValue(item) * item.amount
                        if (silverVal === 0) {
                            menuView.setItem(x, y, null)
                            val player = menuView.player
                            player.world.dropItemNaturally(player.location, item)
                        } else {
                            silver += silverVal
                        }
                    } else {
                        copper += copperVal
                    }
                }
            }
        }
        return Pair(copper, silver)
    }

    fun getCopperValue(item: ItemStack): Int {
        when (item.type) {
            Material.NETHER_STAR -> {
                return priceConfig.netherStarToCopper
            }
            Material.DIAMOND -> {
                return priceConfig.diamondToCopper
            }
            Material.EMERALD -> {
                return priceConfig.emeraldToCopper
            }
            Material.QUARTZ -> {
                return priceConfig.quartzToCopper
            }
            else -> {
                return 0
            }
        }
    }

    fun getSilverValue(item: ItemStack): Int {
        when (item.type) {
            Material.SKULL_ITEM -> {
                if (item.durability === 4.toShort()) {
                    return priceConfig.creeperSkullToSilver
                }
                return 0
            }
            Material.GOLDEN_APPLE -> {
                if (item.durability === 1.toShort()) {
                    return priceConfig.notchAppleToSilver
                }
                return 0
            }
            else -> {
                return 0
            }
        }
    }

    fun resetConfirmItem(confirmItem: ItemStack, menuView: MenuView, clickType: ClickType, cursor: ItemStack) {
        val confirmMeta = confirmItem.itemMeta
        val (a, b) = calcAmount(menuView)
        var copper = getCopperValue(cursor)
        var silver = getSilverValue(cursor)
        if (clickType === ClickType.LEFT) {
            copper *= cursor.amount
            silver *= cursor.amount
        }
        confirmMeta.lore = listOf(
                I18N.instance.translateKeyFormat("menu.conversion.confirmButton.desc",
                        a + copper,
                        b + silver))
        confirmItem.itemMeta = confirmMeta
    }
}