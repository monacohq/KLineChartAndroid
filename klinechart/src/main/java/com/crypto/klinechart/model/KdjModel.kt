package com.crypto.klinechart.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class KdjModel(
    val k: Double?,
    val d: Double?,
    val j: Double?
) : Parcelable
