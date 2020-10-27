package com.crypto.klinechart.internal.utils

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
fun Number?.formatDecimal(decimal: Int = 8): String = this?.run {
    String.format("%.${decimal}f", this)
} ?: "--"
