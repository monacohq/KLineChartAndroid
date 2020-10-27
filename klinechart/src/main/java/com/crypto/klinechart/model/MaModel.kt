package com.crypto.klinechart.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MaModel(
    /**
     * 5 days ma
     */
    val ma5: Double,

    /**
     * 10 days ma
     */
    val ma10: Double,

    /**
     * 20 days ma
     */
    val ma20: Double,

    /**
     * 60 days ma
     */
    val ma60: Double
) : Parcelable
