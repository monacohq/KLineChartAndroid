package com.crypto.klinechart.chart

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import com.crypto.klinechart.component.Component
import com.crypto.klinechart.component.XAxis
import com.crypto.klinechart.internal.DataProvider
import com.crypto.klinechart.internal.ViewPortHandler
import com.crypto.klinechart.internal.utils.Utils
import com.crypto.klinechart.internal.utils.formatDate
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

internal class XAxisChart(
    private val axis: XAxis,
    private val dataProvider: DataProvider,
    private val viewPortHandler: ViewPortHandler
) : AxisChart() {

    private var valuePoints = FloatArray(0)

    init {
        this.paint.textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas) {
        computeAxis(5)
        drawAxisLine(canvas)
        drawAxisLabels(canvas)
        drawGridLines(canvas)
        drawTickLines(canvas)
    }

    override fun drawAxisLine(canvas: Canvas) {
        if (!this.axis.displayAxisLine) {
            return
        }
        this.paint.apply {
            strokeWidth = axis.axisLineSize
            color = axis.axisLineColor
        }
        canvas.drawLine(this.viewPortHandler.contentLeft(), this.offsetTop, this.viewPortHandler.contentRight(), this.offsetTop, this.paint)
    }

    /**
     * draw x-axis labels
     * @param canvas Canvas
     */
    override fun drawAxisLabels(canvas: Canvas, indicatorType: String) {
        if (!this.axis.displayTickText) {
            return
        }
        this.paint.apply {
            textSize = axis.tickTextSize
            color = axis.tickTextColor
            style = Paint.Style.FILL
        }
        val labelHeight = Utils.getTextHeight(this.paint, "T")
        var startY = this.viewPortHandler.contentBottom() + this.axis.textMarginSpace + labelHeight
        if (this.axis.displayTickLine) {
            startY += axis.tickLineSize
        }

        for (i in this.valuePoints.indices) {
            val x = this.valuePoints[i]
            val kLineModel = this.dataProvider.dataList[this.labelValues[i].toInt()]
            val timestamp = kLineModel.timestamp
            var label = timestamp.formatDate(pattern = "MM-dd HH:mm")
            label = this.axis.valueFormatter?.format(timestamp) ?: label

            canvas.drawText(label, x, startY, this.paint)
        }
    }

    /**
     * draw grid lines
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
        }
        if (this.axis.gridLineStyle == Component.LineStyle.DASH) {
            this.paint.pathEffect = DashPathEffect(this.axis.separatorLineDashValues, 0f)
        }

        for (i in this.valuePoints.indices) {
            val x = this.valuePoints[i]
            this.separatorLinePath.apply {
                reset()
                moveTo(x, viewPortHandler.contentTop())
                lineTo(x, viewPortHandler.contentBottom())
            }
            canvas.drawPath(this.separatorLinePath, this.paint)
        }
        this.paint.pathEffect = null
    }

    /**
     * draw tick lines
     * @param canvas Canvas
     */
    override fun drawTickLines(canvas: Canvas) {
        if (!this.axis.displayTickLine) {
            return
        }
        this.paint.apply {
            strokeWidth = 1f
            color = axis.axisLineColor
        }
        val startY = this.viewPortHandler.contentBottom()
        val endY = startY + this.axis.tickLineSize

        for (i in this.valuePoints.indices) {
            val x = this.valuePoints[i]
            canvas.drawLine(x, startY, x, endY, this.paint)
        }
    }

    /**
     * get point values to pixel
     * @return FloatArray
     */
    private fun pointValuesToPixel() {
        val offsetLeft = this.viewPortHandler.contentLeft()
        this.valuePoints = FloatArray(valueCount)
        for (i in this.valuePoints.indices) {
            val pos = this.labelValues[i]
            val chartDataSpace = this.dataProvider.getChartDataSpace()
            this.valuePoints[i] = offsetLeft + ((pos - this.dataProvider.getVisibleDataMinPos()) * chartDataSpace + chartDataSpace * (1 - DataProvider.DATA_SPACE_RATE) / 2).toFloat()
        }
    }

    override fun computeAxis(labelCount: Int) {
        val visibleDataMinPos = this.dataProvider.getVisibleDataMinPos()
        val max = min(visibleDataMinPos + this.dataProvider.getVisibleDataCount() - 1, this.dataProvider.dataList.size - 1)
        computeAxisValues(visibleDataMinPos.toFloat(), max.toFloat(), labelCount)
        pointValuesToPixel()
    }

    override fun fixComputeAxisValues() {
        val dataSize = this.dataProvider.dataList.size
        if (dataSize > 0) {
            this.paint.textSize = this.axis.tickTextSize
            val defaultLabelWidth = Utils.getTextWidth(this.paint, "0000-00-00 00:00:00")
            val chartDataSpace = this.dataProvider.getChartDataSpace()
            var startPos = ceil(defaultLabelWidth / 2.0 / chartDataSpace) - 1
            if (startPos > dataSize - 1) {
                startPos = dataSize - 1.0
            }
            val barCount = ceil(defaultLabelWidth / (chartDataSpace * (1 + DataProvider.DATA_SPACE_RATE))) + 1
            if (dataSize > barCount) {
                this.valueCount = floor((dataSize - startPos) / barCount).toInt() + 1
            } else {
                this.valueCount = 1
            }
            this.labelValues = DoubleArray(this.valueCount)
            this.labelValues[0] = startPos
            for (i in 1 until this.valueCount) {
                this.labelValues[i] = startPos + i * (barCount - 1)
            }
        } else {
            this.valueCount = 0
            this.labelValues = doubleArrayOf()
        }
    }

    override fun calcRange(min: Float, max: Float): Float = if (max < 0f) 0f else abs(max - min) + 1f

    override fun isFillChart(): Boolean = this.dataProvider.dataList.size > this.dataProvider.getVisibleDataCount()
}
