package com.crypto.klinechart.chart

import android.graphics.Canvas
import android.graphics.Path
import com.crypto.klinechart.component.Indicator
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.nextUp
import kotlin.math.pow
import kotlin.math.roundToLong

internal abstract class AxisChart : Chart() {

    /**
     * label values
     */
    var labelValues = DoubleArray(0)

    /**
     * value count
     */
    var valueCount = 0

    /**
     * axis value decimals
     */
    var axisValueDecimals = 0

    /**
     * separator line Path
     */
    val separatorLinePath = Path()

    /**
     * draw Axis Line
     * @param canvas Canvas
     */
    abstract fun drawAxisLine(canvas: Canvas)

    /**
     * draw Axis Labels
     * @param canvas Canvas
     * @param indicatorType String
     */
    abstract fun drawAxisLabels(canvas: Canvas, indicatorType: String = Indicator.Type.NO)

    /**
     * draw grid Lines
     * @param canvas Canvas
     */
    abstract fun drawGridLines(canvas: Canvas)

    /**
     * draw Tick Lines
     * @param canvas Canvas
     */
    abstract fun drawTickLines(canvas: Canvas)

    /**
     * calculate Range
     * @param min Float
     * @param max Float
     * @return Float
     */
    abstract fun calcRange(min: Float, max: Float): Float

    /**
     * get axis tick distance
     */
    abstract fun computeAxis(labelCount: Int)

    /**
     * number of data is larger than chart paint area
     * @return Boolean
     */
    open fun isFillChart(): Boolean = true

    /**
     * get axis tick label
     * @param min Float
     * @param max Float
     * @param labelCount Int
     */
    open fun computeAxisValues(min: Float, max: Float, labelCount: Int) {
        val range = calcRange(min, max)
        if (labelCount == 0 || range <= 0 || range.isInfinite()) {
            this.labelValues = doubleArrayOf()
            this.valueCount = 0
            return
        }

        if (isFillChart()) {
            val rawInterval = range / labelCount
            var interval = roundToNextSignificant(rawInterval.toDouble())
            val intervalMagnitude = roundToNextSignificant(10.0.pow(log10(interval)))
            val intervalSigDigit = (interval / intervalMagnitude).toInt()
            if (intervalSigDigit > 5) {
                interval = floor(10.0 * intervalMagnitude)
            }

            var n = 0

            val first = if (interval == 0.0) 0.0 else (ceil(min / interval) * interval)

            val last = if (interval == 0.0) 0.0 else (floor(max / interval) * interval).nextUp()
            var f: Double = first

            if (interval != 0.0) {
                while (f <= last) {
                    ++n
                    f += interval
                }
            }
            this.valueCount = n
            this.labelValues = DoubleArray(n)

            var i = 0
            f = first
            while (i < n) {
                if (f == 0.0) {
                    f = 0.0
                }
                this.labelValues[i] = f
                f += interval
                ++i
            }
            this.axisValueDecimals = if (interval < 1) {
                ceil(-log10(interval)).toInt()
            } else {
                0
            }
        } else {
            fixComputeAxisValues()
        }
    }

    open fun fixComputeAxisValues() {}

    private fun roundToNextSignificant(v: Double): Double {
        if (v == Double.POSITIVE_INFINITY ||
            v == Double.NEGATIVE_INFINITY ||
            v.isNaN() ||
            v == 0.0
        ) {
            return 0.0
        }
        var n = v
        if (n < 0.0) {
            n = -n
        }
        val d = ceil(log10(n))
        val pw = 1 - d.toInt()
        val magnitude = 10.0.pow(pw)
        val shifted = (v * magnitude).roundToLong()
        return shifted / magnitude
    }
}
