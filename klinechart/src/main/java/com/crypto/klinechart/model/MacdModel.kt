package com.crypto.klinechart.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MacdModel(
    val diff: Double?,
    val dea: Double?,
    val macd: Double?
) : Parcelable
