package com.crypto.klinechart.chart

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import com.crypto.klinechart.KLineChartView
import com.crypto.klinechart.component.Component
import com.crypto.klinechart.component.Indicator
import com.crypto.klinechart.component.YAxis
import com.crypto.klinechart.internal.DataProvider
import com.crypto.klinechart.internal.ViewPortHandler
import com.crypto.klinechart.internal.utils.Utils
import com.crypto.klinechart.model.KLineModel
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class YAxisChart(
    private val axis: YAxis,
    private val dataProvider: DataProvider,
    private val viewPortHandler: ViewPortHandler
) : AxisChart() {

    /**
     * y-axis minimum value
     */
    var axisMinimum = 0f

    /**
     * y-axis maximum value
     */
    var axisMaximum = 0f

    /**
     * axis range of max value and min value
     */
    var axisRange = 0f

    /**
     * calculate y-axis min and max
     */
    var calcYAxisMinMax: KLineChartView.CustomIndicatorListener.CalcYAxisMinMax? = null

    private var labelStartX: Float = 0f

    private var labelWidth: Int = 0

    private var yAxisLabelOffset = Utils.convertDpToPixel(16f).toInt()

    var valueDecimalPrice: Int = 0

    override fun draw(canvas: Canvas) {
        throw UnsupportedOperationException("Unsupported operation")
    }

    override fun drawAxisLine(canvas: Canvas) {
        if (!this.axis.displayAxisLine) {
            return
        }
        this.paint.apply {
            strokeWidth = axis.axisLineSize
            color = axis.axisLineColor
        }
        val endY = this.offsetTop + this.height
        if (this.axis.yAxisPosition == YAxis.AxisPosition.LEFT) {
            canvas.drawLine(this.viewPortHandler.contentLeft(), this.offsetTop, this.viewPortHandler.contentLeft(), endY, this.paint)
        } else {
            canvas.drawLine(this.viewPortHandler.contentRight(), this.offsetTop, this.viewPortHandler.contentRight(), endY, this.paint)
        }
    }

    /**
     * draw y-axis labels
     * @param canvas Canvas
     */
    override fun drawAxisLabels(canvas: Canvas, indicatorType: String) {
        if (!this.axis.displayTickText) {
            return
        }

        if (this.axis.yAxisPosition == YAxis.AxisPosition.LEFT) {
            labelStartX = if (this.axis.yAxisTextPosition == YAxis.TextPosition.OUTSIDE) {
                if (this.axis.displayTickLine) {
                    viewPortHandler.contentLeft() - this.axis.tickLineSize - this.axis.textMarginSpace
                } else {
                    viewPortHandler.contentLeft() - this.axis.textMarginSpace
                }
            } else {
                if (this.axis.displayTickLine) {
                    this.viewPortHandler.contentLeft() + this.axis.tickLineSize + this.axis.textMarginSpace
                } else {
                    this.viewPortHandler.contentLeft() + this.axis.textMarginSpace
                }
            }
        } else {
            labelStartX = if (this.axis.yAxisTextPosition == YAxis.TextPosition.OUTSIDE) {
                if (this.axis.displayTickLine) {
                    this.viewPortHandler.contentRight() + this.axis.tickLineSize + this.axis.textMarginSpace
                } else {
                    this.viewPortHandler.contentRight() + this.axis.textMarginSpace
                }
            } else {
                if (this.axis.displayTickLine) {
                    this.viewPortHandler.contentRight() - this.axis.tickLineSize - this.axis.textMarginSpace
                } else {
                    this.viewPortHandler.contentRight() - this.axis.textMarginSpace
                }
            }
        }

        this.paint.apply {
            textSize = axis.tickTextSize
            color = axis.tickTextColor
            style = Paint.Style.FILL
        }

        for (i in this.labelValues.indices) {
            val labelY = getY(this.labelValues[i])

            var label = BigDecimal(this.labelValues[i]).setScale(valueDecimalPrice, BigDecimal.ROUND_DOWN).toString()
            label = this.axis.valueFormatter?.format(
                indicatorType, this.labelValues[i]
            ) ?: label
            val labelHeight = Utils.getTextHeight(this.paint, label)
            labelWidth = Utils.getTextWidth(this.paint, label)
            val halfLabelHeight = labelHeight / 2
            if (checkShowLabel(labelY, labelHeight)) {
                if ((this.axis.yAxisPosition == YAxis.AxisPosition.LEFT && this.axis.yAxisTextPosition == YAxis.TextPosition.OUTSIDE) ||
                    (this.axis.yAxisPosition == YAxis.AxisPosition.RIGHT && this.axis.yAxisTextPosition != YAxis.TextPosition.OUTSIDE)
                ) {
                    this.paint.textAlign = Paint.Align.RIGHT
                } else {
                    this.paint.textAlign = Paint.Align.LEFT
                }
                val startY = labelY + halfLabelHeight
                canvas.drawText(label, labelStartX, startY, this.paint)
            }
        }
    }

    /**
     * draw y-axis grid line
     * @param canvas Canvas
     */
    override fun drawGridLines(canvas: Canvas) {
        if (!this.axis.displayGridLine) {
            return
        }
        this.paint.apply {
            strokeWidth = axis.gridLineSize
            color = axis.gridLineColor
            style = Paint.Style.STROKE
            textSize = axis.tickTextSize
        }

        val labelHeight = Utils.getTextHeight(this.paint, "0")

        if (this.axis.gridLineStyle == Component.LineStyle.DASH) {
            this.paint.pathEffect = DashPathEffect(this.axis.separatorLineDashValues, 0f)
        }

        for (value in this.labelValues) {
            val y = getY(value)
            if (checkShowLabel(y, labelHeight)) {
                this.separatorLinePath.apply {
                    reset()
                    moveTo(viewPortHandler.contentLeft(), y)
                    lineTo(viewPortHandler.contentRight(), y)
                }
                canvas.drawPath(this.separatorLinePath, this.paint)
            }
        }
        this.paint.pathEffect = null
    }

    /**
     * draw y-axis label line
     * @param canvas Canvas
     */
    override fun drawTickLines(canvas: Canvas) {
        if (!this.axis.displayTickLine) {
            return
        }
        this.paint.apply {
            strokeWidth = 1f
            color = axis.axisLineColor
            style = Paint.Style.STROKE
            textSize = axis.tickTextSize
        }

        val labelHeight = Utils.getTextHeight(this.paint, "0")
        val startX: Float
        val endX: Float
        if (this.axis.yAxisPosition == YAxis.AxisPosition.LEFT) {
            startX = this.viewPortHandler.contentLeft()
            endX = if (this.axis.yAxisTextPosition == YAxis.TextPosition.OUTSIDE) {
                startX - this.axis.tickLineSize
            } else {
                startX + this.axis.tickLineSize
            }
        } else {
            startX = this.viewPortHandler.contentRight()
            endX = if (this.axis.yAxisTextPosition == YAxis.TextPosition.OUTSIDE) {
                startX + this.axis.tickLineSize
            } else {
                startX - this.axis.tickLineSize
            }
        }
        for (value in this.labelValues) {
            val y = getY(value)
            if (checkShowLabel(y, labelHeight)) {
                canvas.drawLine(startX, y, endX, y, this.paint)
            }
        }
    }

    /**
     * check whether show label and tick line and separator line
     * @param y Float
     * @param labelHeight Float
     * @return Boolean
     */
    private fun checkShowLabel(y: Float, labelHeight: Int) = y > this.offsetTop + labelHeight && y < this.offsetTop + this.height - labelHeight

    /**
     * get y-axis min and max data
     * @param indicatorType String
     * @param isMainYAxis Boolean
     * @param isTimeLine Boolean
     */
    fun getYAxisDataMinMax(indicatorType: String, isMainYAxis: Boolean, isTimeLine: Boolean) {
        val dataList = this.dataProvider.dataList
        val min = this.dataProvider.getVisibleDataMinPos()
        val max = min(min + this.dataProvider.getVisibleDataCount(), dataList.size)
        val minMaxArray = doubleArrayOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
        if (isTimeLine) {
            for (i in min until max) {
                val model = dataList[i]
                minMaxArray[0] = min(model.averagePrice, minMaxArray[0])
                minMaxArray[0] = min(model.closePrice, minMaxArray[0])
                minMaxArray[1] = max(model.averagePrice, minMaxArray[1])
                minMaxArray[1] = max(model.closePrice, minMaxArray[1])
            }
        } else {
            for (i in min until max) {
                val kLineModel = dataList[i]
                calcIndexMinMax(indicatorType, kLineModel, minMaxArray)
                if (isMainYAxis) {
                    minMaxArray[0] = min(kLineModel.lowPrice, minMaxArray[0])
                    minMaxArray[1] = max(kLineModel.highPrice, minMaxArray[1])
                }
            }
        }

        if (minMaxArray[0] != Double.POSITIVE_INFINITY && minMaxArray[1] != Double.NEGATIVE_INFINITY) {
            this.axisMinimum = minMaxArray[0].toFloat()
            this.axisMaximum = minMaxArray[1].toFloat()
        }
    }

    /**
     * get index min and max value
     * @param indicatorType String
     * @param kLineModel KLineModel
     * @param minMaxArray DoubleArray
     * @return DoubleArray
     */
    private fun calcIndexMinMax(indicatorType: String, kLineModel: KLineModel, minMaxArray: DoubleArray): DoubleArray {
        when (indicatorType) {
            Indicator.Type.NO -> {}
            Indicator.Type.MA -> {
                minMaxArray[0] = min(kLineModel.ma?.ma5 ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.ma?.ma10 ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.ma?.ma20 ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.ma?.ma60 ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[1] = max(kLineModel.ma?.ma5 ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.ma?.ma10 ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.ma?.ma20 ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.ma?.ma60 ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
            }
            Indicator.Type.MACD -> {
                minMaxArray[0] = min(kLineModel.macd?.dea ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.macd?.diff ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.macd?.macd ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[1] = max(kLineModel.macd?.dea ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.macd?.diff ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.macd?.macd ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
            }
            Indicator.Type.VOL -> {
                minMaxArray[0] = min(kLineModel.volume, 0.0)
                minMaxArray[1] = max(kLineModel.volume, minMaxArray[1])
            }
            Indicator.Type.BOLL -> {
                minMaxArray[0] = min(kLineModel.boll?.up ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.boll?.mid ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.boll?.dn ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.lowPrice, minMaxArray[0])
                minMaxArray[1] = max(kLineModel.boll?.up ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.boll?.mid ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.boll?.dn ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.highPrice, minMaxArray[1])
            }
            Indicator.Type.KDJ -> {
                minMaxArray[0] = min(kLineModel.kdj?.k ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.kdj?.d ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.kdj?.j ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[1] = max(kLineModel.kdj?.k ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.kdj?.d ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.kdj?.j ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
            }
            Indicator.Type.RSI -> {
                minMaxArray[0] = min(kLineModel.rsi?.rsi1 ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.rsi?.rsi2 ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[0] = min(kLineModel.rsi?.rsi3 ?: Double.POSITIVE_INFINITY, minMaxArray[0])
                minMaxArray[1] = max(kLineModel.rsi?.rsi1 ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.rsi?.rsi2 ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
                minMaxArray[1] = max(kLineModel.rsi?.rsi3 ?: Double.NEGATIVE_INFINITY, minMaxArray[1])
            }
            else -> {
                calcYAxisMinMax?.calcYAxisMinMax(indicatorType, kLineModel, minMaxArray)
            }
        }
        return minMaxArray
    }

    override fun calcRange(min: Float, max: Float): Float = abs(max - min)

    override fun computeAxis(labelCount: Int) {
        var min = this.axisMinimum
        var max = this.axisMaximum
        var range = abs(max - min)

        if (range == 0f) {
            max += 1f
            min -= 1f
            range = abs(max - min)
        }

        this.axisMinimum = min - (range / 100f) * 10
        this.axisMaximum = max + (range / 100f) * 20

        this.axisRange = abs(this.axisMaximum - this.axisMinimum)

        computeAxisValues(this.axisMinimum, this.axisMaximum, labelCount)
    }

    /**
     * get y-axis coordinate
     * @param value Float
     * @return Float
     */
    fun getY(value: Double): Float {
        return (this.offsetTop + (1f - (value - this.axisMinimum) / this.axisRange) * this.height).toFloat()
    }

    /**
     * get y-axis label start x
     * @return Float
     */
    fun getLabelStartX(): Float {
        return labelStartX
    }

    /**
     * get y-axis label width
     * @return Float
     */
    fun getLabelWidth(): Int {
        return labelWidth
    }

    /**
     * get margin between y-axis label and last candle
     * @return Float
     */
    fun getYAxisLabelOffset(): Int {
        return yAxisLabelOffset
    }

    /**
     * get value of y-axis coordinate
     * @param y Float
     * @return Float
     */
    fun getValue(y: Float): Float {
        return (1f - (y - this.offsetTop) / this.height) * (this.axisRange) + this.axisMinimum
    }
}
