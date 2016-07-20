package com.dummyc0m.pylon.bukkit.servernetwork.cosmetic

import com.dummyc0m.pylon.bukkit.servernetwork.I18N
import com.dummyc0m.pylon.datakit.bukkit.DataKitBukkit
import com.dummyc0m.pylon.datakit.bukkit.DataReceiveEvent
import com.dummyc0m.pylon.datakit.bukkit.DeltaReceiveEvent
import com.dummyc0m.pylon.datakit.bukkitcommons.icon.Icon
import com.dummyc0m.pylon.datakit.extlib.com.fasterxml.jackson.databind.node.BooleanNode
import com.dummyc0m.pylon.datakit.extlib.com.fasterxml.jackson.databind.node.ObjectNode
import com.dummyc0m.pylon.datakit.extlib.com.fasterxml.jackson.databind.node.TextNode
import com.dummyc0m.pylon.datakit.network.message.DeltaMessage
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.*
import java.util.*

class CosmeticListener(private val cosmeticModule: CosmeticModule,
                       private val dataKit: DataKitBukkit,
                       private val lobbyMode: Boolean,
                       private val elytraIcon: Icon,
                       private val dragonHeadIcon: Icon) : Listener {
    private val random = Random()

    @EventHandler(ignoreCancelled = true)
    fun onPlayerMove(playerMoveEvent: PlayerMoveEvent) {
        if (lobbyMode) {
            val player = playerMoveEvent.player
            if (playerMoveEvent.to.blockY !in 0..256) {
                playerMoveEvent.to = player.world.spawnLocation
            }
            val cosmeticData = cosmeticModule.getData(player.uniqueId)
            if (cosmeticData !== null) {
                if (cosmeticData.getBoolean("doubleJumpOn") && player.world.getBlockAt(player.location.add(0.0, -1.0, 0.0)).type !== Material.AIR) {
                    player.allowFlight = true
                }
                if (cosmeticData.getBoolean("smokeTrailOn")) {
                    player.world.spawnParticle(Particle.SMOKE_NORMAL, player.location, 3)
                }
                if (cosmeticData.getBoolean("rainbowTrailOn")) {
                    player.world.spawnParticle(Particle.REDSTONE, player.location, 3, random.nextDouble(), random.nextDouble(), random.nextDouble())
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerFly(playerToggleFlightEvent: PlayerToggleFlightEvent) {
        val player = playerToggleFlightEvent.player
        if (lobbyMode && player.gameMode !== GameMode.CREATIVE) {
            val cosmeticData = cosmeticModule.getData(player.uniqueId)
            //double jump start
            if (cosmeticData !== null && cosmeticData.getBoolean("doubleJumpOn")) {
                player.allowFlight = false
                playerToggleFlightEvent.isCancelled = true
                player.velocity = player.velocity.multiply(5)
                player.world.playEffect(player.location, Effect.SMOKE, 0)
                player.world.playEffect(player.location, Effect.WITHER_SHOOT, 0)
            }
            //double jump end
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerDamage(playerDamageEvent: EntityDamageEvent) {
        if (lobbyMode && playerDamageEvent.entityType === EntityType.PLAYER) {
            when (playerDamageEvent.cause) {
                EntityDamageEvent.DamageCause.CUSTOM, EntityDamageEvent.DamageCause.SUICIDE, EntityDamageEvent.DamageCause.VOID -> {
                }
                else -> playerDamageEvent.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onDeltaReceive(deltaReceiveEvent: DeltaReceiveEvent) {
        val cosmeticData = cosmeticModule.getData(deltaReceiveEvent.player.uniqueId)
        if (cosmeticData !== null) {
            for ((key, value) in deltaReceiveEvent.dataMap) {
                if (key.startsWith("cosmetic.")) {
                    val element = key.substring(9)
                    when (value) {
                        is BooleanNode -> cosmeticData.setBooleanTemporarily(element, value.asBoolean())
                        is TextNode -> {
                            if (element.equals("chatColorValue")) {
                                cosmeticData.setChatColorValue(ChatColor.getByChar(value.asText("7")))
                            } else {
                                println("MDZZ... I don't know what this stuff is $key $value (cosmetic)")
                            }
                        }
                        else -> {
                            println("MDZZ... I don't know what this stuff is $key $value (cosmetic)")
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onDataReceive(dataReceiveEvent: DataReceiveEvent) {
        val cosmeticNode: ObjectNode? = dataReceiveEvent.jsonData.get("cosmetic") as ObjectNode?
        val cosmeticData = CosmeticData(dataReceiveEvent.player.uniqueId, cosmeticNode, dataKit)
        cosmeticModule.addData(cosmeticData)
        if (cosmeticData.getRank() !== Rank.GENTLEMAN && cosmeticData.getRankExpiration() < System.currentTimeMillis()) {
            cosmeticData.setRank(Rank.GENTLEMAN, 0)
        }
        Bukkit.getScheduler().runTask(dataKit) {
            val player = dataReceiveEvent.player
            val rank = cosmeticData.getRank()
            if (lobbyMode) {
                if (cosmeticData.getBoolean("dragonHeadOn")) {
                    player.inventory.helmet = dragonHeadIcon.getItem(player)
                }
                if (cosmeticData.getBoolean("speedOn")) {
                    player.walkSpeed *= 1.5F
                }
                if (rank > Rank.GENTLEMAN) {
                    if (cosmeticData.getBoolean("doubleJumpOn")) {
                        player.allowFlight = true
                    }
                }
                if (rank > Rank.KNIGHT) {
                    if (cosmeticData.getBoolean("elytraOn")) {
                        player.inventory.chestplate = elytraIcon.getItem(player)
                    }
                }
                if (rank > Rank.VISCOUNT) {
                    if (cosmeticData.getBoolean("flightOn")) {
                        player.allowFlight = true
                        player.isFlying = true
                    }
                }
                if (rank > Rank.BARON) {
                    if (cosmeticData.getBoolean("customPrefix")) {
                        player.displayName = rank.prefix + cosmeticData.getCustomPrefixValue() + player.name
                        player.server.broadcastMessage(I18N.instance.translateKeyFormat("chat.cosmetic.joinMessage", rank.prefix + cosmeticData.getCustomPrefixValue(), player.name))
                    } else {
                        player.displayName = rank.prefix + player.name
                        player.server.broadcastMessage(I18N.instance.translateKeyFormat("chat.cosmetic.joinMessage", rank.prefix, player.name))
                    }
                } else if (rank > Rank.GENTLEMAN) {
                    player.displayName = rank.prefix + player.name
                    player.server.broadcastMessage(I18N.instance.translateKeyFormat("chat.cosmetic.joinMessage", rank.prefix, player.name))
                }
            } else {
                if (rank > Rank.BARON && cosmeticData.getBoolean("customPrefix")) {
                    player.server.broadcastMessage(I18N.instance.translateKeyFormat("chat.cosmetic.joinMessage", rank.prefix + cosmeticData.getCustomPrefixValue(), player.name))
                } else {
                    player.server.broadcastMessage(I18N.instance.translateKeyFormat("chat.cosmetic.joinMessage", rank.prefix, player.name))
                }
            }

        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerChat(playerChatEvent: AsyncPlayerChatEvent) {
        val cosmeticData = cosmeticModule.getData(playerChatEvent.player.uniqueId)
        if (cosmeticData !== null) {
            val rank = cosmeticData.getRank()
            if (rank > Rank.BARON && cosmeticData.getBoolean("customPrefix")) {
                playerChatEvent.format = I18N.instance.translateKeyFormat("chat.cosmetic.chatFormat", rank.prefix + cosmeticData.getCustomPrefixValue(), cosmeticData.getChatColorValue())
            } else {
                playerChatEvent.format = I18N.instance.translateKeyFormat("chat.cosmetic.chatFormat", rank.prefix, cosmeticData.getChatColorValue())
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerJoin(playerJoinEvent: PlayerJoinEvent) {
        playerJoinEvent.joinMessage = null
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerDisconnect(playerQuitEvent: PlayerQuitEvent) {
        playerQuitEvent.quitMessage = null
        //save cosmetic data and reset
        val cosmeticData = cosmeticModule.getData(playerQuitEvent.player.uniqueId)
        if (cosmeticData !== null) {
            val delta = DeltaMessage()
            delta.offlineUUID = playerQuitEvent.player.uniqueId
            delta.dereference = true
            for ((key, value) in cosmeticData.changeBuffer) {
                when (value) {
                    is Boolean -> delta.setDelta("cosmetic.$key", BooleanNode.valueOf(value))
                //is Int -> delta.setDelta("cosmetic.$key", TextNode(java.lang.String.valueOf(value)))
                //is String -> delta.setDelta("cosmetic.$key", TextNode(value))
                    is ChatColor -> delta.setDelta("cosmetic.$key", TextNode(java.lang.String.valueOf(value.char)))
                //is Rank -> delta.setDelta("cosmetic.$key", TextNode(java.lang.String.valueOf(value.ordinal)))
                    else -> {
                        println("Non-resolvable attribute $key:$value")
                    }
                }
            }
            dataKit.send(delta)
            cosmeticModule.drop(playerQuitEvent.player.uniqueId)
        }
    }
}