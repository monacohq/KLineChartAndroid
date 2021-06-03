package com.crypto.klinechart.chart

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import com.crypto.klinechart.KLineChartView
import com.crypto.klinechart.R
import com.crypto.klinechart.component.Candle
import com.crypto.klinechart.component.Component
import com.crypto.klinechart.component.Indicator
import com.crypto.klinechart.component.Tooltip
import com.crypto.klinechart.component.YAxis
import com.crypto.klinechart.internal.DataProvider
import com.crypto.klinechart.internal.ViewPortHandler
import com.crypto.klinechart.internal.utils.Utils
import com.crypto.klinechart.internal.utils.formatDate
import com.crypto.klinechart.internal.utils.formatDecimal
import com.crypto.klinechart.model.KLineModel
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

internal class TooltipChart(
    private val candleChart: CandleChart,
    private val volChart: IndicatorChart,
    private val indicatorChart: IndicatorChart,
    private val tooltip: Tooltip,
    private val candle: Candle,
    private val indicator: Indicator,
    private val yAxis: YAxis,
    private val dataProvider: DataProvider,
    private val viewPortHandler: ViewPortHandler
) : Chart() {
    /**
     * custom tooltip labels
     */
    var tooltipLabels: KLineChartView.CustomIndicatorListener.TooltipLabels? = null

    /**
     * custom tooltip values
     */
    var tooltipValues: KLineChartView.CustomIndicatorListener.TooltipValues? = null

    /**
     * default general data labels
     */
    private val defaultGeneralDataLabels: MutableList<String> = mutableListOf(
        "${Utils.getResourceString(R.string.time)}: ",
        "${Utils.getResourceString(R.string.open_price)}: ",
        "${Utils.getResourceString(R.string.close_price)}: ",
        "${Utils.getResourceString(R.string.highest_price)}: ",
        "${Utils.getResourceString(R.string.lowest_price)}: ",
        "${Utils.getResourceString(R.string.change)}: ",
        "${Utils.getResourceString(R.string.chg)}: ",
        "${Utils.getResourceString(R.string.volume)}: "
    )

    /**
     * crosshairs path
     */
    private val crosshairsPath = Path()
    /**
     * crosshairs bg rect path
     */
    private val crosshairsYAxisLabelStrokePathPoints = arrayListOf(PointF(), PointF(), PointF(), PointF())

    /**
     * last price line path
     */
    private val lastPriceLinePath = Path()

    /**
     * last price bg rect path
     */
    private val yAxisLastPriceLabelStrokePathPoints = arrayListOf(PointF(), PointF(), PointF(), PointF())

    private val dp20ToPx = Utils.convertDpToPixel(20f)
    private val dp50ToPx = Utils.convertDpToPixel(50f)
    private val dp5ToPx = Utils.convertDpToPixel(5f)
    private val dp2ToPx = Utils.convertDpToPixel(2f)
    private val dp3ToPx = Utils.convertDpToPixel(3f)
    private val dp8ToPx = Utils.convertDpToPixel(8f)

    override fun draw(canvas: Canvas) {
        if (this.dataProvider.currentTipDataPos < this.dataProvider.dataList.size) {
            val kLineModel: KLineModel = this.dataProvider.dataList[this.dataProvider.currentTipDataPos]
            val previousDataIndex = this.dataProvider.currentTipDataPos - 1
            val previouskLineModel: KLineModel? = if (previousDataIndex > 0) this.dataProvider.dataList[previousDataIndex] else null
            val displayCross = this.dataProvider.crossPoint.y >= 0

            if (this.tooltip.indicatorDisplayRule == Tooltip.IndicatorDisplayRule.ALWAYS ||
                (this.tooltip.indicatorDisplayRule == Tooltip.IndicatorDisplayRule.FOLLOW_CROSS && displayCross)
            ) {
                this.paint.apply {
                    textSize = tooltip.indicatorTextSize
                    style = Paint.Style.FILL
                }
                val textHeight = Utils.getTextHeight(this.paint, "0")
                val startX = this.viewPortHandler.contentLeft() + this.dp3ToPx
                if (this.candle.chartStyle != Candle.ChartStyle.TIME_LINE) {
                    // draw candle chart tooltip
                    drawIndicatorTooltip(
                        canvas, startX,
                        this.candleChart.offsetTop + this.dp2ToPx + textHeight,
                        kLineModel,
                        this.candleChart.indicatorType
                    )
                }
                // draw volume chart tooltip
                drawIndicatorTooltip(
                    canvas, startX,
                    this.volChart.offsetTop + this.dp2ToPx + textHeight,
                    kLineModel, this.volChart.indicatorType
                )
                // draw sub chart indicator tooltip
                drawIndicatorTooltip(
                    canvas, startX,
                    this.indicatorChart.offsetTop + this.dp2ToPx + textHeight,
                    kLineModel, this.indicatorChart.indicatorType
                )
            }

            drawLastPriceMark(canvas)

            if (displayCross) {
                val chartDataSpace = this.dataProvider.getChartDataSpace()
                this.dataProvider.crossPoint.x = this.viewPortHandler.contentLeft() +
                    chartDataSpace * (this.dataProvider.currentTipDataPos - this.dataProvider.getVisibleDataMinPos()) +
                    chartDataSpace * (1f - DataProvider.DATA_SPACE_RATE) / 2f

                this.paint.textSize = this.tooltip.crossTextSize

                drawCrossHorizontalLine(canvas)
                drawCrossVerticalLine(canvas, kLineModel)
                drawGeneralDataTooltip(canvas, kLineModel, previouskLineModel)
            }
        }
    }

    /**
     * get crosshair y-axis label
     */
    private fun getCrossYAxisLabel(): String? {
        val crossPointY = this.dataProvider.crossPoint.y
        if (crossPointY > this.viewPortHandler.contentTop() &&
            crossPointY < this.viewPortHandler.contentBottom()
        ) {

            val candleChartContentTop = this.candleChart.offsetTop
            val volIndicatorChartContentTop = this.volChart.offsetTop

            val yAxisChart: YAxisChart
            val indicatorType: String

            when {
                crossPointY > candleChartContentTop && crossPointY < (this.candleChart.height + candleChartContentTop) -> {
                    yAxisChart = this.candleChart.yAxisChart
                    indicatorType = this.candleChart.indicatorType
                }
                crossPointY > volIndicatorChartContentTop && crossPointY < this.volChart.height + volIndicatorChartContentTop -> {
                    yAxisChart = this.volChart.yAxisChart
                    indicatorType = Indicator.Type.VOL
                }
                else -> {
                    yAxisChart = this.indicatorChart.yAxisChart
                    indicatorType = this.indicatorChart.indicatorType
                }
            }

            val yData = yAxisChart.getValue(crossPointY)
            val text = if (indicatorType == Indicator.Type.VOL) "${yData.toInt()}" else yData.formatDecimal(yAxisChart.valueDecimalPrice)
            return this.tooltip.valueFormatter?.format(
                Tooltip.ValueFormatter.Y_AXIS,
                indicatorType, "$yData"
            ) ?: text
        }
        return null
    }

    /**
     * draw crosshair horizontal line
     * @param canvas Canvas
     */
    private fun drawCrossHorizontalLine(canvas: Canvas) {
        val yAxisDataLabel = getCrossYAxisLabel() ?: return

        val crossPointY = this.dataProvider.crossPoint.y
        val isDrawYAxisTextOutside = this.yAxis.yAxisTextPosition == YAxis.TextPosition.OUTSIDE
        val isYAxisPositionLeft = this.yAxis.yAxisPosition == YAxis.AxisPosition.LEFT

        val yAxisDataLabelSize = Utils.getTextSize(this.paint, yAxisDataLabel)
        val yAxisDataLabelWidth = yAxisDataLabelSize.width()
        val halfLabelHeight = yAxisDataLabelSize.height() / 2f

        val lineStartX = this.viewPortHandler.contentLeft()
        val lineEndX = when {
            !isDrawYAxisTextOutside -> this.viewPortHandler.contentRight() - this.tooltip.crossTextRectStrokeLineSize * 2 - this.tooltip.crossTextMargin * 3 - yAxisDataLabelWidth
            else -> this.viewPortHandler.contentRight()
        }

        val labelStartX = when {
            isDrawYAxisTextOutside && !isYAxisPositionLeft -> lineEndX + this.tooltip.crossTextRectStrokeLineSize + this.tooltip.crossTextMargin
            !isDrawYAxisTextOutside && !isYAxisPositionLeft -> lineEndX + this.tooltip.crossTextRectStrokeLineSize + this.tooltip.crossTextMargin * 2
            !isDrawYAxisTextOutside && isYAxisPositionLeft -> lineStartX + this.tooltip.crossTextRectStrokeLineSize + this.tooltip.crossTextMargin * 2
            else -> lineStartX - this.tooltip.crossTextRectStrokeLineSize - this.tooltip.crossTextMargin * 2 - yAxisDataLabelWidth
        }
        val labelStartY = crossPointY + halfLabelHeight

        if (this.yAxis.yAxisPosition == YAxis.AxisPosition.LEFT) {
            // left
            this.crosshairsYAxisLabelStrokePathPoints[0].set(
                lineStartX - this.tooltip.crossTextMargin,
                crossPointY - halfLabelHeight - this.tooltip.crossTextMargin
            )
            this.crosshairsYAxisLabelStrokePathPoints[1].set(
                lineStartX - this.tooltip.crossTextMargin * 3 - yAxisDataLabelSize.width(),
                this.crosshairsYAxisLabelStrokePathPoints[0].y
            )
            this.crosshairsYAxisLabelStrokePathPoints[2].set(
                this.crosshairsYAxisLabelStrokePathPoints[1].x,
                crossPointY + halfLabelHeight + this.tooltip.crossTextMargin
            )
            this.crosshairsYAxisLabelStrokePathPoints[3].set(
                this.crosshairsYAxisLabelStrokePathPoints[0].x,
                this.crosshairsYAxisLabelStrokePathPoints[2].y
            )
        } else {
            // right
            this.crosshairsYAxisLabelStrokePathPoints[0].set(
                lineEndX + this.tooltip.crossTextMargin,
                crossPointY - halfLabelHeight - this.tooltip.crossTextMargin
            )
            this.crosshairsYAxisLabelStrokePathPoints[1].set(
                lineEndX + this.tooltip.crossTextMargin * 3 + yAxisDataLabelSize.width(),
                this.crosshairsYAxisLabelStrokePathPoints[0].y
            )
            this.crosshairsYAxisLabelStrokePathPoints[2].set(
                this.crosshairsYAxisLabelStrokePathPoints[1].x,
                crossPointY + halfLabelHeight + this.tooltip.crossTextMargin
            )
            this.crosshairsYAxisLabelStrokePathPoints[3].set(
                this.crosshairsYAxisLabelStrokePathPoints[0].x,
                this.crosshairsYAxisLabelStrokePathPoints[2].y
            )
        }

        // draw crosshairs horizontal line
        this.paint.apply {
            strokeWidth = tooltip.crosshairsSize
            style = Paint.Style.STROKE
            color = tooltip.crosshairsColor
        }
        if (this.tooltip.crosshairsStyle == Component.LineStyle.DASH) {
            this.paint.pathEffect = DashPathEffect(this.tooltip.crosshairsDashValues, 0f)
        }
        this.crosshairsPath.apply {
            reset()
            moveTo(lineStartX, crossPointY)
            lineTo(lineEndX, crossPointY)
        }
        canvas.drawPath(this.crosshairsPath, this.paint)
        this.paint.pathEffect = null

        // draw y-axis crosshairs text rect

        this.paint.apply {
            style = Paint.Style.FILL
            color = tooltip.crossTextRectFillColor
        }
        this.crosshairsPath.apply {
            reset()
            moveTo(crosshairsYAxisLabelStrokePathPoints[0].x, crosshairsYAxisLabelStrokePathPoints[0].y)
        }
        for (i in 1 until this.crosshairsYAxisLabelStrokePathPoints.size) {
            this.crosshairsPath.lineTo(this.crosshairsYAxisLabelStrokePathPoints[i].x, this.crosshairsYAxisLabelStrokePathPoints[i].y)
        }
        this.crosshairsPath.close()
        canvas.drawPath(this.crosshairsPath, this.paint)

        this.paint.apply {
            strokeWidth = tooltip.crossTextRectStrokeLineSize
            style = Paint.Style.STROKE
            color = tooltip.crossTextRectStrokeLineColor
        }
        this.crosshairsPath.apply {
            reset()
            moveTo(crosshairsYAxisLabelStrokePathPoints[0].x, crosshairsYAxisLabelStrokePathPoints[0].y)
        }
        for (i in 1 until this.crosshairsYAxisLabelStrokePathPoints.size) {
            this.crosshairsPath.lineTo(this.crosshairsYAxisLabelStrokePathPoints[i].x, this.crosshairsYAxisLabelStrokePathPoints[i].y)
        }
        this.crosshairsPath.close()
        canvas.drawPath(this.crosshairsPath, this.paint)

        this.paint.apply {
            color = tooltip.crossTextColor
            style = Paint.Style.FILL
        }
        canvas.drawText(yAxisDataLabel, labelStartX, labelStartY, this.paint)
    }

    /**
     * draw crosshair vertical line
     * @param canvas Canvas
     * @param kLineModel KLineModel
     */
    private fun drawCrossVerticalLine(canvas: Canvas, kLineModel: KLineModel) {
        val crossPointX = this.dataProvider.crossPoint.x
        this.paint.apply {
            strokeWidth = tooltip.crosshairsSize
            style = Paint.Style.STROKE
            color = tooltip.crosshairsColor
        }
        if (this.tooltip.crosshairsStyle == Component.LineStyle.DASH) {
            this.paint.pathEffect = DashPathEffect(this.tooltip.crosshairsDashValues, 0f)
        }
        // draw crosshair vertical line
        this.crosshairsPath.apply {
            reset()
            moveTo(crossPointX, viewPortHandler.contentTop())
            lineTo(crossPointX, viewPortHandler.contentBottom())
        }
        canvas.drawPath(this.crosshairsPath, this.paint)
        this.paint.pathEffect = null

        val timestamp = kLineModel.timestamp
        var label = kLineModel.timestamp.formatDate("yyyy-MM-dd HH:mm")
        label = this.tooltip.valueFormatter?.format(
            Tooltip.ValueFormatter.X_AXIS,
            null,
            "$timestamp"
        ) ?: label
        val labelSize = Utils.getTextSize(this.paint, label)
        var xAxisLabelX = crossPointX - labelSize.width() / 2

        // ensure x-axis label can show
        if (xAxisLabelX < this.viewPortHandler.contentLeft() + this.tooltip.crossTextMargin + this.tooltip.crossTextRectStrokeLineSize) {
            xAxisLabelX = this.viewPortHandler.contentLeft()
        } else if (xAxisLabelX > this.viewPortHandler.contentRight() - labelSize.width() - this.tooltip.crossTextRectStrokeLineSize) {
            xAxisLabelX = this.viewPortHandler.contentRight() - labelSize.width() - this.tooltip.crossTextRectStrokeLineSize
        }

        val rectLeft = xAxisLabelX - this.tooltip.crossTextRectStrokeLineSize - this.tooltip.crossTextMargin
        val rectTop = this.viewPortHandler.contentBottom()
        val rectRight = xAxisLabelX + labelSize.width() + this.tooltip.crossTextMargin + this.tooltip.crossTextRectStrokeLineSize
        val rectBottom = this.viewPortHandler.contentBottom() + labelSize.height() + this.tooltip.crossTextRectStrokeLineSize + this.tooltip.crossTextMargin * 2
        this.paint.apply {
            style = Paint.Style.FILL
            color = tooltip.crossTextRectFillColor
        }
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, this.paint)

        this.paint.apply {
            strokeWidth = tooltip.crossTextRectStrokeLineSize
            style = Paint.Style.STROKE
            color = tooltip.crossTextRectStrokeLineColor
        }
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, this.paint)

        // draw x-axis crosshair label
        this.paint.apply {
            color = tooltip.crossTextColor
            style = Paint.Style.FILL
        }
        canvas.drawText(
            label,
            xAxisLabelX,
            this.viewPortHandler.contentBottom() + labelSize.height() + this.tooltip.crossTextRectStrokeLineSize + this.tooltip.crossTextMargin,
            this.paint
        )
    }

    /**
     * draw General Data Tooltip
     * @param canvas Canvas
     * @param kLineModel KLineModel
     */
    private fun drawGeneralDataTooltip(canvas: Canvas, kLineModel: KLineModel, previouskLineModel: KLineModel?) {
        val drawGeneralDataListener = this.tooltip.drawGeneralDataListener
        if (drawGeneralDataListener != null) {
            drawGeneralDataListener.draw(
                canvas, paint,
                this.dataProvider.crossPoint,
                this.tooltip,
                this.viewPortHandler.contentRect,
                kLineModel
            )
        } else {
            this.paint.textSize = this.tooltip.generalDataTextSize
            var labels = defaultGeneralDataLabels
            val values: MutableList<String>
            val generalDataFormatter = this.tooltip.generalDataFormatter
            if (generalDataFormatter != null) {
                labels = generalDataFormatter.generatedLabels()
                values = generalDataFormatter.generatedValues(kLineModel)
            } else {
                val changeValue: Double? = if (previouskLineModel?.closePrice != null) kLineModel.closePrice - previouskLineModel.closePrice else null
                val changeStr = changeValue?.formatDecimal(tooltip.priceDecimalPlace) ?: "N/A"
                val changePercent = if (changeValue != null && previouskLineModel?.closePrice != null) "${(changeValue / previouskLineModel.closePrice * 100).formatDecimal(decimal = 2)}%" else "N/A"
                values = mutableListOf(
                    kLineModel.timestamp.formatDate(),
                    kLineModel.openPrice.formatDecimal(tooltip.priceDecimalPlace),
                    kLineModel.closePrice.formatDecimal(tooltip.priceDecimalPlace),
                    kLineModel.highPrice.formatDecimal(tooltip.priceDecimalPlace),
                    kLineModel.lowPrice.formatDecimal(tooltip.priceDecimalPlace),
                    changeStr,
                    changePercent,
                    kLineModel.volume.formatDecimal(tooltip.volumeDecimalPlace, mode = RoundingMode.DOWN)
                )
            }

            var maxLabelWidth = Int.MIN_VALUE
            val labelHeight = Utils.getTextHeight(this.paint, "0")
            val labelSize = labels.size
            val valueSize = values.size
            for (i in 0 until labelSize) {
                val value = if (i < valueSize) values[i] else "--"
                val width = Utils.getTextWidth(this.paint, "${labels[i]}$value")
                maxLabelWidth = max(maxLabelWidth, width)
            }

            val rectStartX: Float
            val rectEndX: Float
            val rectStartY = this.viewPortHandler.contentTop() + this.dp20ToPx
            val rectEndY = rectStartY + this.tooltip.generalDataRectStrokeLineSize * 2 + labelSize * labelHeight + (labelSize - 1) * this.dp8ToPx + this.dp5ToPx * 2
            val centerPoint = this.viewPortHandler.getContentCenter()
            if (this.dataProvider.crossPoint.x < centerPoint.x) {
                rectStartX = this.viewPortHandler.contentRight() - this.dp50ToPx - this.tooltip.generalDataRectStrokeLineSize * 2 - this.dp3ToPx * 2 - maxLabelWidth
                rectEndX = this.viewPortHandler.contentRight() - this.dp50ToPx
            } else {
                rectStartX = this.viewPortHandler.contentLeft() + this.dp50ToPx
                rectEndX = rectStartX + this.tooltip.generalDataRectStrokeLineSize * 2 + this.dp3ToPx * 2 + maxLabelWidth
            }

            this.paint.apply {
                style = Paint.Style.FILL
                color = tooltip.generalDataRectFillColor
            }
            val rect = RectF(rectStartX, rectStartY, rectEndX, rectEndY)
            canvas.drawRoundRect(rect, this.dp3ToPx, this.dp3ToPx, this.paint)

            this.paint.apply {
                style = Paint.Style.STROKE
                color = tooltip.generalDataRectStrokeLineColor
            }
            canvas.drawRoundRect(rect, this.dp3ToPx, this.dp3ToPx, this.paint)

            this.paint.apply {
                style = Paint.Style.FILL
                color = tooltip.generalDataTextColor
            }
            val labelStartX = rectStartX + this.tooltip.generalDataRectStrokeLineSize + this.dp3ToPx
            var textStartY = rectStartY + this.tooltip.generalDataRectStrokeLineSize + this.dp5ToPx + labelHeight
            for (i in 0 until labelSize) {
                this.paint.apply {
                    color = tooltip.generalDataTextColor
                    textAlign = Paint.Align.LEFT
                }
                canvas.drawText(
                    labels[i],
                    labelStartX, textStartY, this.paint
                )
                val valueStartX = rectEndX - this.tooltip.crossTextRectStrokeLineSize - this.dp3ToPx
                if (generalDataFormatter != null) {
                    generalDataFormatter.generatedStyle(this.paint, kLineModel, this.tooltip, i)
                } else {
                    if (i == 5 || i == 6) {
                        when {
                            kLineModel.closePrice > kLineModel.openPrice -> this.paint.color = this.tooltip.generalDataIncreasingColor
                            kLineModel.closePrice < kLineModel.openPrice -> this.paint.color = this.tooltip.generalDataDecreasingColor
                            else -> this.paint.color = this.tooltip.generalDataTextColor
                        }
                    } else {
                        this.paint.color = this.tooltip.generalDataTextColor
                    }
                }
                this.paint.textAlign = Paint.Align.RIGHT
                val value = if (i < valueSize) values[i] else "--"
                canvas.drawText(value, valueStartX, textStartY, this.paint)
                textStartY += labelHeight + this.dp8ToPx
            }
        }
        this.paint.textAlign = Paint.Align.LEFT
    }

    /**
     * draw indicator tooltip
     * @param canvas Canvas
     * @param startX Float
     * @param startY Float
     * @param kLineModel KLineModel
     * @param indicatorType String
     */
    private fun drawIndicatorTooltip(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        kLineModel: KLineModel,
        indicatorType: String
    ) {
        when (indicatorType) {
            Indicator.Type.NO -> {}
            Indicator.Type.MA -> {
                val maData = kLineModel.ma
                drawIndicatorTooltipLabels(
                    canvas, startX, startY,
                    mutableListOf(maData?.ma5, maData?.ma10, maData?.ma20, maData?.ma60),
                    mutableListOf("MA5", "MA10", "MA20", "MA60"),
                    indicatorType
                )
            }
            Indicator.Type.MACD -> {
                val macdData = kLineModel.macd
                drawIndicatorTooltipLabels(
                    canvas, startX, startY,
                    mutableListOf(macdData?.diff, macdData?.dea, macdData?.macd),
                    mutableListOf("DIFF", "DEA", "MACD"),
                    indicatorType
                )
            }
            Indicator.Type.VOL -> {
                val volData = kLineModel.volume
                drawIndicatorTooltipLabels(
                    canvas, startX, startY,
                    mutableListOf(volData),
                    mutableListOf("VOLUME"),
                    indicatorType
                )
            }
            Indicator.Type.BOLL -> {
                val bollData = kLineModel.boll
                drawIndicatorTooltipLabels(
                    canvas, startX, startY,
                    mutableListOf(bollData?.up, bollData?.mid, bollData?.dn),
                    mutableListOf("UP", "MID", "DN"),
                    indicatorType
                )
            }
            Indicator.Type.KDJ -> {
                val kdjData = kLineModel.kdj
                drawIndicatorTooltipLabels(
                    canvas, startX, startY,
                    mutableListOf(kdjData?.k, kdjData?.d, kdjData?.j),
                    mutableListOf("K", "D", "J"),
                    indicatorType
                )
            }
            Indicator.Type.RSI -> {
                val rsiData = kLineModel.rsi
                drawIndicatorTooltipLabels(
                    canvas, startX, startY,
                    mutableListOf(rsiData?.rsi1, rsiData?.rsi2, rsiData?.rsi3),
                    mutableListOf("RSI6", "RSI12", "RSI24"),
                    indicatorType
                )
            }

            else -> {
                drawIndicatorTooltipLabels(
                    canvas, startX, startY,
                    this.tooltipValues?.getTooltipValues(indicatorType, kLineModel.customIndicator) ?: mutableListOf(),
                    this.tooltipLabels?.getTooltipLabels(indicatorType) ?: mutableListOf(),
                    indicatorType
                )
            }
        }
    }

    /**
     * draw Indicator Tooltip Labels (top right corner labels)
     * @param canvas Canvas
     * @param startX Float
     * @param startY Float
     * @param values MutableList<Double?>
     * @param labels MutableList<String>
     * @param indicatorType String
     */
    private fun drawIndicatorTooltipLabels(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        values: MutableList<Double?>,
        labels: MutableList<String>,
        indicatorType: String
    ) {
        var labelX = startX
        val valueSize = values.size
        val lineColorSize = this.indicator.lineColors.size
        for (i in 0 until valueSize) {
            val value = if (values[i] == null) null else "${values[i]}"
            var valueStr = "--"
            if (value != null) {
                valueStr = if (indicatorType == Indicator.Type.VOL) {
                    values[i].formatDecimal(tooltip.volumeDecimalPlace, mode = RoundingMode.DOWN)
                } else {
                    values[i].formatDecimal(tooltip.priceDecimalPlace)
                }
            }
            valueStr = this.tooltip.valueFormatter?.format(
                Tooltip.ValueFormatter.CHART,
                indicatorType,
                "${values[i]}"
            ) ?: valueStr
            val text = "${labels[i]}: $valueStr"
            val textWidth = Utils.getTextWidth(this.paint, text)
            this.paint.color = this.indicator.lineColors[i % lineColorSize]
            canvas.drawText(text, labelX, startY, this.paint)
            labelX += this.dp8ToPx + textWidth
        }
    }

    /**
     * draw Last Price Mark
     * @param canvas Canvas
     */
    private fun drawLastPriceMark(canvas: Canvas) {
        val dataList = this.dataProvider.dataList
        val dataSize = dataList.size
        if (!this.candle.displayLastPriceMark || dataSize == 0) {
            return
        }
        this.lastPriceLinePath.reset()
        val lastPrice = dataList[dataSize - 1].closePrice
        val valueDecimalPrice = this.candleChart.yAxisChart.valueDecimalPrice
        val priceLabel = lastPrice.formatDecimal(valueDecimalPrice)

        val isDrawYAxisTextOutside = this.yAxis.yAxisTextPosition == YAxis.TextPosition.OUTSIDE
        val yAxisDataLabelSize = Utils.getTextSize(this.paint, priceLabel)
        val halfLabelHeight = yAxisDataLabelSize.height() / 2f

        val lineStartX = this.viewPortHandler.contentLeft()
        val lineEndX = when (isDrawYAxisTextOutside) {
            true -> this.viewPortHandler.contentRight()
            false -> {
                this.viewPortHandler.contentRight() -
                    this.tooltip.crossTextRectStrokeLineSize * 2 -
                    this.tooltip.crossTextMargin * 3 -
                    yAxisDataLabelSize.width()
            }
        }

        var priceY = this.candleChart.yAxisChart.getY(lastPrice)
        priceY = max(this.candleChart.offsetTop + this.candleChart.height * 0.05f, min(priceY, this.candleChart.offsetTop + this.candleChart.height * 0.98f))

        val drawPriceMarkListener = this.candle.drawPriceMarkListener
        if (drawPriceMarkListener != null) {
            drawPriceMarkListener.draw(
                canvas, this.paint, Candle.DrawPriceMarkListener.LAST,
                PointF(this.viewPortHandler.contentLeft(), priceY),
                this.viewPortHandler.contentRect,
                this.candle, lastPrice
            )
        } else {
            if (this.candle.lastPriceMarkLineStyle == Component.LineStyle.DASH) {
                this.paint.pathEffect = DashPathEffect(this.candle.lastPriceMarkLineDashValues, 0f)
            }
            this.lastPriceLinePath.apply {
                moveTo(lineStartX, priceY)
                lineTo(lineEndX, priceY)
            }
            canvas.drawPath(
                this.lastPriceLinePath,
                this.paint.apply {
                    strokeWidth = candle.lastPriceMarkLineSize
                    color = candle.lastPriceMarkLineColor
                    style = Paint.Style.STROKE
                }
            )
            this.paint.pathEffect = null

            // draw last price label border
            when {
                this.yAxis.yAxisPosition == YAxis.AxisPosition.LEFT -> {
                    this.yAxisLastPriceLabelStrokePathPoints[0].set(
                        lineStartX - this.tooltip.crossTextMargin,
                        priceY - halfLabelHeight - this.tooltip.crossTextMargin
                    )
                    this.yAxisLastPriceLabelStrokePathPoints[1].set(
                        lineStartX - this.tooltip.crossTextMargin * 3 - yAxisDataLabelSize.width(),
                        this.yAxisLastPriceLabelStrokePathPoints[0].y
                    )
                    this.yAxisLastPriceLabelStrokePathPoints[2].set(
                        this.yAxisLastPriceLabelStrokePathPoints[1].x,
                        priceY + halfLabelHeight + this.tooltip.crossTextMargin
                    )
                    this.yAxisLastPriceLabelStrokePathPoints[3].set(
                        this.yAxisLastPriceLabelStrokePathPoints[0].x,
                        this.yAxisLastPriceLabelStrokePathPoints[2].y
                    )
                }
                this.yAxis.yAxisPosition == YAxis.AxisPosition.RIGHT -> {
                    this.yAxisLastPriceLabelStrokePathPoints[0].set(
                        lineEndX + this.tooltip.crossTextMargin,
                        priceY - halfLabelHeight - this.tooltip.crossTextMargin
                    )
                    this.yAxisLastPriceLabelStrokePathPoints[1].set(
                        lineEndX + this.tooltip.crossTextMargin * 3 + yAxisDataLabelSize.width(),
                        this.yAxisLastPriceLabelStrokePathPoints[0].y
                    )
                    this.yAxisLastPriceLabelStrokePathPoints[2].set(
                        this.yAxisLastPriceLabelStrokePathPoints[1].x,
                        priceY + halfLabelHeight + this.tooltip.crossTextMargin
                    )
                    this.yAxisLastPriceLabelStrokePathPoints[3].set(
                        this.yAxisLastPriceLabelStrokePathPoints[0].x,
                        this.yAxisLastPriceLabelStrokePathPoints[2].y
                    )
                }
            }
            this.lastPriceLinePath.apply {
                reset()
                moveTo(yAxisLastPriceLabelStrokePathPoints[0].x, yAxisLastPriceLabelStrokePathPoints[0].y)
            }
            for (i in 1 until this.yAxisLastPriceLabelStrokePathPoints.size) {
                this.lastPriceLinePath.lineTo(this.yAxisLastPriceLabelStrokePathPoints[i].x, this.yAxisLastPriceLabelStrokePathPoints[i].y)
            }
            this.lastPriceLinePath.close()

            canvas.drawPath(
                this.lastPriceLinePath,
                this.candleChart.yAxisChart.paint.apply {
                    style = Paint.Style.FILL
                    color = candle.lastPriceMarkLineColor
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                }
            )

            // draw last price label
            canvas.drawText(
                priceLabel,
                this.candleChart.yAxisChart.getLabelStartX(),
                priceY + halfLabelHeight,
                this.candleChart.yAxisChart.paint.apply {
                    color = Color.WHITE
                    textSize = yAxis.tickTextSize
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                }
            )
        }
    }
}
