package com.crypto.klinechart.component

import com.crypto.klinechart.internal.utils.Utils
import kotlin.math.max
import kotlin.math.min

class XAxis : Axis() {
    /**
     * ValueFormatter
     */
    interface ValueFormatter {
        /**
         * format
         * @param value String
         */
        fun format(value: Long): String
    }

    /**
     * x-axis max height
     */
    var xAxisMaxHeight = 0f

    /**
     * x-axis min height
     */
    var xAxisMinHeight = 0f

    /**
     * ValueFormatter
     */
    var valueFormatter: ValueFormatter? = null

    /**
     * get x-axis required height
     * @return Float
     */
    fun getRequiredHeightSpace(): Float {

        paint.textSize = tickTextSize

        var height = Utils.getTextHeight(paint, "T") + textMarginSpace
        if (displayTickLine) {
            height += tickLineSize
        }
        if (displayAxisLine) {
            height += axisLineSize
        }
        val maxHeight = if (xAxisMaxHeight > 0f) xAxisMaxHeight else height
        height = max(xAxisMinHeight, min(height, maxHeight))

        return height
    }
}
