package com.dummyc0m.pylon.bukkit.servernetwork.cosmetic

import com.dummyc0m.pylon.datakit.bukkit.DataKitBukkit
import com.dummyc0m.pylon.datakit.extlib.com.fasterxml.jackson.databind.node.BooleanNode
import com.dummyc0m.pylon.datakit.extlib.com.fasterxml.jackson.databind.node.IntNode
import com.dummyc0m.pylon.datakit.extlib.com.fasterxml.jackson.databind.node.ObjectNode
import com.dummyc0m.pylon.datakit.extlib.com.fasterxml.jackson.databind.node.TextNode
import com.dummyc0m.pylon.datakit.network.message.DeltaMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Thread-safe
 *
 * always use the safely method when doing things related to silver transaction, it guarantees data security, almost.
 */
class CosmeticData(val uuid: UUID, jsonData: ObjectNode?, private val dataKit: DataKitBukkit) {
    val money: AtomicInteger = AtomicInteger(0)
    val attributes: Map<String, Any>
        get() = attribute
    val changeBuffer: Map<String, Any>
        get() = bufferedChanges
    private val attribute: MutableMap<String, Any> = ConcurrentHashMap()
    private val bufferedChanges: MutableMap<String, Any> = ConcurrentHashMap()

    init {
        if (jsonData != null) {
            jsonData.fields().forEach { entry ->
                when (entry.key) {
                    "silver" -> money.set(entry.value.asInt(0))
                    "chatColorValue" -> attribute.put(entry.key, ChatColor.getByChar(entry.value.asText("7")))
                    "customPrefixValue" -> attribute.put(entry.key, entry.value.asText(""))
                    "rankExpiration" -> attribute.put(entry.key, entry.value.asText("0").toLong())
                    "rank" -> attribute.put(entry.key, Rank.getRank(entry.value.asText("0").toInt()))
                    else -> attribute.put(entry.key, entry.value.asBoolean(false))
                }
            }
        } else {
            attribute.put("rank", Rank.GENTLEMAN)
            attribute.put("rankExpiration", 0L)
        }
    }

    fun getBoolean(key: String): Boolean {
        val value = attribute[key] as Boolean?
        return if (value === null) false else value
    }

    // use this when setting ons and offs
    fun setBoolean(key: String, attr: Boolean) {
        if (key != "silver" || key != "customPrefixValue" || key != "rankExpiration" || key != "rank") {
            attribute.put(key, attr)
            bufferedChanges.put(key, attr)
        }
    }

    /**
     * internal method
     */
    internal fun setBooleanTemporarily(key: String, attr: Boolean) {
        attribute.put(key, attr)
    }

    // use this when doing silver related transactions
    fun setBooleanSafely(key: String, attr: Boolean) {
        if (key != "silver" || key != "customPrefixValue" || key != "rankExpiration" || key != "rank") {
            attribute.put(key, attr)
            Bukkit.getScheduler().runTaskAsynchronously(dataKit) {
                val message = DeltaMessage()
                message.offlineUUID = uuid
                message.setDelta("cosmetic.$key", BooleanNode.valueOf(attr))
                dataKit.send(message)
            }
        }
    }

    fun getChatColorValue(): ChatColor {
        val value = attribute["chatColorValue"] as ChatColor?
        return if (value === null) ChatColor.GRAY else value
    }

    /**
     * internal method
     */
    internal fun setChatColorValueTemporarily(attr: ChatColor) {
        attribute.put("chatColorValue", attr)
    }

    fun setChatColorValue(attr: ChatColor) {
        attribute.put("chatColorValue", attr)
        bufferedChanges.put("chatColorValue", attr)
    }

    fun getCustomPrefixValue(): String {
        val value = attribute["customPrefixValue"] as String?
        return if (value === null) "" else value
    }

    fun setCustomPrefixValue(attr: String) {
        attribute.put("customPrefixValue", attr)
        Bukkit.getScheduler().runTaskAsynchronously(dataKit) {
            val message = DeltaMessage()
            message.offlineUUID = uuid
            message.setDelta("cosmetic.customPrefixValue", TextNode(attr))
            dataKit.send(message)
        }
    }

    fun getRankExpiration(): Long {
        val value = attribute["rankExpiration"] as Long?
        return if (value === null) 0L else value
    }

    fun getRank(): Rank {
        val value = attribute["rank"] as Rank?
        return if (value === null) Rank.GENTLEMAN else value
    }

    fun setRank(attr: Rank, expiration: Long) {
        attribute.put("rank", attr)
        attribute.put("rankExpiration", expiration)
        Bukkit.getScheduler().runTaskAsynchronously(dataKit) {
            val message = DeltaMessage()
            message.offlineUUID = uuid
            message.setDelta("cosmetic.rank", TextNode(java.lang.String.valueOf(attr.ordinal)))
            message.setDelta("cosmetic.rankExpiration", TextNode(java.lang.String.valueOf(expiration)))
            dataKit.send(message)
        }
    }

    fun change(silver: Int) {
        money.addAndGet(silver)
        Bukkit.getScheduler().runTaskAsynchronously(dataKit) {
            val message = DeltaMessage()
            message.offlineUUID = uuid
            message.setDelta("cosmetic.silver", IntNode(silver))
            dataKit.send(message)
        }
    }
}