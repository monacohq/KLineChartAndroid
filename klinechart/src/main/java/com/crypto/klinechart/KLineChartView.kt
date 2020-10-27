package com.crypto.klinechart

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.crypto.klinechart.chart.CandleChart
import com.crypto.klinechart.chart.ChartBorderChart
import com.crypto.klinechart.chart.IndicatorChart
import com.crypto.klinechart.chart.TooltipChart
import com.crypto.klinechart.chart.XAxisChart
import com.crypto.klinechart.component.Candle
import com.crypto.klinechart.component.ChartBorderLine
import com.crypto.klinechart.component.Component
import com.crypto.klinechart.component.Indicator
import com.crypto.klinechart.component.Tooltip
import com.crypto.klinechart.component.XAxis
import com.crypto.klinechart.component.YAxis
import com.crypto.klinechart.internal.DataProvider
import com.crypto.klinechart.internal.TouchEvent
import com.crypto.klinechart.internal.ViewPortHandler
import com.crypto.klinechart.internal.utils.CalcIndicatorUtils
import com.crypto.klinechart.internal.utils.Utils
import com.crypto.klinechart.model.KLineModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.max

class KLineChartView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int = 0
) : View(
    context,
    attributeSet,
    defStyleAttr
) {
    /**
     * CustomIndicatorListener
     */
    interface CustomIndicatorListener {

        /**
         * CalcIndicator
         */
        interface CalcIndicator {
            /**
             * calculate indicator
             * @param indicatorType String
             * @param dataList MutableList<KLineModel>
             * @return MutableList<KLineModel>
             */
            fun calcIndicator(indicatorType: String, dataList: MutableList<KLineModel>): MutableList<KLineModel>
        }

        /**
         * CalcYAxisMinMax
         */
        interface CalcYAxisMinMax {
            /**
             * Calculate y-axis min and max value
             * @param indicatorType String
             * @param kLineModel Any?
             * @param minMaxArray DoubleArray
             */
            fun calcYAxisMinMax(indicatorType: String, kLineModel: KLineModel, minMaxArray: DoubleArray)
        }

        /**
         * TooltipLabels
         */
        interface TooltipLabels {
            /**
             * get tooltip labels
             * @param indicatorType String
             * @return MutableList<String>
             */
            fun getTooltipLabels(indicatorType: String): MutableList<String>
        }

        /**
         * TooltipValues
         */
        interface TooltipValues {
            /**
             * get tooltip values
             * @param indicatorType String
             * @param indicatorData KLineModel
             * @return DoubleArray
             */
            fun getTooltipValues(indicatorType: String, indicatorData: Any?): MutableList<Double?>
        }

        /**
         * DrawIndicator
         */
        interface DrawIndicator {
            /**
             * 绘制指标
             * @param canvas Canvas
             * @param paint Paint
             * @param indicator Indicator
             * @param startPoint PointF
             * @param yMax Float
             * @param chartValueRate Float
             * @param dataSpace Float
             * @param spaceRate Float
             * @param drawDataList MutableList<KLineModel>
             * @param indicatorType String
             */
            fun draw(
                canvas: Canvas,
                paint: Paint,
                indicator: Indicator,
                startPoint: PointF,
                yMax: Float,
                chartValueRate: Float,
                dataSpace: Float,
                spaceRate: Float,
                drawDataList: MutableList<KLineModel>,
                indicatorType: String
            )
        }
    }

    /**
     * LoadMoreListener
     */
    interface LoadMoreListener {
        /**
         * loadMore
         */
        fun loadMore()
    }

    /**
     * Grid
     */
    lateinit var chartBorderLine: ChartBorderLine
        private set

    /**
     * XAxis
     */
    lateinit var xAxis: XAxis
        private set

    /**
     * YAxis
     */
    lateinit var yAxis: YAxis
        private set

    /**
     * Candle
     */
    lateinit var candle: Candle
        private set

    /**
     * Indicator
     */
    lateinit var indicator: Indicator
        private set

    /**
     * Tooltip
     */
    lateinit var tooltip: Tooltip
        private set

    /**
     * ChartHeightSizeType
     */
    var chartHeightSizeType = Component.ChartHeightSizeType.FIXED

    /**
     * set whether enable fast scroll or not
     */
    var decelerationEnable = true

    /**
     * noMore
     */
    var noMore = false

    /**
     * LoadMoreListener
     */
    var loadMoreListener: LoadMoreListener? = null

    /**
     * ViewPortHandler
     */
    private lateinit var viewPortHandler: ViewPortHandler

    /**
     * CandleChart
     */
    private lateinit var candleChart: CandleChart

    /**
     * VolChart
     */
    private lateinit var volChart: IndicatorChart

    /**
     * IndicatorChart
     */
    private lateinit var indicatorChart: IndicatorChart

    /**
     * TooltipChart
     */
    private lateinit var tooltipChart: TooltipChart

    /**
     * XAxisChart
     */
    private lateinit var xAxisChart: XAxisChart

    /**
     * GridChart
     */
    private lateinit var chartBorderChart: ChartBorderChart

    /**
     * TouchEvent
     */
    private lateinit var touchEvent: TouchEvent

    /**
     * DataProvider
     */
    private lateinit var dataProvider: DataProvider

    /**
     * CustomIndicatorListener
     */
    private var calcIndicator: CustomIndicatorListener.CalcIndicator? = null

    /**
     * Coroutine Scope
     */
    private val mainScope = MainScope()

    init {
        initializeConfig()
        val typedArray = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.KLineChartView
        )
        initializeChartAttrs(typedArray)
        initializeGridAttrs(typedArray)
        initializeCandleAttrs(typedArray)
        initializeIndicatorAttrs(typedArray)
        initializeTooltipAttrs(typedArray)
        initializeXAxisAttrs(typedArray)
        initializeYAxisAttrs(typedArray)
        typedArray.recycle()
    }

    /**
     * initialize Config
     */
    private fun initializeConfig() {
        Utils.init(context)
        this.candle = Candle()
        this.xAxis = XAxis()
        this.yAxis = YAxis()
        this.indicator = Indicator()
        this.tooltip = Tooltip()
        this.chartBorderLine = ChartBorderLine()
        this.viewPortHandler = ViewPortHandler()
        this.dataProvider = DataProvider(this.viewPortHandler)
        this.candleChart = CandleChart(
            this.candle,
            this.indicator,
            this.xAxis,
            this.yAxis,
            this.dataProvider,
            this.viewPortHandler
        )
        this.volChart = IndicatorChart(
            this.indicator,
            this.xAxis,
            this.yAxis,
            this.dataProvider,
            this.viewPortHandler
        )
        this.indicatorChart = IndicatorChart(
            this.indicator,
            this.xAxis,
            this.yAxis,
            this.dataProvider,
            this.viewPortHandler
        )
        this.tooltipChart = TooltipChart(
            this.candleChart,
            this.volChart,
            this.indicatorChart,
            this.tooltip,
            this.candle,
            this.indicator,
            this.yAxis,
            this.dataProvider,
            this.viewPortHandler
        )

        this.xAxisChart = XAxisChart(
            this.xAxis,
            this.dataProvider,
            this.viewPortHandler
        )
        this.chartBorderChart = ChartBorderChart(
            this.chartBorderLine,
            this.viewPortHandler
        )
        this.touchEvent = TouchEvent(this, this.dataProvider, this.viewPortHandler)
    }

    /**
     * initialize Chart Attrs
     * @param typedArray TypedArray
     */
    private fun initializeChartAttrs(typedArray: TypedArray) {
        this.candleChart.indicatorType = typedArray.getString(R.styleable.KLineChartView_mainIndicatorType) ?: Indicator.Type.MA

        val displayVolIndicatorChart = typedArray.getBoolean(R.styleable.KLineChartView_displayVolIndicatorChart, true)
        if (displayVolIndicatorChart) {
            this.volChart.indicatorType = Indicator.Type.VOL
        } else {
            this.volChart.indicatorType = Indicator.Type.NO
        }

        this.indicatorChart.indicatorType = typedArray.getString(R.styleable.KLineChartView_subIndicatorType) ?: Indicator.Type.MACD

        this.chartHeightSizeType = typedArray.getString(R.styleable.KLineChartView_chartHeightSizeType) ?: Component.ChartHeightSizeType.FIXED
        val volChartHeight: Float
        val indicatorChartHeight: Float
        if (this.chartHeightSizeType == Component.ChartHeightSizeType.SCALE) {
            volChartHeight = typedArray.getFloat(R.styleable.KLineChartView_volChartHeight, -1f)
            indicatorChartHeight = typedArray.getFloat(R.styleable.KLineChartView_indicatorChartHeight, -1f)
        } else {
            volChartHeight = typedArray.getDimension(R.styleable.KLineChartView_volChartHeight, -1f)
            indicatorChartHeight = typedArray.getDimension(R.styleable.KLineChartView_indicatorChartHeight, -1f)
        }
        setVolChartHeight(volChartHeight)
        setSubIndicatorChartHeight(indicatorChartHeight)
        this.decelerationEnable = typedArray.getBoolean(R.styleable.KLineChartView_decelerationEnable, this.decelerationEnable)
    }

    /**
     * initialize Grid Attrs
     * @param typedArray TypedArray
     */
    private fun initializeGridAttrs(typedArray: TypedArray) {
        this.chartBorderLine.apply {
            displayChartBorderLine = typedArray.getBoolean(R.styleable.KLineChartView_chartBorderLine_displayLine, displayChartBorderLine)
            lineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_chartBorderLine_lineSize, lineSize.toInt()).toFloat()
            lineColor = typedArray.getColor(R.styleable.KLineChartView_chartBorderLine_lineColor, lineColor)
        }
    }

    /**
     * initialize Candle Attrs
     * @param typedArray TypedArray
     */
    private fun initializeCandleAttrs(typedArray: TypedArray) {
        this.candle.apply {
            increasingColor = typedArray.getColor(R.styleable.KLineChartView_candle_increasingColor, increasingColor)
            decreasingColor = typedArray.getColor(R.styleable.KLineChartView_candle_decreasingColor, decreasingColor)
            candleStyle = typedArray.getInt(R.styleable.KLineChartView_candle_style, candleStyle)
            chartStyle = typedArray.getInt(R.styleable.KLineChartView_candle_chartStyle, chartStyle)
            displayHighestPriceMark = typedArray.getBoolean(R.styleable.KLineChartView_candle_displayHighestPriceMark, displayHighestPriceMark)
            displayLowestPriceMark = typedArray.getBoolean(R.styleable.KLineChartView_candle_displayLowestPriceMark, displayLowestPriceMark)
            lowestHighestPriceMarkTextColor = typedArray.getColor(R.styleable.KLineChartView_candle_lowestHighestPriceMarkTextColor, lowestHighestPriceMarkTextColor)
            lowestHighestPriceMarkTextSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_candle_lowestHighestPriceMarkTextSize, lowestHighestPriceMarkTextSize.toInt()).toFloat()
            displayLastPriceMark = typedArray.getBoolean(R.styleable.KLineChartView_candle_displayLastPriceMark, displayLastPriceMark)
            lastPriceMarkLineStyle = typedArray.getInt(R.styleable.KLineChartView_candle_lastPriceMarkLineStyle, lastPriceMarkLineStyle)
            lastPriceMarkLineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_candle_lastPriceMarkLineSize, lastPriceMarkLineSize.toInt()).toFloat()
            lastPriceMarkLineColor = typedArray.getColor(R.styleable.KLineChartView_candle_lastPriceMarkLineColor, lastPriceMarkLineColor)
            timeLineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_candle_timeLineSize, timeLineSize.toInt()).toFloat()
            timeLineColor = typedArray.getColor(R.styleable.KLineChartView_candle_timeLineColor, timeLineColor)
            timeLineFillColor = typedArray.getColor(R.styleable.KLineChartView_candle_timeLineFillColor, timeLineFillColor)
            timeAverageLineColor = typedArray.getColor(R.styleable.KLineChartView_candle_timeAverageLineColor, timeAverageLineColor)
        }
    }

    /**
     * initialize Indicator Attrs
     * @param typedArray TypedArray
     */
    private fun initializeIndicatorAttrs(typedArray: TypedArray) {
        this.indicator.apply {
            lineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_indicator_lineSize, lineSize.toInt()).toFloat()
            increasingColor = typedArray.getColor(R.styleable.KLineChartView_indicator_increasingColor, increasingColor)
            decreasingColor = typedArray.getColor(R.styleable.KLineChartView_indicator_decreasingColor, decreasingColor)
        }
    }

    /**
     * initialize Tooltip Attrs
     * @param typedArray TypedArray
     */
    private fun initializeTooltipAttrs(typedArray: TypedArray) {
        this.tooltip.apply {
            crosshairsStyle = typedArray.getInt(R.styleable.KLineChartView_tooltip_crossLineStyle, crosshairsStyle)
            crosshairsSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_tooltip_crossLineSize, crosshairsSize.toInt()).toFloat()
            crosshairsColor = typedArray.getColor(R.styleable.KLineChartView_tooltip_crossLineColor, crosshairsColor)
            crossTextRectStrokeLineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_tooltip_crossTextRectStrokeLineSize, crossTextRectStrokeLineSize.toInt()).toFloat()
            crossTextRectStrokeLineColor = typedArray.getColor(R.styleable.KLineChartView_tooltip_crossTextRectStrokeLineColor, crossTextRectStrokeLineColor)
            crossTextRectFillColor = typedArray.getColor(R.styleable.KLineChartView_tooltip_crossTextRectFillColor, crossTextRectFillColor)
            crossTextColor = typedArray.getColor(R.styleable.KLineChartView_tooltip_crossTextColor, crossTextColor)
            crossTextSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_tooltip_crossTextSize, crossTextSize.toInt()).toFloat()
            crossTextMargin = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_tooltip_crossTextMarginSpace, crossTextMargin.toInt()).toFloat()
            generalDataRectStrokeLineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_tooltip_generalDataRectStrokeLineSize, generalDataRectStrokeLineSize.toInt()).toFloat()
            generalDataRectStrokeLineColor = typedArray.getColor(R.styleable.KLineChartView_tooltip_generalDataRectStrokeLineColor, generalDataRectStrokeLineColor)
            generalDataRectFillColor = typedArray.getColor(R.styleable.KLineChartView_tooltip_generalDataRectFillColor, generalDataRectFillColor)
            generalDataTextSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_tooltip_generalDataTextSize, generalDataTextSize.toInt()).toFloat()
            generalDataTextColor = typedArray.getColor(R.styleable.KLineChartView_tooltip_generalDataTextColor, generalDataTextColor)
            generalDataIncreasingColor = typedArray.getColor(R.styleable.KLineChartView_tooltip_generalDataIncreasingColor, generalDataIncreasingColor)
            generalDataDecreasingColor = typedArray.getColor(R.styleable.KLineChartView_tooltip_generalDataDecreasingColor, generalDataDecreasingColor)
            indicatorDisplayRule = typedArray.getInt(R.styleable.KLineChartView_tooltip_indicatorDisplayRule, indicatorDisplayRule)
            indicatorTextSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_tooltip_indicatorTextSize, indicatorTextSize.toInt()).toFloat()
        }
    }

    /**
     * initialize XAxis Attrs
     * @param typedArray TypedArray
     */
    private fun initializeXAxisAttrs(typedArray: TypedArray) {
        this.xAxis.apply {
            displayAxisLine = typedArray.getBoolean(R.styleable.KLineChartView_xaxis_displayAxisLine, displayAxisLine)
            axisLineColor = typedArray.getColor(R.styleable.KLineChartView_xaxis_axisLineColor, axisLineColor)
            axisLineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_xaxis_axisLineSize, axisLineSize.toInt()).toFloat()
            displayTickText = typedArray.getBoolean(R.styleable.KLineChartView_xaxis_displayTickText, displayTickText)
            tickTextColor = typedArray.getColor(R.styleable.KLineChartView_xaxis_tickTextColor, tickTextColor)
            tickTextSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_xaxis_tickTextSize, tickTextSize.toInt()).toFloat()
            displayTickLine = typedArray.getBoolean(R.styleable.KLineChartView_xaxis_displayTickLine, true)
            tickLineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_xaxis_tickLineSize, tickLineSize.toInt()).toFloat()
            displayGridLine = typedArray.getBoolean(R.styleable.KLineChartView_xaxis_displayGridLine, displayGridLine)
            gridLineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_xaxis_gridLineSize, gridLineSize.toInt()).toFloat()
            gridLineColor = typedArray.getColor(R.styleable.KLineChartView_xaxis_gridLineColor, gridLineColor)
            gridLineStyle = typedArray.getInt(R.styleable.KLineChartView_xaxis_gridLineStyle, gridLineStyle)
            textMarginSpace = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_xaxis_textMarginSpace, textMarginSpace.toInt()).toFloat()
            xAxisMaxHeight = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_xaxis_axisMaxHeight, xAxisMaxHeight.toInt()).toFloat()
            xAxisMinHeight = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_xaxis_axisMinHeight, xAxisMinHeight.toInt()).toFloat()
        }
    }

    /**
     * initialize YAxis Attrs
     * @param typedArray TypedArray
     */
    private fun initializeYAxisAttrs(typedArray: TypedArray) {
        this.yAxis.apply {
            displayAxisLine = typedArray.getBoolean(R.styleable.KLineChartView_yaxis_displayAxisLine, false)
            axisLineColor = typedArray.getColor(R.styleable.KLineChartView_yaxis_axisLineColor, axisLineColor)
            axisLineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_yaxis_axisLineSize, axisLineSize.toInt()).toFloat()
            displayTickText = typedArray.getBoolean(R.styleable.KLineChartView_yaxis_displayTickText, displayTickText)
            tickTextColor = typedArray.getColor(R.styleable.KLineChartView_yaxis_tickTextColor, tickTextColor)
            tickTextSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_yaxis_tickTextSize, tickTextSize.toInt()).toFloat()
            displayTickLine = typedArray.getBoolean(R.styleable.KLineChartView_yaxis_displayTickLine, false)
            tickLineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_yaxis_tickLineSize, tickLineSize.toInt()).toFloat()
            displayGridLine = typedArray.getBoolean(R.styleable.KLineChartView_yaxis_displayGridLine, displayGridLine)
            gridLineSize = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_yaxis_gridLineSize, gridLineSize.toInt()).toFloat()
            gridLineColor = typedArray.getColor(R.styleable.KLineChartView_yaxis_gridLineColor, gridLineColor)
            gridLineStyle = typedArray.getInt(R.styleable.KLineChartView_yaxis_gridLineStyle, gridLineStyle)
            textMarginSpace = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_yaxis_textMarginSpace, textMarginSpace.toInt()).toFloat()
            yAxisTextPosition = typedArray.getInt(R.styleable.KLineChartView_yaxis_textPosition, yAxisTextPosition)
            yAxisPosition = typedArray.getInt(R.styleable.KLineChartView_yaxis_axisPosition, yAxisPosition)
            yAxisMaxWidth = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_yaxis_axisMaxWidth, yAxisMaxWidth.toInt()).toFloat()
            yAxisMinWidth = typedArray.getDimensionPixelSize(R.styleable.KLineChartView_yaxis_axisMinWidth, yAxisMinWidth.toInt()).toFloat()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = Utils.convertDpToPixel(50f).toInt()
        setMeasuredDimension(
            max(suggestedMinimumWidth, resolveSize(size, widthMeasureSpec)),
            max(suggestedMinimumHeight, resolveSize(size, heightMeasureSpec))
        )
        calcChartHeight()
        calcOffsets()
    }

    /**
     * calculate each chart's height
     */
    private fun calcChartHeight() {
        val xChartHeight = this.xAxis.getRequiredHeightSpace()
        val totalChartHeight = measuredHeight - paddingBottom - paddingTop - xChartHeight
        val isDisplayVolChart = isDisplayVolChart()
        val isDisplayIndicatorChart = isDisplayIndicatorChart()
        var volChartHeight = this.volChart.height
        var indicatorChartHeight = this.indicatorChart.height

        when {
            isDisplayVolChart && isDisplayIndicatorChart -> {
                val defaultChartHeight = totalChartHeight * 0.2f
                if (this.chartHeightSizeType == Component.ChartHeightSizeType.SCALE) {
                    volChartHeight = totalChartHeight * this.volChart.chartHeightScale
                    indicatorChartHeight = totalChartHeight * this.indicatorChart.chartHeightScale
                }
                volChartHeight = fixChartHeight(totalChartHeight, volChartHeight, defaultChartHeight)
                indicatorChartHeight = fixChartHeight(totalChartHeight, indicatorChartHeight, defaultChartHeight)

                if (totalChartHeight < volChartHeight + indicatorChartHeight) {
                    volChartHeight = defaultChartHeight
                    indicatorChartHeight = defaultChartHeight
                }
            }
            isDisplayVolChart && !isDisplayIndicatorChart -> {
                val defaultChartHeight = totalChartHeight * 0.3f
                if (this.chartHeightSizeType == Component.ChartHeightSizeType.SCALE) {
                    volChartHeight = totalChartHeight * this.volChart.chartHeightScale
                }
                volChartHeight = fixChartHeight(totalChartHeight, volChartHeight, defaultChartHeight)
                indicatorChartHeight = -1f
            }
            !isDisplayVolChart && isDisplayIndicatorChart -> {
                val defaultChartHeight = totalChartHeight * 0.3f
                if (this.chartHeightSizeType == Component.ChartHeightSizeType.SCALE) {
                    indicatorChartHeight = totalChartHeight * this.indicatorChart.chartHeightScale
                }
                indicatorChartHeight = fixChartHeight(totalChartHeight, indicatorChartHeight, defaultChartHeight)
                volChartHeight = -1f
            }
        }
        val candleChartHeight = totalChartHeight - volChartHeight - indicatorChartHeight
        var contentTop = paddingTop.toFloat()
        this.candleChart.setChartDimens(candleChartHeight, contentTop)

        contentTop += candleChartHeight
        this.volChart.setChartDimens(volChartHeight, contentTop)

        contentTop += volChartHeight
        this.indicatorChart.setChartDimens(indicatorChartHeight, contentTop)

        contentTop += indicatorChartHeight
        this.xAxisChart.setChartDimens(xChartHeight, contentTop)
    }

    /**
     * set the chart height, prevent chart is exceed view height
     * @param totalChartHeight Float
     * @param chartHeight Float
     * @param defaultHeight Float
     * @return Float
     */
    private fun fixChartHeight(totalChartHeight: Float, chartHeight: Float, defaultHeight: Float): Float {
        if (chartHeight < 0 || totalChartHeight < chartHeight) {
            return defaultHeight
        }
        return chartHeight
    }

    /**
     * draw all component of the KLineChart, control drawing order
     */
    override fun onDraw(canvas: Canvas) {
        this.dataProvider.space(getCandleChartYAxisLabelInsideLabelWidth(), yAxisPositionIsRight(), yAxisLabelPositionIsInside(), getYAxisLabelOffset())
        this.chartBorderChart.draw(canvas)
        this.xAxisChart.draw(canvas)
        this.candleChart.draw(canvas)
        this.volChart.draw(canvas)
        this.indicatorChart.draw(canvas)
        this.tooltipChart.draw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        return this.touchEvent.onTouch(this, event)
    }

    override fun computeScroll() {
        this.touchEvent.computeScroll()
    }

    /**
     * calculate chart size without x-axis and y-axis
     */
    fun calcOffsets() {
        var offsetLeft = paddingLeft.toFloat()
        var offsetRight = paddingRight.toFloat()
        val offsetTop = paddingTop.toFloat()
        var offsetBottom = paddingBottom.toFloat()

        if (this.yAxis.needsOffset()) {
            // calculate y-axis max width
            val yAxisRequireWidthSpace = this.yAxis.getRequiredWidthSpace()

            if (this.yAxis.yAxisPosition == YAxis.AxisPosition.LEFT) {
                offsetLeft += yAxisRequireWidthSpace
            } else {
                offsetRight += yAxisRequireWidthSpace
            }
        }

        val requireXAxisHeight = this.xAxis.getRequiredHeightSpace()
        offsetBottom += requireXAxisHeight

        this.viewPortHandler.setDimens(
            offsetLeft, offsetTop,
            measuredWidth - offsetRight,
            measuredHeight - offsetBottom
        )
    }

    /**
     * calculate Indicator
     * @param indicatorType String
     */
    private fun calcIndicator(indicatorType: String) {
        this.mainScope.launch {
            when (indicatorType) {
                Indicator.Type.NO -> {
                }

                Indicator.Type.MA -> {
                    this@KLineChartView.dataProvider.dataList =
                        CalcIndicatorUtils.calcMa(this@KLineChartView.dataProvider.dataList)
                }
                Indicator.Type.MACD -> {
                    this@KLineChartView.dataProvider.dataList =
                        CalcIndicatorUtils.calcMacd(this@KLineChartView.dataProvider.dataList)
                }
                Indicator.Type.VOL -> {
                }
                Indicator.Type.BOLL -> {
                    this@KLineChartView.dataProvider.dataList =
                        CalcIndicatorUtils.calcBoll(this@KLineChartView.dataProvider.dataList)
                }
                Indicator.Type.KDJ -> {
                    this@KLineChartView.dataProvider.dataList =
                        CalcIndicatorUtils.calcKdj(this@KLineChartView.dataProvider.dataList)
                }
                Indicator.Type.RSI -> {
                    this@KLineChartView.dataProvider.dataList =
                        CalcIndicatorUtils.calcRsi(this@KLineChartView.dataProvider.dataList)
                }
                else -> {
                    this@KLineChartView.dataProvider.dataList =
                        this@KLineChartView.calcIndicator?.calcIndicator(
                        indicatorType,
                        this@KLineChartView.dataProvider.dataList
                    ) ?: this@KLineChartView.dataProvider.dataList
                }
            }
            invalidate()
        }
    }

    /**
     * calculate ChartIndicator
     */
    private fun calcChartIndicator() {
        if (this.candleChart.isDisplayIndicatorChart()) {
            calcIndicator(this.candleChart.indicatorType)
        }
        if (this.volChart.isDisplayIndicatorChart()) {
            calcIndicator(Indicator.Type.VOL)
        }
        if (this.indicatorChart.isDisplayIndicatorChart()) {
            calcIndicator(this.indicatorChart.indicatorType)
        }
    }

    /**
     * add single [KLineModel] Data
     * @param kLineModel KLineModel
     * @param pos Int
     */
    @JvmOverloads
    @Synchronized
    fun addData(kLineModel: KLineModel, pos: Int = getDataList().size) {
        this.dataProvider.addData(kLineModel, pos)
        calcChartIndicator()
    }

    /**
     * add list of [KLineModel] Data
     * @param list MutableList<KLineModel>
     * @param pos Int
     */
    @JvmOverloads
    @Synchronized
    fun addData(list: MutableList<KLineModel>, pos: Int = 0) {
        val dataSize = list.size
        if (dataSize > 0) {
            this.dataProvider.addData(list, pos)
            calcChartIndicator()
        }
    }

    /**
     * set Candle Stick Chart IndicatorType
     * @param indicatorType String
     */
    fun setCandleStickChartIndicatorType(indicatorType: String) {
        if (this.candleChart.indicatorType != indicatorType) {
            this.candleChart.indicatorType = indicatorType
            calcIndicator(indicatorType)
        }
    }

    /**
     * get Candle Stick Chart IndicatorType
     * @return String
     */
    fun getMainIndicatorType() = this.candleChart.indicatorType

    /**
     * set whether show vol indicator chart or not
     * @param isShow Boolean
     */
    fun setShowVolIndicatorChart(isShow: Boolean) {
        if (isShow) {
            this.volChart.indicatorType = Indicator.Type.VOL
            calcChartIndicator()
        } else {
            this.volChart.indicatorType = Indicator.Type.NO
        }
        calcChartHeight()
    }

    /**
     * get whether show Volume Chart or not
     * @return Boolean
     */
    fun isDisplayVolChart() = this.volChart.isDisplayIndicatorChart()

    /**
     * set Sub IndicatorType
     * @param indicatorType String
     */
    fun setSubIndicatorType(indicatorType: String) {
        if (this.indicatorChart.indicatorType != indicatorType) {
            val shouldCalcChartHeight = indicatorType == Indicator.Type.NO || this.indicatorChart.indicatorType == Indicator.Type.NO
            this.indicatorChart.indicatorType = indicatorType
            if (shouldCalcChartHeight) {
                calcChartHeight()
            }
            calcIndicator(indicatorType)
        }
    }

    /**
     * get whether show Sub IndicatorChart
     * @return Boolean
     */
    fun isDisplayIndicatorChart() = this.indicatorChart.isDisplayIndicatorChart()

    /**
     * get Sub IndicatorType
     * @return Int
     */
    fun getSubIndicatorType() = this.indicatorChart.indicatorType

    /**
     * set Custom Indicator Listener
     * @param calcIndicator CalcIndicator?
     * @param calcYAxisMinMax CalcYAxisMinMax?
     * @param tooltipLabels TooltipLabels?
     * @param tooltipValues TooltipValues?
     * @param drawIndicator DrawIndicator?
     */
    fun setCustomIndicatorListener(
        calcIndicator: CustomIndicatorListener.CalcIndicator?,
        calcYAxisMinMax: CustomIndicatorListener.CalcYAxisMinMax?,
        tooltipLabels: CustomIndicatorListener.TooltipLabels?,
        tooltipValues: CustomIndicatorListener.TooltipValues?,
        drawIndicator: CustomIndicatorListener.DrawIndicator?
    ) {
        this.calcIndicator = calcIndicator
        this.indicatorChart.yAxisChart.calcYAxisMinMax = calcYAxisMinMax
        this.indicatorChart.drawCustomIndicator = drawIndicator
        this.tooltipChart.tooltipLabels = tooltipLabels
        this.tooltipChart.tooltipValues = tooltipValues
    }

    /**
     * get Data List
     * @return MutableList<KLineModel>
     */
    fun getDataList() = this.dataProvider.dataList

    /**
     * set Vol Chart Height
     * @param height Float
     */
    fun setVolChartHeight(height: Float) {
        if (this.chartHeightSizeType == Component.ChartHeightSizeType.SCALE) {
            this.volChart.chartHeightScale = height
        } else {
            this.volChart.height = Utils.convertDpToPixel(height)
        }
    }

    /**
     * set Sub Indicator Chart Height
     * @param height Float
     */
    fun setSubIndicatorChartHeight(height: Float) {
        if (this.chartHeightSizeType == Component.ChartHeightSizeType.SCALE) {
            this.indicatorChart.chartHeightScale = height
        } else {
            this.indicatorChart.height = Utils.convertDpToPixel(height)
        }
    }

    /**
     * set price decimal place
     * @param dp Int
     */
    fun setPriceDecimalPlace(dp: Int) {
        this.tooltip.priceDecimalPlace = dp
        this.candleChart.yAxisChart.valueDecimalPrice = dp
    }

    /**
     * set volume decimal place
     * @param dp Int
     */
    fun setVolumeDecimalPlace(dp: Int) {
        this.tooltip.volumeDecimalPlace = dp
        this.volChart.yAxisChart.valueDecimalPrice = dp
    }

    fun getCandleChartYAxisLabelStartX(): Float {
        return this.candleChart.yAxisChart.getLabelStartX()
    }

    fun getCandleChartYAxisLabelInsideLabelWidth(): Int {
        return this.candleChart.yAxisChart.getLabelWidth()
    }

    fun yAxisPositionIsRight(): Boolean {
        return this.yAxis.yAxisPosition == YAxis.AxisPosition.RIGHT
    }

    fun yAxisLabelPositionIsInside(): Boolean {
        return this.yAxis.yAxisTextPosition == YAxis.TextPosition.INSIDE
    }

    fun getYAxisLabelOffset(): Int {
        return this.candleChart.yAxisChart.getYAxisLabelOffset()
    }

    /**
     * loadComplete
     */
    fun loadComplete() {
        this.dataProvider.isLoading = false
    }

    /**
     * clear Data List
     */
    fun clearDataList() {
        this.dataProvider.dataList.clear()
    }

    override fun onDetachedFromWindow() {
        // cancel coroutine scope
        this.mainScope.cancel()
        super.onDetachedFromWindow()
    }
}
