package com.crypto.klinechart.app

import androidx.annotation.Nullable
import java.math.BigDecimal
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class BigDecimalAdapter {

    @ToJson
    fun toJson(@Nullable value: BigDecimal? = BigDecimal.ZERO) = value?.toString()

    @FromJson
    fun fromJson(@Nullable value: String?): BigDecimal? =
        value?.let { BigDecimal("$value") }
}