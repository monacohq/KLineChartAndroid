package com.crypto.klinechart.component

import android.graphics.Color
import com.crypto.klinechart.internal.utils.Utils

class ChartBorderLine {
    /**
     * whether display chart border line or not
     */
    var displayChartBorderLine = false

    /**
     * chart border line size
     */
    var lineSize = Utils.convertDpToPixel(1f)

    /**
     * chart border line color
     */
    var lineColor = Color.parseColor("#707070")
}
