package com.crypto.klinechart.component

import com.crypto.klinechart.internal.utils.Utils
import kotlin.math.max
import kotlin.math.min

class YAxis : Axis() {
    /**
     * y-axis position
     */
    class AxisPosition {
        companion object {
            /**
             * LEFT
             */
            const val LEFT = 0

            /**
             * RIGHT
             */
            const val RIGHT = 1
        }
    }

    /**
     * TextPosition
     */
    class TextPosition {
        companion object {
            /**
             * OUTSIDE
             */
            const val OUTSIDE = 0

            /**
             * INSIDE
             */
            const val INSIDE = 1
        }
    }

    /**
     * ValueFormatter
     */
    interface ValueFormatter {
        /**
         * format
         * @param indicatorType
         * @param value String
         */
        fun format(indicatorType: String, value: Double): String
    }

    /**
     * set y-axis text position
     */
    var yAxisTextPosition =
        TextPosition.INSIDE

    /**
     * set y-axis position
     */
    var yAxisPosition =
        AxisPosition.RIGHT

    /**
     * set y-axis max width
     */
    var yAxisMaxWidth = 0f

    /**
     * set y-axis min width
     */
    var yAxisMinWidth = 0f

    /**
     * value formatter
     */
    var valueFormatter: ValueFormatter? = null

    /**
     * y-axis whether need margin or not
     * @return Boolean
     */
    fun needsOffset() = ((displayTickText || displayTickLine || textMarginSpace > 0f) && yAxisTextPosition == TextPosition.OUTSIDE) || displayAxisLine

    /**
     * get y-axis required width
     * @return Float
     */
    fun getRequiredWidthSpace(): Float {

        paint.textSize = tickTextSize

        var width = 0f
        if (yAxisTextPosition == TextPosition.OUTSIDE) {
            width += Utils.getTextWidth(paint, "0000000") + textMarginSpace
            if (displayTickLine) {
                width += tickLineSize
            }
        }

        if (displayAxisLine) {
            width += axisLineSize
        }

        val maxWidth = if (yAxisMaxWidth > 0f) yAxisMaxWidth else width
        width = min(maxWidth, max(width, yAxisMinWidth))
        return width
    }
}
