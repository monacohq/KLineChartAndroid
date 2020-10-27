package com.crypto.klinechart.internal

import android.graphics.PointF
import com.crypto.klinechart.KLineChartView
import com.crypto.klinechart.model.KLineModel
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

internal class DataProvider(private val viewPortHandler: ViewPortHandler) {
    companion object {
        const val DATA_SPACE_RATE = 0.22f
    }

    /**
     * data list
     */
    var dataList = mutableListOf<KLineModel>()

    /**
     * visible data min pos
     */
    private var visibleDataMinPos = 0

    /**
     * number of visible data
     */
    private var visibleDataCount = 120

    /**
     * data space
     */
    private var dataSpace = 0f

    /**
     * maxVisibleDataCount
     */
    var maxVisibleDataCount = 180

    /**
     * minVisibleDataCount
     */
    var minVisibleDataCount = 10

    /**
     * crossPoint
     */
    var crossPoint = PointF(0f, -1f)

    /**
     * currentTipDataPos
     */
    var currentTipDataPos = 0

    /**
     * isLoading
     */
    var isLoading = false

    /**
     * addData single data
     * @param kLineModel KLineModel
     * @param pos Int
     */
    fun addData(kLineModel: KLineModel, pos: Int) {
        if (pos > -1) {
            if (pos >= this.dataList.size) {
                this.dataList.add(kLineModel)
            } else {
                this.dataList[pos] = kLineModel
            }
        }
    }

    /**
     * addData list of data
     * @param list MutableList<KLineModel>
     * @param pos Int
     */
    fun addData(list: MutableList<KLineModel>, pos: Int) {
        if (this.dataList.size == 0) {
            this.dataList.addAll(list)
            moveToLast()
        } else {
            dataList.addAll(pos, list)
            visibleDataMinPos += list.size
        }
    }

    /**
     * move To Last
     */
    private fun moveToLast() {
        this.visibleDataMinPos = if (this.dataList.size > this.visibleDataCount) {
            this.dataList.size - this.visibleDataCount
        } else {
            0
        }
        this.currentTipDataPos = this.dataList.size - 1
        if (this.currentTipDataPos < 0) {
            this.currentTipDataPos = 0
        }
    }

    /**
     * space of each candle
     */
    fun space(yAxisLabelWidth: Int, yAxisPositionIsRight: Boolean, yAxisLabelPositionIsInside: Boolean, yAxisLabelOffset: Int) {
        this.dataSpace = when {
            yAxisPositionIsRight && yAxisLabelPositionIsInside && this.isShowingLastData() -> {
                val chartWidth = this.viewPortHandler.contentWidth() - yAxisLabelWidth - yAxisLabelOffset
                chartWidth / this.visibleDataCount
            }
            else -> this.viewPortHandler.contentWidth() / this.visibleDataCount
        }
    }

    /**
     * calculate current data index
     * @param x Float
     */
    fun calcCurrentDataIndex(x: Float) {
        val range = ceil((x.toDouble() - this.viewPortHandler.contentLeft()) / this.dataSpace).toInt()
        this.currentTipDataPos = min(this.visibleDataMinPos + range - 1, this.dataList.size - 1)
        if (this.currentTipDataPos < 0) {
            this.currentTipDataPos = 0
        }
    }

    /**
     * calculate zoom
     * @param scaleX Float
     * @param touchRange Int
     * @param touchStartMinPos Int
     * @return Boolean
     */
    fun calcZoom(scaleX: Float, touchRange: Int, touchStartMinPos: Int): Boolean {
        // is zooming out
        val isZoomingOut = scaleX < 1
        if (isZoomingOut) {

            if (this.visibleDataCount >= this.maxVisibleDataCount) {
                // cannot zoom out anymore
                return false
            }
        } else {
            if (this.visibleDataCount <= this.minVisibleDataCount) {
                // cannot zoom in anymore
                return false
            }
        }
        // calculate range after zoom
        val visibleDataCountAfterZoom = (touchRange / scaleX).toInt()
        this.visibleDataCount = min(max(visibleDataCountAfterZoom, this.minVisibleDataCount), this.maxVisibleDataCount)
        val minPos = touchStartMinPos + touchRange - this.visibleDataCount
        when {
            minPos + this.visibleDataCount > this.dataList.size -> this.visibleDataMinPos = 0
            minPos < 0 -> this.visibleDataMinPos = 0
            else -> this.visibleDataMinPos = minPos
        }
        return true
    }

    /**
     * calculate Drag
     * @param moveDist Float
     * @param touchMovePoint PointF
     * @param eventX Float
     * @return Boolean
     */
    fun calcDrag(
        moveDist: Float,
        touchMovePoint: PointF,
        eventX: Float,
        noMore: Boolean,
        loadMoreListener: KLineChartView.LoadMoreListener?
    ): Boolean {
        val dataSize = this.dataList.size
        when {
            moveDist < 0 - this.dataSpace / 2 -> {

                if (this.visibleDataMinPos + this.visibleDataCount == dataSize || dataSize < this.visibleDataCount) {
                    return false
                }

                touchMovePoint.x = eventX

                var moveRange = abs(moveDist / this.dataSpace).toInt()
                if (moveRange == 0) {
                    moveRange = 1
                }

                this.visibleDataMinPos += moveRange
                if (this.visibleDataMinPos > dataSize - this.visibleDataCount) {
                    this.visibleDataMinPos = dataSize - this.visibleDataCount
                }

                return true
            }

            moveDist > this.dataSpace / 2 -> {
                if (this.visibleDataMinPos == 0 || dataSize < this.visibleDataCount) {
                    return false
                }

                touchMovePoint.x = eventX

                var moveRange = abs(moveDist / this.dataSpace).toInt()
                if (moveRange == 0) {
                    moveRange = 1
                }

                this.visibleDataMinPos -= moveRange
                if (this.visibleDataMinPos < 0) {
                    this.visibleDataMinPos = 0
                }

                if (this.visibleDataMinPos == 0 &&
                    !noMore &&
                    !this.isLoading &&
                    loadMoreListener != null
                ) {
                    this.isLoading = true
                    loadMoreListener.loadMore()
                }
                return true
            }
        }
        return false
    }

    fun getVisibleDataMinPos(): Int = visibleDataMinPos

    fun getVisibleDataCount(): Int = visibleDataCount

    fun isShowingLastData(): Boolean = dataList.size <= visibleDataMinPos + visibleDataCount

    fun getChartDataSpace(): Float = dataSpace
}
