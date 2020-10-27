package com.crypto.klinechart.component

import android.graphics.Color
import com.crypto.klinechart.internal.utils.Utils

class Indicator {
    /**
     * Indicator Type
     */
    class Type {
        companion object {
            /**
             * NO
             */
            const val NO = "no"

            /**
             * MA
             */
            const val MA = "ma"

            /**
             * VOLUME
             */
            const val VOL = "vol"

            /**
             * MACD
             */
            const val MACD = "macd"

            /**
             * BOLL
             */
            const val BOLL = "boll"

            /**
             * KDJ
             */
            const val KDJ = "kdj"

            /**
             * RSI
             */
            const val RSI = "rsi"
        }
    }

    /**
     * line size
     */
    var lineSize = Utils.convertDpToPixel(1f)

    /**
     * increasing color
     */
    var increasingColor = Color.parseColor("#5DB300")

    /**
     * decreasing color
     */
    var decreasingColor = Color.parseColor("#FF4A4A")

    /**
     * line colors array
     */
    var lineColors = intArrayOf(
        Color.parseColor("#898989"),
        Color.parseColor("#F5A623"),
        Color.parseColor("#F601FF"),
        Color.parseColor("#1587DD"),
        Color.parseColor("#50A300")
    )
}
