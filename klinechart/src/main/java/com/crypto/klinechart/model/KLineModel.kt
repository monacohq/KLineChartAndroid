package com.crypto.klinechart.model

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class KLineModel @JvmOverloads constructor(
    /**
     * open price
     */
    var openPrice: Double = 0.0,

    /**
     * lowest price
     */
    var lowPrice: Double = 0.0,

    /**
     * highest price
     */
    var highPrice: Double = 0.0,

    /**
     * close price
     */
    var closePrice: Double = 0.0,

    /**
     * volume
     */
    var volume: Double = 0.0,

    /**
     * time stamp
     */
    var timestamp: Long = System.currentTimeMillis(),

    /**
     * turnover
     */
    var turnover: Double = 0.0,

    /**
     * average price
     */
    var averagePrice: Double = 0.0,

    /**
     * Boll Model
     */
    var boll: BollModel? = null,

    /**
     * Kdj Model
     */
    var kdj: KdjModel? = null,

    /**
     * Macd Model
     */
    var macd: MacdModel? = null,

    /**
     * Ma Model
     */
    var ma: MaModel? = null,

    /**
     * Rsi Model
     */
    var rsi: RsiModel? = null
) : Parcelable {
    /**
     * extension Data
     */
    @IgnoredOnParcel
    var extensionData: Any? = null

    /**
     * custom Indicator
     */
    @IgnoredOnParcel
    var customIndicator: Any? = null

    override fun toString(): String {
        return "open: $openPrice, low: $lowPrice, high: $highPrice, close: $closePrice"
    }
}
