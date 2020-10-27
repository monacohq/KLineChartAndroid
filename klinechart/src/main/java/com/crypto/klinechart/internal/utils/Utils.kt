package com.crypto.klinechart.internal.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.util.DisplayMetrics

internal object Utils {
    private lateinit var metrics: DisplayMetrics

    private lateinit var resources: Resources

    private val calcTextSizeRect = Rect()

    /**
     * init
     * @param context Context
     */
    fun init(context: Context) {
        resources = context.resources
        metrics = resources.displayMetrics
    }

    /**
     * convert Dp To Pixel
     * @param dp Float
     * @return Float
     */
    fun convertDpToPixel(dp: Float): Float {
        return dp * metrics.density
    }

    /**
     * get text width
     * @param paint Paint
     * @param demoText String
     * @return Int
     */
    fun getTextWidth(paint: Paint, demoText: String): Int {
        return paint.measureText(demoText).toInt()
    }

    /**
     * get text height
     * @param paint Paint
     * @param demoText String
     * @return Int
     */
    fun getTextHeight(paint: Paint, demoText: String): Int = getTextRect(
        paint,
        demoText
    ).height()

    /**
     * get text size
     * @param paint Paint
     * @param demoText String
     * @return Rect
     */
    fun getTextSize(paint: Paint, demoText: String): Rect =
        getTextRect(paint, demoText)

    /**
     * get text rect
     * @param paint Paint
     * @param demoText String
     * @return Rect
     */
    private fun getTextRect(paint: Paint, demoText: String): Rect {
        val r = calcTextSizeRect
        r.set(0, 0, 0, 0)
        paint.getTextBounds(demoText, 0, demoText.length, r)
        return r
    }

    /**
     * get Resource String
     * @param id
     * @return
     */
    fun getResourceString(id: Int): String {
        return resources.getString(id)
    }
}
