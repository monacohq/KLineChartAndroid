package com.crypto.klinechart.component

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import com.crypto.klinechart.internal.utils.Utils

class Candle {
    /**
     * Custom Draw Price Mark Listener
     */
    interface DrawPriceMarkListener {
        companion object {
            /**
             * HIGHEST
             */
            const val HIGHEST = 0

            /**
             * LOWEST
             */
            const val LOWEST = 1

            /**
             * LAST
             */
            const val LAST = 2
        }

        /**
         * draw
         * @param canvas Canvas
         * @param paint Paint
         * @param drawType Int
         * @param point PointF
         * @param chartRect RectF
         * @param candle Candle
         * @param price Double
         */
        fun draw(canvas: Canvas, paint: Paint, drawType: Int, point: PointF, chartRect: RectF, candle: Candle, price: Double)
    }

    /**
     * CandleStyle
     */
    class CandleStyle {
        companion object {
            /**
             * SOLID
             */
            const val SOLID = 0

            /**
             * STROKE
             */
            const val STROKE = 1

            /**
             * INCREASING_STROKE
             */
            const val INCREASING_STROKE = 2

            /**
             * DECREASING_STROKE
             */
            const val DECREASING_STROKE = 3

            /**
             * OHLC
             */
            const val OHLC = 4
        }
    }

    /**
     * ChartStyle
     */
    class ChartStyle {
        companion object {
            /**
             * CANDLE
             */
            const val CANDLE = 0

            /**
             * TIME LINE
             */
            const val TIME_LINE = 1
        }
    }

    /**
     * ValueFormatter
     */
    interface ValueFormatter {
        /**
         * format
         * @param value String?
         * @return String
         */
        fun format(value: String?): String
    }

    /**
     * increasingColor
     */
    var increasingColor = Color.parseColor("#5DB300")

    /**
     * decreasingColor
     */
    var decreasingColor = Color.parseColor("#FF4A4A")

    /**
     * candleStyle
     */
    var candleStyle =
        CandleStyle.SOLID

    /**
     * chartStyle
     */
    var chartStyle =
        ChartStyle.CANDLE

    /**
     * displayHighestPriceMark
     */
    var displayHighestPriceMark = true

    /**
     * displayLowestPriceMark
     */
    var displayLowestPriceMark = true

    /**
     * lowestHighestPriceMarkTextColor
     */
    var lowestHighestPriceMarkTextColor = Color.parseColor("#898989")

    /**
     * lowestHighestPriceMarkTextSize
     */
    var lowestHighestPriceMarkTextSize = Utils.convertDpToPixel(10f)

    /**
     * displayLastPriceMark
     */
    var displayLastPriceMark = true

    /**
     * lastPriceMarkLineStyle
     */
    var lastPriceMarkLineStyle =
        Component.LineStyle.DASH

    /**
     * lastPriceMarkLineDashValues
     */
    var lastPriceMarkLineDashValues = floatArrayOf(15f, 10f)

    /**
     * lastPriceMarkLineSize
     */
    var lastPriceMarkLineSize = Utils.convertDpToPixel(1f)

    /**
     * lastPriceMarkLineColor
     */
    var lastPriceMarkLineColor = Color.parseColor("#B9B9B9")

    /**
     * timeLineSize
     */
    var timeLineSize = Utils.convertDpToPixel(1f)

    /**
     * timeLineColor
     */
    var timeLineColor = Color.parseColor("#D8D8D8")

    /**
     * timeLineFillColor
     */
    var timeLineFillColor = Color.parseColor("#20D8D8D8")

    /**
     * timeAverageLineColor
     */
    var timeAverageLineColor = Color.parseColor("#F5A623")

    /**
     * drawPriceMarkListener
     */
    var drawPriceMarkListener: DrawPriceMarkListener? = null

    /**
     * valueFormatter
     */
    var valueFormatter: ValueFormatter? = null
}
