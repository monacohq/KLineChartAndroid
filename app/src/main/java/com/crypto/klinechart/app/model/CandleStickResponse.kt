package com.crypto.klinechart.app.model

import com.squareup.moshi.Json
import java.math.BigDecimal

class CandleStickResponse(val result: Result? = null) : ApiResponse()

data class Result(
    val channel: String? = null,
    @Json(name = "data")
    val marketDataList: List<MarketData>? = null,
    val depth: Int? = null,
    @Json(name = "instrument_name")
    val instrumentName: String? = null,
    val interval: String? = null,
    val subscription: String? = null
)

data class MarketData(
    @Json(name = "c")
    val close: BigDecimal? = null,
    @Json(name = "h")
    val high: BigDecimal? = null,
    @Json(name = "l")
    val low: BigDecimal? = null,
    @Json(name = "o")
    val open: BigDecimal? = null,
    @Json(name = "t")
    val startTime: Long? = null, // start time of the stick, in epochtime
    @Json(name = "v")
    val volume: BigDecimal? = null,
    @Json(name = "i")
    val instrumentName: String? = null
)
