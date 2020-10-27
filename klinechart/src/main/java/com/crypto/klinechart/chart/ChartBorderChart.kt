package com.crypto.klinechart.chart

import android.graphics.Canvas
import android.graphics.Paint
import com.crypto.klinechart.component.ChartBorderLine
import com.crypto.klinechart.internal.ViewPortHandler

internal class ChartBorderChart(
    private val chartBorderLine: ChartBorderLine,
    private val viewPortHandler: ViewPortHandler
) : Chart() {
    override fun draw(canvas: Canvas) {
        if (!this.chartBorderLine.displayChartBorderLine) {
            return
        }
        this.paint.apply {
            style = Paint.Style.FILL
            color = chartBorderLine.lineColor
            strokeWidth = chartBorderLine.lineSize
        }

        canvas.apply {
            drawLine(viewPortHandler.contentLeft(), viewPortHandler.contentTop(), viewPortHandler.contentRight(), viewPortHandler.contentTop(), paint)
            drawLine(viewPortHandler.contentLeft(), viewPortHandler.contentBottom(), viewPortHandler.contentRight(), viewPortHandler.contentBottom(), paint)
            drawLine(viewPortHandler.contentLeft(), viewPortHandler.contentTop(), viewPortHandler.contentLeft(), viewPortHandler.contentBottom(), paint)
            drawLine(viewPortHandler.contentRight(), viewPortHandler.contentTop(), viewPortHandler.contentRight(), viewPortHandler.contentBottom(), paint)
        }
    }
}
