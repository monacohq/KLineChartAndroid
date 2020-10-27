package com.crypto.klinechart.component

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import com.crypto.klinechart.internal.utils.Utils
import com.crypto.klinechart.model.KLineModel

class Tooltip {
    /**
     * Indicator Display Rule
     */
    class IndicatorDisplayRule {
        companion object {
            /**
             * always
             */
            const val ALWAYS = 0

            /**
             * follow crosshair
             */
            const val FOLLOW_CROSS = 1

            /**
             * none
             */
            const val NONE = 2
        }
    }

    /**
     * Custom Draw General Data Listener
     */
    interface DrawGeneralDataListener {
        /**
         * @param canvas Canvas
         * @param paint Paint
         * @param kLineModel KLineModel
         * @param point PointF
         * @param tooltip Tooltip
         * @param chartRect RectF
         */
        fun draw(canvas: Canvas, paint: Paint, point: PointF, tooltip: Tooltip, chartRect: RectF, kLineModel: KLineModel)
    }

    /**
     * General Data Formatter
     */
    interface GeneralDataFormatter {
        /**
         * generate Labels
         * @return MutableList<String>
         */
        fun generatedLabels(): MutableList<String>

        /**
         * generate Values
         * @return MutableList<String>
         */
        fun generatedValues(kLineModel: KLineModel): MutableList<String>

        /**
         * generate Style
         * @param paint Paint
         */
        fun generatedStyle(paint: Paint, kLineModel: KLineModel, tooltip: Tooltip, labelPos: Int)
    }

    /**
     * Value Formatter
     */
    interface ValueFormatter {
        companion object {
            /**
             * value position x-axis
             */
            const val X_AXIS = 0
            /**
             * value position  y-axis
             */
            const val Y_AXIS = 1
            /**
             * value position chart
             */
            const val CHART = 2
        }

        /**
         * format
         * @param seat Int
         * @param indicatorType String?
         * @param value String
         * @return String
         */
        fun format(seat: Int, indicatorType: String?, value: String): String
    }

    /**
     * crosshairs style
     */
    var crosshairsStyle =
        Component.LineStyle.SOLID

    /**
     * crosshairs dash values
     */
    var crosshairsDashValues = floatArrayOf(8f, 6f)

    /**
     * crosshairs size
     */
    var crosshairsSize = 1f

    /**
     * crosshairs color
     */
    var crosshairsColor = Color.parseColor("#505050")

    /**
     * crosshairs text rect stroke line size
     */
    var crossTextRectStrokeLineSize = Utils.convertDpToPixel(1f)

    /**
     * crosshairs text rect stroke line color
     */
    var crossTextRectStrokeLineColor = Color.parseColor("#EDEDED")

    /**
     * crosshairs text rect color
     */
    var crossTextRectFillColor = Color.parseColor("#505050")

    /**
     * crosshairs text color
     */
    var crossTextColor = Color.parseColor("#EDEDED")

    /**
     * crosshairs text margin
     */
    var crossTextMargin = Utils.convertDpToPixel(3f)

    /**
     * crosshairs text size
     */
    var crossTextSize = Utils.convertDpToPixel(10f)

    /**
     * general data rect stroke line size
     */
    var generalDataRectStrokeLineSize = Utils.convertDpToPixel(1f)

    /**
     * general data rect stroke line color
     */
    var generalDataRectStrokeLineColor = Color.parseColor("#505050")

    /**
     * general data rect fill color
     */
    var generalDataRectFillColor = Color.parseColor("#99000000")

    /**
     * general data text size
     */
    var generalDataTextSize = Utils.convertDpToPixel(10f)

    /**
     * general data text color
     */
    var generalDataTextColor = Color.parseColor("#EDEDED")

    /**
     * general data increasing color
     */
    var generalDataIncreasingColor = Color.parseColor("#5DB300")

    /**
     * general data decreasing color
     */
    var generalDataDecreasingColor = Color.parseColor("#FF4A4A")

    /**
     * general data formatter
     */
    var generalDataFormatter: GeneralDataFormatter? = null

    /**
     * Indicator Display Rule
     */
    var indicatorDisplayRule =
        IndicatorDisplayRule.ALWAYS

    /**
     * indicator text size
     */
    var indicatorTextSize = Utils.convertDpToPixel(10f)

    /**
     * custom draw general data listener
     */
    var drawGeneralDataListener: DrawGeneralDataListener? = null

    /**
     * value formatter
     */
    var valueFormatter: ValueFormatter? = null

    /**
     * price decimal place
     */
    var priceDecimalPlace: Int = 8

    /**
     * volume decimal place
     */
    var volumeDecimalPlace: Int = 0
}
