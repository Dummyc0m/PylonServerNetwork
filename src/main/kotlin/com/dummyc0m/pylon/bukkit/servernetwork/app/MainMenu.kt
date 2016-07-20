package com.dummyc0m.pylon.bukkit.servernetwork.app

import com.dummyc0m.pylon.bukkit.servernetwork.I18N
import com.dummyc0m.pylon.datakit.bukkitcommons.app.Menu
import com.dummyc0m.pylon.datakit.bukkitcommons.app.MenuView
import com.dummyc0m.pylon.datakit.bukkitcommons.app.Router
import com.dummyc0m.pylon.datakit.bukkitcommons.app.view.RootElement
import com.dummyc0m.pylon.datakit.bukkitcommons.app.view.menu
import com.dummyc0m.pylon.datakit.bukkitcommons.conversation.ChatTriggerModule
import com.dummyc0m.pylon.datakit.bukkitcommons.econ.EconModule
import com.dummyc0m.pylon.datakit.bukkitcommons.util.MathUtil
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.util.ChatPaginator
import java.util.*

/**
 * Created by Dummy on 7/17/16.
 */
class MainMenu(private val router: Router,
               private val chatTriggerModule: ChatTriggerModule,
               private val econModule: EconModule) : Menu() {
    override val enableBottom: Boolean = false
    override val root: RootElement = menu {
        title = I18N.instance.translateKey("menu.main.title")
        topSize = 3
        static {
            x = 2
            y = 1
            material = Material.ANVIL
            displayName = I18N.instance.translateKey("menu.main.conversionButton.title")
            for (line in ChatPaginator.wordWrap(I18N.instance.translateKey("menu.main.conversionButton.desc"), 20)) {
                lore(line)
            }

            onClick { menuView, clickType, cursor -> router.routeTo(menuView.player, "/convert") }
        }

        static {
            x = 4
            y = 1
            material = Material.SKULL_ITEM
            damage = 3
            displayName = I18N.instance.translateKey("menu.main.cosmeticButton.title")
            for (line in ChatPaginator.wordWrap(I18N.instance.translateKey("menu.main.cosmeticButton.desc"), 20)) {
                lore(line)
            }
            enchant(Enchantment.LUCK, 1)
            flag(ItemFlag.HIDE_ENCHANTS)
            onRender { itemMeta, humanEntity -> if (itemMeta is SkullMeta) itemMeta.owner = humanEntity.name }
            onClick { menuView, clickType, cursor -> router.routeTo(menuView.player, "/cosmetic") }
        }

        static {
            x = 6
            y = 1
            material = Material.BRICK
            displayName = "${I18N.instance.translateKey("menu.main.remitButton.title")}"
            for (line in ChatPaginator.wordWrap(I18N.instance.translateKey("menu.main.remitButton.desc"), 20)) {
                lore(line)
            }
            onClick { menuView, clickType, cursor -> remit(menuView) }
        }
    }

    fun remit(menuView: MenuView) {
        val player = menuView.player
        router.hide(player)
        player.sendMessage(I18N.instance.translateKey("chat.remit.recipient"))
        val recipient: Array<Player?> = arrayOf(null)

        chatTriggerModule.onChat(menuView.player.uniqueId).then { event ->
            event.isCancelled = true
            recipient[0] = event.player.server.getPlayer(
                    UUID.nameUUIDFromBytes("OfflinePlayer:${event.message}"
                            .toByteArray(Charsets.UTF_8)))
            player.sendMessage(event.message)
            if (recipient[0] != null && recipient[0]?.uniqueId != player.uniqueId) {
                player.sendMessage(I18N.instance.translateKey("chat.remit.silver"))
                true
            } else {
                player.sendMessage(I18N.instance.translateKey("chat.remit.invalidRecipient"))
                false
            }
        }.next().then { event ->
            event.isCancelled = true
            player.sendMessage(event.message)
            val recipientLocal = recipient[0]
            if (recipientLocal != null && recipientLocal.isOnline && MathUtil.isInteger(event.message)) {
                val amount = event.message.toInt()
                val thisData = econModule.getPlayerData(player.uniqueId)
                val thatData = econModule.getPlayerData(recipientLocal.uniqueId)
                if (thisData != null && thatData != null) {
                    if (amount <= thisData.money.get()) {
                        thisData.change(-amount)
                        thatData.change(amount)
                    } else {
                        player.sendMessage(I18N.instance.translateKey("chat.remit.insufficientBalance"))
                    }
                } else {
                    player.sendMessage(I18N.instance.translateKeyFormat("chat.general.unknownError", "Remittance", "DataUninitializedError"))
                }
            } else {
                player.sendMessage(I18N.instance.translateKeyFormat("chat.general.unknownError", "Remittance", "InvalidInput"))
            }
            false
        }
    }
}