package com.dummyc0m.pylon.bukkit.servernetwork.cosmetic

import com.dummyc0m.pylon.bukkit.servernetwork.I18N

enum class Rank(val prefix: String) {
    GENTLEMAN(I18N.instance.translateKey("rank.gentleman")),
    KNIGHT(I18N.instance.translateKey("rank.knight")),
    BARON(I18N.instance.translateKey("rank.baron")),
    VISCOUNT(I18N.instance.translateKey("rank.viscount")),
    EARL(I18N.instance.translateKey("rank.earl")),
    MARQUESS(I18N.instance.translateKey("rank.marquess")),
    DUKE(I18N.instance.translateKey("rank.duke"));

    companion object {
        private val values = values()

        fun getRank(rank: Int): Rank {
            if (rank in 0..values.lastIndex) {
                return values[rank]
            }
            return GENTLEMAN
        }
    }
}