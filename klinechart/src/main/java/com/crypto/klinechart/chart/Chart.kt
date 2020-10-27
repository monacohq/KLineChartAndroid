package com.crypto.klinechart.chart

import android.graphics.Canvas
import android.graphics.Paint

internal abstract class Chart {

    /**
     * chart height
     */
    var height = -1f

    /**
     * chart content offsetTop
     */
    var offsetTop = 0f

    /**
     * paint
     */
    var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    abstract fun draw(canvas: Canvas)

    /**
     * set chart height and offsetTop
     * @param height Float
     */
    open fun setChartDimens(height: Float, offsetTop: Float) {
        this.height = height
        this.offsetTop = offsetTop
    }
}
