package com.crypto.klinechart.app.model

import com.crypto.klinechart.app.BigDecimalAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

open class ApiResponse {
    var id: Long? = null
    var method: String? = null
    var code: Int? = null
    var message: String? = null

    open val isValid: Boolean get() = code == 0

    override fun toString(): String {
        val moshi = Moshi.Builder()
            .add(BigDecimalAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
        return moshi.adapter(Any::class.java).toJson(this)
    }
}
