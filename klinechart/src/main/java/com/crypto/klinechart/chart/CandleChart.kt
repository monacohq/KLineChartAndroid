package com.crypto.klinechart.chart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import com.crypto.klinechart.component.Candle
import com.crypto.klinechart.component.Indicator
import com.crypto.klinechart.component.XAxis
import com.crypto.klinechart.component.YAxis
import com.crypto.klinechart.internal.DataProvider
import com.crypto.klinechart.internal.ViewPortHandler
import com.crypto.klinechart.internal.utils.Utils
import com.crypto.klinechart.internal.utils.formatDecimal
import com.crypto.klinechart.model.KLineModel
import kotlin.math.abs

internal class CandleChart(
    private val candle: Candle,
    private val indicator: Indicator,
    private val xAxis: XAxis,
    private val yAxis: YAxis,
    private val dataProvider: DataProvider,
    private val viewPortHandler: ViewPortHandler
) : IndicatorChart(indicator, xAxis, yAxis, dataProvider, viewPortHandler) {

    /**
     * HighLowPriceMark
     * @property x Float
     * @property price Double
     * @constructor
     */
    private data class HighLowPriceMark(
        val x: Float,
        val price: Double
    )

    /**
     * candleLineSize
     */
    private val candleLineSize = Utils.convertDpToPixel(1f)

    private val shadowBuffers = FloatArray(8)
    private val bodyBuffers = FloatArray(4)

    private val markLinePoints = FloatArray(12)

    /**
     * linePath
     */
    private val linePath = Path()

    /**
     * timeLineAreaPath
     */
    private val timeLineAreaPath = Path()

    /**
     * timeAverageLinePath
     */
    private val timeAverageLinePath = Path()

    /**
     * highestPriceMark
     */
    private var highestPriceMark: HighLowPriceMark? = null

    /**
     * lowestPriceMark
     */
    private var lowestPriceMark: HighLowPriceMark? = null

    private val dp3ToPx = Utils.convertDpToPixel(3f)
    private val dp2ToPx = Utils.convertDpToPixel(2f)
    private val dp6ToPx = Utils.convertDpToPixel(6f)

    override fun drawChart(canvas: Canvas) {
        if (this.candle.chartStyle != Candle.ChartStyle.TIME_LINE) {
            drawCandle(canvas)
            drawIndicator(canvas)
            drawHighestPriceMark(canvas)
            drawLowestPriceMark(canvas)
        } else {
            drawTimeLine(canvas)
        }
    }

    /**
     * draw Highest PriceMark
     * @param canvas Canvas
     */
    private fun drawHighestPriceMark(canvas: Canvas) {
        if (!this.candle.displayHighestPriceMark) {
            return
        }

        val x = this.highestPriceMark?.x ?: return
        if (x < 0) {
            return
        }

        val price = this.highestPriceMark?.price ?: return
        if (price == Double.MAX_VALUE) {
            return
        }

        drawLowestHighestPriceMark(canvas, x, price, Candle.DrawPriceMarkListener.HIGHEST)
    }

    /**
     * draw Lowest PriceMark
     * @param canvas Canvas
     */
    private fun drawLowestPriceMark(canvas: Canvas) {
        if (!this.candle.displayLowestPriceMark) {
            return
        }

        val x = this.lowestPriceMark?.x ?: return
        if (x < 0) {
            return
        }

        val price = this.lowestPriceMark?.price ?: return
        if (price == Double.MAX_VALUE) {
            return
        }

        drawLowestHighestPriceMark(canvas, x, price, Candle.DrawPriceMarkListener.LOWEST)
    }

    /**
     * draw Lowest Highest PriceMark
     * @param canvas Canvas
     * @param x Float
     * @param price Double
     */
    private fun drawLowestHighestPriceMark(canvas: Canvas, x: Float, price: Double, type: Int) {
        this.paint.apply {
            color = candle.lowestHighestPriceMarkTextColor
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val priceY = this.yAxisChart.getY(price)
        val drawPriceMarkListener = this.candle.drawPriceMarkListener
        if (drawPriceMarkListener != null) {
            drawPriceMarkListener.draw(
                canvas, paint, type,
                PointF(x, priceY),
                this.viewPortHandler.contentRect,
                this.candle, price
            )
        } else {
            val start = x + this.dp3ToPx
            this.markLinePoints[0] = start
            this.markLinePoints[1] = priceY
            this.markLinePoints[2] = start + dp6ToPx
            this.markLinePoints[3] = priceY

            this.markLinePoints[4] = start
            this.markLinePoints[5] = priceY
            this.markLinePoints[6] = start + this.dp3ToPx
            this.markLinePoints[7] = priceY - this.dp2ToPx

            this.markLinePoints[8] = start
            this.markLinePoints[9] = priceY
            this.markLinePoints[10] = start + this.dp3ToPx
            this.markLinePoints[11] = priceY + this.dp2ToPx

            canvas.drawLines(this.markLinePoints, this.paint)

            this.paint.apply {
                style = Paint.Style.FILL
                textSize = candle.lowestHighestPriceMarkTextSize
            }
            val priceText = this.candle.valueFormatter?.format(price.toString()) ?: price.formatDecimal()
            val priceTextHeight = Utils.getTextHeight(this.paint, priceText)
            canvas.drawText(priceText, this.markLinePoints[2] + this.dp3ToPx, priceY + priceTextHeight / 2, this.paint)
        }
    }

    /**
     * draw TimeLine
     * @param canvas Canvas
     */
    private fun drawTimeLine(canvas: Canvas) {
        this.linePath.reset()
        this.timeAverageLinePath.reset()
        this.timeLineAreaPath.reset()
        this.paint.apply {
            strokeWidth = candle.timeLineSize
            color = candle.timeLineColor
            style = Paint.Style.STROKE
        }
        this.timeLineAreaPath.moveTo(this.viewPortHandler.contentLeft(), this.offsetTop + this.height)
        val visibleDataMinPos = this.dataProvider.getVisibleDataMinPos()
        val visibleDataCount = this.dataProvider.getVisibleDataCount()
        val dataSize = this.dataProvider.dataList.size
        val onDrawing: (i: Int, x: Float, halfBarSpace: Float, kLineModel: KLineModel) -> Unit = { i, x, _, kLineModel ->
            val closeY = this.yAxisChart.getY(kLineModel.closePrice)
            val averagePrice = kLineModel.averagePrice
            val averagePriceY = this.yAxisChart.getY(averagePrice)
            when (i) {
                visibleDataMinPos -> {
                    this.linePath.moveTo(x, closeY)
                    if (averagePrice != 0.0) {
                        this.timeAverageLinePath.moveTo(x, averagePriceY)
                    }
                    this.timeLineAreaPath.apply {
                        lineTo(viewPortHandler.contentLeft(), closeY)
                        lineTo(x, closeY)
                    }
                }

                visibleDataMinPos + visibleDataCount - 1 -> {
                    this.linePath.lineTo(x, closeY)
                    if (averagePrice != 0.0) {
                        this.timeAverageLinePath.lineTo(x, averagePriceY)
                    }
                    this.timeLineAreaPath.apply {
                        lineTo(x, closeY)
                        lineTo(viewPortHandler.contentRight(), closeY)
                        lineTo(viewPortHandler.contentRight(), offsetTop + height)
                    }
                }

                dataSize - 1 -> {
                    this.linePath.lineTo(x, closeY)
                    if (averagePrice != 0.0) {
                        this.timeAverageLinePath.lineTo(x, averagePriceY)
                    }
                    this.timeLineAreaPath.apply {
                        lineTo(x, closeY)
                        lineTo(x, offsetTop + height)
                    }
                }

                else -> {
                    this.linePath.lineTo(x, closeY)
                    if (averagePrice != 0.0) {
                        this.timeAverageLinePath.lineTo(x, averagePriceY)
                    }
                    this.timeLineAreaPath.lineTo(x, closeY)
                }
            }
        }

        this.drawGraphs(canvas, onDrawing) {
            // draw time line
            this.timeLineAreaPath.close()
            canvas.drawPath(this.linePath, this.paint)

            // draw time line area paint
            this.paint.apply {
                style = Paint.Style.FILL
                color = candle.timeLineFillColor
            }
            canvas.drawPath(this.timeLineAreaPath, this.paint)
            this.paint.shader = null

            // draw time average line
            this.paint.apply {
                style = Paint.Style.STROKE
                color = candle.timeAverageLineColor
            }
            canvas.drawPath(this.timeAverageLinePath, this.paint)
        }
    }

    /**
     * draw Candle
     * @param canvas Canvas
     */
    private fun drawCandle(canvas: Canvas) {
        this.paint.strokeWidth = this.candleLineSize
        val increasingColor = candle.increasingColor
        val decreasingColor = candle.decreasingColor
        var highestPrice = Double.MIN_VALUE
        var lowestPrice = Double.MAX_VALUE
        var highestPriceX = -1f
        var lowestPriceX = -1f
        val dataList = this.dataProvider.dataList
        val onDrawing: (i: Int, x: Float, halfBarSpace: Float, kLineModel: KLineModel) -> Unit = { i, x, halfBarSpace, kLineModel ->
            var refKLineModel: KLineModel? = null
            if (i > 0) {
                refKLineModel = dataList[i - 1]
            }
            val refClosePrice = refKLineModel?.closePrice ?: Double.NEGATIVE_INFINITY
            val openPrice = kLineModel.openPrice
            val closePrice = kLineModel.closePrice
            val highPrice = kLineModel.highPrice
            val lowPrice = kLineModel.lowPrice
            if (closePrice >= openPrice) {
                this.paint.color = increasingColor
            } else {
                this.paint.color = decreasingColor
            }

            drawCandleItem(
                canvas, x, halfBarSpace, refClosePrice,
                kLineModel.openPrice, closePrice,
                highPrice, lowPrice
            )

            if (highestPrice < highPrice) {
                highestPrice = highPrice
                highestPriceX = x
            }

            if (lowPrice < lowestPrice) {
                lowestPrice = lowPrice
                lowestPriceX = x
            }
        }

        drawGraphs(canvas, onDrawing, {})

        this.highestPriceMark =
            HighLowPriceMark(
                highestPriceX,
                highestPrice
            )
        this.lowestPriceMark =
            HighLowPriceMark(
                lowestPriceX,
                lowestPrice
            )
    }

    /**
     * draw Candle Item
     * @param canvas Canvas
     * @param x Float
     * @param halfBarSpace Float
     * @param refClosePrice Double
     * @param openPrice Double
     * @param closePrice Double
     * @param highPrice Double
     * @param lowPrice Double
     */
    private fun drawCandleItem(
        canvas: Canvas,
        x: Float,
        halfBarSpace: Float,
        refClosePrice: Double,
        openPrice: Double,
        closePrice: Double,
        highPrice: Double,
        lowPrice: Double
    ) {
        when (candle.candleStyle) {
            Candle.CandleStyle.SOLID -> {
                this.paint.style = Paint.Style.FILL
                drawCandleReact(
                    canvas, x, halfBarSpace,
                    openPrice, closePrice,
                    highPrice, lowPrice
                )
            }
            Candle.CandleStyle.STROKE -> {
                this.paint.style = Paint.Style.STROKE
                drawCandleReact(
                    canvas, x, halfBarSpace,
                    openPrice, closePrice,
                    highPrice, lowPrice
                )
            }
            Candle.CandleStyle.INCREASING_STROKE -> {
                if (closePrice > refClosePrice) {
                    this.paint.style = Paint.Style.STROKE
                } else {
                    this.paint.style = Paint.Style.FILL
                }
                drawCandleReact(
                    canvas, x, halfBarSpace,
                    openPrice, closePrice,
                    highPrice, lowPrice
                )
            }
            Candle.CandleStyle.DECREASING_STROKE -> {
                if (closePrice > refClosePrice) {
                    this.paint.style = Paint.Style.FILL
                } else {
                    this.paint.style = Paint.Style.STROKE
                }
                drawCandleReact(
                    canvas, x, halfBarSpace,
                    openPrice, closePrice,
                    highPrice, lowPrice
                )
            }
            Candle.CandleStyle.OHLC -> {
                drawOhlc(
                    canvas, x, halfBarSpace, refClosePrice,
                    openPrice, closePrice, highPrice, lowPrice,
                    candle.increasingColor, candle.decreasingColor
                )
            }
            else -> {}
        }
    }

    /**
     * draw Candle React
     * @param canvas Canvas
     */
    private fun drawCandleReact(
        canvas: Canvas,
        x: Float,
        halfBarSpace: Float,
        openPrice: Double,
        closePrice: Double,
        highPrice: Double,
        lowPrice: Double
    ) {
        val priceY = getPriceY(openPrice, closePrice, highPrice, lowPrice)
        this.shadowBuffers[0] = x
        this.shadowBuffers[2] = x
        this.shadowBuffers[4] = x
        this.shadowBuffers[6] = x
        when {
            closePrice < openPrice -> {
                this.shadowBuffers[1] = priceY[2]
                this.shadowBuffers[3] = priceY[0]
                this.shadowBuffers[5] = priceY[3]
                this.shadowBuffers[7] = priceY[1]
            }
            closePrice > openPrice -> {
                this.shadowBuffers[1] = priceY[2]
                this.shadowBuffers[3] = priceY[1]
                this.shadowBuffers[5] = priceY[3]
                this.shadowBuffers[7] = priceY[0]
            }
            else -> {
                this.shadowBuffers[1] = priceY[2]
                this.shadowBuffers[3] = priceY[0]
                this.shadowBuffers[5] = priceY[3]
                this.shadowBuffers[7] = this.shadowBuffers[3]
            }
        }

        this.bodyBuffers[0] = x - halfBarSpace
        this.bodyBuffers[1] = priceY[1]
        this.bodyBuffers[2] = x + halfBarSpace
        this.bodyBuffers[3] = priceY[0]

        if (this.bodyBuffers[1] == this.bodyBuffers[3] ||
            abs(this.bodyBuffers[1] - this.bodyBuffers[3]) < this.candleLineSize
        ) {
            this.paint.strokeWidth = this.candleLineSize
            canvas.drawLine(
                this.bodyBuffers[0], this.bodyBuffers[1],
                this.bodyBuffers[2], this.bodyBuffers[1],
                this.paint
            )
        } else {
            canvas.drawRect(
                this.bodyBuffers[0], this.bodyBuffers[1],
                this.bodyBuffers[2], this.bodyBuffers[3],
                this.paint
            )
        }

        canvas.drawLines(this.shadowBuffers, this.paint)
    }

    override fun isMain(): Boolean = true

    override fun isTimeLine(): Boolean = candle.chartStyle == Candle.ChartStyle.TIME_LINE

    override fun isDrawSeparatorLine(): Boolean = false

    override fun isDraw(): Boolean = true
}
