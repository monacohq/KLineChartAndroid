package com.crypto.klinechart.internal.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * format time
 * @receiver Long?
 * @param pattern String
 * @return String
 */
fun Long?.formatDate(pattern: String = "yyyy-MM-dd HH:mm"): String = this?.run {
    val date = Date(this)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    formatter.format(date)
} ?: "--"

/**
 * format number dp, default dp = 8
 * @receiver Number?
 * @param decimal Int
 * @return String
 */
fun Number?.formatDecimal(decimal: Int = 8, pattern: String = "#,###.##", mode: RoundingMode = RoundingMode.HALF_UP): String = this?.run {
    DecimalFormat(pattern).apply {
        roundingMode = mode
        minimumFractionDigits = decimal
        maximumFractionDigits = decimal
    }.format(this)
} ?: "--"
