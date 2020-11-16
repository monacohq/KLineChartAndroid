package com.crypto.klinechart.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.crypto.klinechart.model.KLineModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mMainActivityViewModel by lazy {
        MainActivityViewModel(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMainActivityViewModel.getMockCandleStickData()

        mMainActivityViewModel.kLineModelList.observe(
            this,
            Observer { kLineModelList ->
                kLineModelList?.let {
                    when {
                        kLineModelList.size == 1 -> {
                            val dataList = kline.getDataList()

                            updateLastPriceMarkLineColor(it.first())

                            when (dataList.isNotEmpty() && dataList.last().timestamp == it.first().timestamp) {
                                true -> kline.addData(it.first(), pos = dataList.size - 1)
                                false -> kline.addData(it.first())
                            }
                        }
                        kLineModelList.size > 1 -> {
                            kline.clearDataList()
                            kline.addData(it.toMutableList())
                        }
                        else -> {
                            // Do nothing
                        }
                    }
                }
            }
        )
    }

    private fun updateLastPriceMarkLineColor(kLineModel: KLineModel) {
        val colorResId = when {
            kLineModel.closePrice >= kLineModel.openPrice -> R.color.priceUpBg
            kLineModel.closePrice < kLineModel.openPrice -> R.color.priceDownBg
            else -> R.color.priceNoChangeBg
        }

        val color = ContextCompat.getColor(this, colorResId)
        kline.candle.lastPriceMarkLineColor = color
        kline.invalidate()
    }
}