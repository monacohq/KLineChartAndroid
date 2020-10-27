package com.crypto.klinechart.component

import android.graphics.Color
import android.graphics.Paint
import com.crypto.klinechart.internal.utils.Utils

abstract class Axis {

    /**
     * whether display axis border line or not
     */
    var displayAxisLine = true

    /**
     * axis border line color
     */
    var axisLineColor = Color.parseColor("#707070")

    /**
     * axis border line size
     */
    var axisLineSize = 1f

    /**
     * whether display axis tick text or not
     */
    var displayTickText = true

    /**
     * tick text color
     */
    var tickTextColor = Color.parseColor("#707070")

    /**
     * tick text size
     */
    var tickTextSize = Utils.convertDpToPixel(10f)

    /**
     * whether display axis tick line or not
     */
    var displayTickLine = true

    /**
     * tick line size
     */
    var tickLineSize = Utils.convertDpToPixel(3f)

    /**
     * whether display grid line
     */
    var displayGridLine = false

    /**
     * grid line size
     */
    var gridLineSize = 1f

    /**
     * grid line color
     */
    var gridLineColor = Color.parseColor("#B8B8B8")

    /**
     * grid line style
     */
    var gridLineStyle =
        Component.LineStyle.DASH

    /**
     * grid line dash values
     */
    var separatorLineDashValues = floatArrayOf(15f, 10f)

    /**
     * text margin space
     */
    var textMarginSpace = Utils.convertDpToPixel(3f)

    /**
     * paint
     */
    val paint = Paint()
}
