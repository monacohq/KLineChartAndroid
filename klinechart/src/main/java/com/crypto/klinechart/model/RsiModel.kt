package com.crypto.klinechart.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RsiModel(
    val rsi1: Double?,
    val rsi2: Double?,
    val rsi3: Double?
) : Parcelable
