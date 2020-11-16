package com.crypto.klinechart.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.crypto.klinechart.app.model.CandleStickResponse
import com.crypto.klinechart.app.model.MarketData
import com.crypto.klinechart.model.KLineModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class MainActivityViewModel(val activity: MainActivity): ViewModel() {
    val moshi = Moshi.Builder()
        .add(BigDecimalAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()


    val marketDataList: MutableLiveData<List<MarketData>?> = MutableLiveData(null)
    val kLineModelList = marketDataList.map {
        it?.mapNotNull { toKLineModel(it) }
    }

    fun toKLineModel(marketData: MarketData): KLineModel? = when (marketData.startTime != null && marketData.open != null && marketData.high != null && marketData.low != null && marketData.close != null && marketData.volume != null) {
        true -> KLineModel(
            openPrice = marketData.open.toDouble(),
            closePrice = marketData.close.toDouble(),
            highPrice = marketData.high.toDouble(),
            lowPrice = marketData.low.toDouble(),
            volume = marketData.volume.toDouble(),
            timestamp = marketData.startTime
        )
        else -> null
    }

    fun getMockCandleStickData() {
        val candleStickResponse = moshi.adapter(CandleStickResponse::class.java).fromJson(getMockCandleStickDataJsonStr())
        marketDataList.value = candleStickResponse?.result?.marketDataList
    }

    fun getJsonFromPath(classLoader: ClassLoader, path: String): String {
        val uri = classLoader.getResource(path)
        val file = File(uri.path)
        return String(file.readBytes())
    }

    fun getMockCandleStickDataJsonStr(): String {
        return """
            {
              "code": 0,
              "method": "subscribe",
              "result": {
                "channel":"candlestick",
                "instrument_name": "CRO_USDT",
                "subscription": "candlestick.15m.CRO_USDT",
                "depth": 300,
                "interval": "15m",
                "data":[
                  {"c":"0.0698","h":"0.0702","l":"0.0698","o":"0.07","t":1605302100000,"v":"381600.118"},
                  {"c":"0.0699","h":"0.07","l":"0.0697","o":"0.0698","t":1605303000000,"v":"159792.959"},
                  {"c":"0.0699","h":"0.0701","l":"0.0698","o":"0.0699","t":1605303900000,"v":"99703.223"},
                  {"c":"0.0701","h":"0.0703","l":"0.0698","o":"0.0699","t":1605304800000,"v":"201497.132"},
                  {"c":"0.0702","h":"0.0704","l":"0.0699","o":"0.0701","t":1605305700000,"v":"194621.517"},
                  {"c":"0.0702","h":"0.0704","l":"0.0699","o":"0.0701","t":1605306600000,"v":"108398.349"},
                  {"c":"0.0703","h":"0.0705","l":"0.0701","o":"0.0702","t":1605307500000,"v":"182262.22"},
                  {"c":"0.07","h":"0.0705","l":"0.0693","o":"0.0703","t":1605308400000,"v":"881756.158"},
                  {"c":"0.0698","h":"0.0702","l":"0.0693","o":"0.0702","t":1605309300000,"v":"554854.124"},
                  {"c":"0.0705","h":"0.0706","l":"0.0698","o":"0.0698","t":1605310200000,"v":"350762.801"},
                  {"c":"0.0703","h":"0.0707","l":"0.0702","o":"0.0706","t":1605311100000,"v":"458310.502"},
                  {"c":"0.07","h":"0.0703","l":"0.07","o":"0.0702","t":1605312000000,"v":"807251.911"},
                  {"c":"0.0699","h":"0.0702","l":"0.0696","o":"0.07","t":1605312900000,"v":"753963.946"},
                  {"c":"0.0699","h":"0.07","l":"0.0697","o":"0.0697","t":1605313800000,"v":"132815.422"},
                  {"c":"0.0701","h":"0.0702","l":"0.0698","o":"0.0699","t":1605314700000,"v":"133457.432"},
                  {"c":"0.0698","h":"0.0702","l":"0.0697","o":"0.0701","t":1605315600000,"v":"215341.202"},
                  {"c":"0.0699","h":"0.07","l":"0.0695","o":"0.0697","t":1605316500000,"v":"559193.369"},
                  {"c":"0.0698","h":"0.0702","l":"0.0696","o":"0.0699","t":1605317400000,"v":"528668.443"},
                  {"c":"0.0697","h":"0.07","l":"0.0695","o":"0.0697","t":1605318300000,"v":"331300.098"},
                  {"c":"0.0696","h":"0.07","l":"0.0694","o":"0.0697","t":1605319200000,"v":"373713.487"},
                  {"c":"0.0697","h":"0.0698","l":"0.0694","o":"0.0696","t":1605320100000,"v":"410864.871"},
                  {"c":"0.0699","h":"0.0702","l":"0.0696","o":"0.0697","t":1605321000000,"v":"387705.789"},
                  {"c":"0.0697","h":"0.0701","l":"0.0695","o":"0.07","t":1605321900000,"v":"838235.74"},
                  {"c":"0.0697","h":"0.0698","l":"0.0695","o":"0.0697","t":1605322800000,"v":"330337.2"},
                  {"c":"0.0698","h":"0.0699","l":"0.0696","o":"0.0696","t":1605323700000,"v":"112698.149"},
                  {"c":"0.0695","h":"0.0699","l":"0.0694","o":"0.0698","t":1605324600000,"v":"627877.332"},
                  {"c":"0.0697","h":"0.0697","l":"0.0694","o":"0.0696","t":1605325500000,"v":"306464.334"},
                  {"c":"0.0694","h":"0.0697","l":"0.0693","o":"0.0697","t":1605326400000,"v":"288539.127"},
                  {"c":"0.0696","h":"0.0698","l":"0.068","o":"0.0694","t":1605327300000,"v":"2076143.983"},
                  {"c":"0.0688","h":"0.0699","l":"0.068","o":"0.0696","t":1605328200000,"v":"1964445.937"},
                  {"c":"0.0696","h":"0.0698","l":"0.0683","o":"0.0691","t":1605329100000,"v":"2645996.722"},
                  {"c":"0.0692","h":"0.0697","l":"0.069","o":"0.0695","t":1605330000000,"v":"536082.695"},
                  {"c":"0.0691","h":"0.0694","l":"0.0688","o":"0.0692","t":1605330900000,"v":"291557.941"},
                  {"c":"0.0695","h":"0.0699","l":"0.069","o":"0.0691","t":1605331800000,"v":"486924.565"},
                  {"c":"0.0697","h":"0.0697","l":"0.0694","o":"0.0695","t":1605332700000,"v":"113835.213"},
                  {"c":"0.0697","h":"0.07","l":"0.0696","o":"0.0696","t":1605333600000,"v":"78980.509"},
                  {"c":"0.0698","h":"0.0699","l":"0.0697","o":"0.0699","t":1605334500000,"v":"35446.944"},
                  {"c":"0.0696","h":"0.0699","l":"0.0695","o":"0.0698","t":1605335400000,"v":"95644.252"},
                  {"c":"0.0694","h":"0.0698","l":"0.0694","o":"0.0696","t":1605336300000,"v":"185319.971"},
                  {"c":"0.0692","h":"0.0698","l":"0.069","o":"0.0695","t":1605337200000,"v":"300813.081"},
                  {"c":"0.0693","h":"0.0694","l":"0.0684","o":"0.0692","t":1605338100000,"v":"943148.248"},
                  {"c":"0.069","h":"0.0693","l":"0.0685","o":"0.0691","t":1605339000000,"v":"265661.581"},
                  {"c":"0.069","h":"0.0692","l":"0.0684","o":"0.069","t":1605339900000,"v":"514300.001"},
                  {"c":"0.0691","h":"0.0695","l":"0.0689","o":"0.0691","t":1605340800000,"v":"120647.859"},
                  {"c":"0.0683","h":"0.0691","l":"0.0676","o":"0.069","t":1605341700000,"v":"795268.513"}
                ]
              }
            }   
        """
    }
}