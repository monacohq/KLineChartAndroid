package com.crypto.klinechart.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BollModel(
    val up: Double?,
    val mid: Double?,
    val dn: Double?
) : Parcelable
