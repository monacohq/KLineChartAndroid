package com.crypto.klinechart.internal

import android.graphics.PointF
import android.graphics.RectF

internal class ViewPortHandler {

    /**
     * content Rect
     */
    val contentRect = RectF()

    fun setDimens(left: Float, top: Float, right: Float, bottom: Float) {
        this.contentRect.set(left, top, right, bottom)
    }

    fun contentTop() = this.contentRect.top

    fun contentLeft() = this.contentRect.left

    fun contentRight() = this.contentRect.right

    fun contentBottom() = this.contentRect.bottom

    fun contentWidth() = this.contentRect.width()

    fun contentHeight() = this.contentRect.height()

    fun getContentCenter() = PointF(this.contentRect.centerX(), this.contentRect.centerY())
}
