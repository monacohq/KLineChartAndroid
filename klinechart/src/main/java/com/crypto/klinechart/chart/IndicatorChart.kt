package com.crypto.klinechart.chart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.crypto.klinechart.KLineChartView
import com.crypto.klinechart.component.Indicator
import com.crypto.klinechart.component.XAxis
import com.crypto.klinechart.component.YAxis
import com.crypto.klinechart.internal.DataProvider
import com.crypto.klinechart.internal.ViewPortHandler
import com.crypto.klinechart.model.KLineModel
import kotlin.math.min

internal open class IndicatorChart(
    private val indicator: Indicator,
    private val xAxis: XAxis,
    private val yAxis: YAxis,
    private val dataProvider: DataProvider,
    private val viewPortHandler: ViewPortHandler
) : Chart() {

    /**
     * y-axis chart
     */
    val yAxisChart = YAxisChart(
        yAxis,
        dataProvider,
        viewPortHandler
    )

    /**
     * indicator Type
     */
    var indicatorType = Indicator.Type.MACD

    /**
     * chart height scale
     */
    var chartHeightScale = -1f

    /**
     * draw custom Indicator
     */
    var drawCustomIndicator: KLineChartView.CustomIndicatorListener.DrawIndicator? = null

    private val barBuffers = FloatArray(4)

    private val linePaths = arrayListOf(Path(), Path(), Path(), Path(), Path())
    private val pathHasMoves = booleanArrayOf(false, false, false, false, false)

    private val clipRect = RectF()

    override fun setChartDimens(height: Float, offsetTop: Float) {
        super.setChartDimens(height, offsetTop)
        this.yAxisChart.setChartDimens(height, offsetTop)
    }

    override fun draw(canvas: Canvas) {
        if (isDraw()) {
            val isMainIndicator = isMain()
            val labelCount = if (isMainIndicator) 6 else 3
            drawChartHorizontalSeparatorLine(canvas)
            this.yAxisChart.apply {
                getYAxisDataMinMax(indicatorType, isMainIndicator, isTimeLine())
                computeAxis(labelCount)
                drawGridLines(canvas)
                drawAxisLine(canvas)
            }

            drawChart(canvas)

            this.yAxisChart.apply {
                drawTickLines(canvas)
                drawAxisLabels(canvas, indicatorType)
            }
        }
    }

    /**
     * draw Chart
     * @param canvas Canvas
     */
    open fun drawChart(canvas: Canvas) {
        drawIndicator(canvas)
    }

    /**
     * draw Chart Horizontal Separator Line
     * @param canvas Canvas
     */
    private fun drawChartHorizontalSeparatorLine(canvas: Canvas) {
        if (isDrawSeparatorLine()) {
            this.paint.apply {
                strokeWidth = xAxis.axisLineSize
                color = xAxis.axisLineColor
                style = Paint.Style.STROKE
            }
            canvas.drawLine(
                this.viewPortHandler.contentLeft(),
                this.offsetTop,
                this.viewPortHandler.contentRight(),
                this.offsetTop,
                this.paint
            )
        }
    }

    /**
     * draw Indicator
     * @param canvas Canvas
     */
    fun drawIndicator(canvas: Canvas) {
        for (i in 0 until this.linePaths.size) {
            this.pathHasMoves[i] = false
            this.linePaths[i].reset()
        }
        var isDraw = true
        var onDrawing: ((i: Int, x: Float, halfBarSpace: Float, kLineModel: KLineModel) -> Unit) = { _, _, _, _ -> }
        var lineNumber = 0
        this.paint.strokeWidth = indicator.lineSize
        when (this.indicatorType) {
            Indicator.Type.NO -> {
                isDraw = false
            }

            Indicator.Type.MA -> {
                lineNumber = 4
                val isMainIndicator = isMain()
                val dataList = this.dataProvider.dataList
                onDrawing = { i, x, halfBarSpace, kLineModel ->
                    val ma = kLineModel.ma
                    preparePath(listOf(ma?.ma5, ma?.ma10, ma?.ma20, ma?.ma60), x)
                    drawIndicatorOhlc(canvas, kLineModel, isMainIndicator, dataList, i, halfBarSpace, x)
                }
            }

            Indicator.Type.MACD -> {
                val dataList = this.dataProvider.dataList
                lineNumber = 2
                onDrawing = { i, x, halfBarSpace, kLineModel ->
                    preparePath(listOf(kLineModel.macd?.diff, kLineModel.macd?.dea), x)
                    var refKLineModel: KLineModel? = null
                    if (i > 0) {
                        refKLineModel = dataList[i - 1]
                    }
                    val macd = kLineModel.macd?.macd ?: -1.0
                    val refMacd = refKLineModel?.macd?.macd
                    if (macd > 0) {
                        this.paint.color = indicator.increasingColor
                    } else {
                        this.paint.color = indicator.decreasingColor
                    }
                    if (refMacd != null && macd > refMacd) {
                        this.paint.style = Paint.Style.STROKE
                    } else {
                        this.paint.style = Paint.Style.FILL
                    }
                    drawBars(canvas, x, halfBarSpace, macd)
                }
            }

            Indicator.Type.VOL -> {
                lineNumber = 3
                onDrawing = { i, x, halfBarSpace, kLineModel ->
                    val vol = kLineModel.volume

                    this.paint.style = Paint.Style.FILL
                    if (kLineModel.closePrice >= kLineModel.openPrice) {
                        this.paint.color = indicator.increasingColor
                    } else {
                        this.paint.color = indicator.decreasingColor
                    }
                    drawBars(canvas, x, halfBarSpace, vol)
                }
            }

            Indicator.Type.BOLL -> {
                val isMainIndicator = isMain()
                val dataList = this.dataProvider.dataList
                lineNumber = 3
                onDrawing = { i, x, halfBarSpace, kLineModel ->
                    val boll = kLineModel.boll
                    preparePath(listOf(boll?.up, boll?.mid, boll?.dn), x)
                    drawIndicatorOhlc(canvas, kLineModel, isMainIndicator, dataList, i, halfBarSpace, x)
                }
            }

            Indicator.Type.KDJ -> {
                lineNumber = 3
                onDrawing = { _, x, _, kLineModel ->
                    val kdj = kLineModel.kdj
                    preparePath(listOf(kdj?.k, kdj?.d, kdj?.j), x)
                }
            }

            Indicator.Type.RSI -> {
                lineNumber = 3
                onDrawing = { _, x, _, kLineModel ->
                    val rsi = kLineModel.rsi
                    preparePath(listOf(rsi?.rsi1, rsi?.rsi2, rsi?.rsi3), x)
                }
            }
            else -> {
                isDraw = false
                val visibleDataMinPos = this.dataProvider.getVisibleDataMinPos()
                val lastPos = min(visibleDataMinPos + this.dataProvider.getVisibleDataCount(), this.dataProvider.dataList.size)
                val dataList = this.dataProvider.dataList.subList(visibleDataMinPos, lastPos)
                val chartValueRate = this.height / this.yAxisChart.axisRange
                this.drawCustomIndicator?.draw(
                    canvas, this.paint, this.indicator,
                    PointF(this.viewPortHandler.contentLeft(), this.offsetTop),
                    this.yAxisChart.axisMaximum, chartValueRate,
                    this.dataProvider.getChartDataSpace(), DataProvider.DATA_SPACE_RATE,
                    dataList, this.indicatorType
                )
            }
        }
        if (isDraw) {
            drawGraphs(canvas, onDrawing) {
                drawLines(canvas, lineNumber)
            }
        }
    }

    /**
     * draw Graphs
     * @param canvas Canvas
     * @param onDrawing Function4<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] Float, [@kotlin.ParameterName] Float, [@kotlin.ParameterName] KLineModel, Unit>
     * @param onDrawEnd Function0<Unit>
     */
    inline fun drawGraphs(
        canvas: Canvas,
        onDrawing: (i: Int, x: Float, halfBarSpace: Float, kLineModel: KLineModel) -> Unit,
        onDrawEnd: () -> Unit
    ) {
        val clipRestoreCount = canvas.save()
        this.clipRect.set(
            this.viewPortHandler.contentLeft(), this.offsetTop,
            this.viewPortHandler.contentRight(), this.offsetTop + this.height
        )
        canvas.clipRect(this.clipRect)
        var startX = this.viewPortHandler.contentLeft()
        val chartDataSpace = this.dataProvider.getChartDataSpace()
        val dataSpace = chartDataSpace * (1f - DataProvider.DATA_SPACE_RATE)
        val halfBarSpace = dataSpace / 2
        val firstPos = this.dataProvider.getVisibleDataMinPos()
        val lastPos = min(this.dataProvider.dataList.size, firstPos + this.dataProvider.getVisibleDataCount())

        for (i in firstPos until lastPos) {
            val endX = startX + dataSpace
            val x = (startX + endX) / 2f
            val kLineModel = this.dataProvider.dataList[i]

            onDrawing(i, x, halfBarSpace, kLineModel)

            startX += chartDataSpace
        }
        onDrawEnd()
        canvas.restoreToCount(clipRestoreCount)
    }

    /**
     * draw Indicator Ohlc line
     * @param canvas Canvas
     * @param kLineModel KLineModel
     * @param isMainIndicator Boolean
     * @param dataList MutableList<KLineModel>
     * @param i Int
     * @param halfBarSpace Float
     * @param x Float
     */
    private fun drawIndicatorOhlc(
        canvas: Canvas,
        kLineModel: KLineModel,
        isMainIndicator: Boolean,
        dataList: MutableList<KLineModel>,
        i: Int,
        halfBarSpace: Float,
        x: Float
    ) {
        if (!isMainIndicator) {
            var refKLineModel: KLineModel? = null
            if (i > 0) {
                refKLineModel = dataList[i - 1]
            }
            val refClosePrice = refKLineModel?.closePrice ?: Double.NEGATIVE_INFINITY
            drawOhlc(
                canvas, x, halfBarSpace, refClosePrice,
                kLineModel.openPrice, kLineModel.closePrice,
                kLineModel.highPrice, kLineModel.lowPrice,
                this.indicator.increasingColor, this.indicator.decreasingColor
            )
        }
    }

    /**
     * draw Ohlc
     * @param canvas Canvas
     * @param x Float
     * @param halfBarSpace Float
     * @param refClosePrice Double
     * @param openPrice Double
     * @param closePrice Double
     * @param highPrice Double
     * @param lowPrice Double
     * @param increasingColor Int
     * @param decreasingColor Int
     */
    internal fun drawOhlc(
        canvas: Canvas,
        x: Float,
        halfBarSpace: Float,
        refClosePrice: Double,
        openPrice: Double,
        closePrice:
            Double,
        highPrice: Double,
        lowPrice: Double,
        increasingColor: Int,
        decreasingColor: Int
    ) {
        val priceY = getPriceY(
            openPrice, closePrice,
            highPrice, lowPrice
        )
        if (closePrice > refClosePrice) {
            this.paint.color = increasingColor
        } else {
            this.paint.color = decreasingColor
        }
        canvas.apply {
            drawLine(x, priceY[2], x, priceY[3], paint)
            drawLine(x - halfBarSpace, priceY[0], x, priceY[0], paint)
            drawLine(x + halfBarSpace, priceY[1], x, priceY[1], paint)
        }
    }

    /**
     * prepare Path
     * @param values MutableList<Double?>
     * @param x Float
     */
    private fun preparePath(values: List<Double?>, x: Float) {
        for (i in values.indices) {
            val value = values[i]
            if (value != null) {
                val y = this.yAxisChart.getY(value)
                if (this.pathHasMoves[i]) {
                    this.linePaths[i].lineTo(x, y)
                } else {
                    this.linePaths[i].moveTo(x, y)
                    this.pathHasMoves[i] = true
                }
            }
        }
    }

    private fun drawLines(canvas: Canvas, lineNumber: Int) {
        this.paint.style = Paint.Style.STROKE
        val lineColorSize = this.indicator.lineColors.size
        for (k in 0 until lineNumber) {
            this.paint.color = this.indicator.lineColors[k % lineColorSize]
            canvas.drawPath(this.linePaths[k], this.paint)
        }
    }

    private fun drawBars(canvas: Canvas, x: Float, halfBarSpace: Float, barData: Double?) {
        if (barData != null) {
            this.barBuffers[0] = x - halfBarSpace
            this.barBuffers[2] = x + halfBarSpace
            val dataY = this.yAxisChart.getY(barData)
            val zeroY = this.yAxisChart.getY(0.0)
            this.barBuffers[1] = dataY
            this.barBuffers[3] = zeroY

            canvas.drawRect(this.barBuffers[0], this.barBuffers[1], this.barBuffers[2], this.barBuffers[3], this.paint)
        }
    }

    /**
     * get y point of price
     * @param openPrice Double
     * @param closePrice Double
     * @param highPrice Double
     * @param lowPrice Double
     * @return FloatArray
     */
    fun getPriceY(
        openPrice: Double,
        closePrice: Double,
        highPrice: Double,
        lowPrice: Double
    ): FloatArray = floatArrayOf(
        this.yAxisChart.getY(openPrice),
        this.yAxisChart.getY(closePrice),
        this.yAxisChart.getY(highPrice),
        this.yAxisChart.getY(lowPrice)
    )

    /**
     * get is display Indicator Chart
     * @return Boolean
     */
    fun isDisplayIndicatorChart(): Boolean = this.indicatorType != Indicator.Type.NO

    /**
     * is Draw
     * @return Boolean
     */
    open fun isDraw(): Boolean = isDisplayIndicatorChart() && this.height > 0f

    /**
     * is Main
     * @return Boolean
     */
    open fun isMain(): Boolean = false

    /**
     * is time line
     * @return Boolean
     */
    open fun isTimeLine(): Boolean = false

    /**
     * is DrawSeparatorLine
     * @return Boolean
     */
    open fun isDrawSeparatorLine(): Boolean = true
}
