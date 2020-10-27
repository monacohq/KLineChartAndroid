package com.crypto.klinechart.internal.utils

import com.crypto.klinechart.model.BollModel
import com.crypto.klinechart.model.KLineModel
import com.crypto.klinechart.model.KdjModel
import com.crypto.klinechart.model.MaModel
import com.crypto.klinechart.model.MacdModel
import com.crypto.klinechart.model.RsiModel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal object CalcIndicatorUtils {
    /**
     * calculate Ma
     * @param dataList MutableList<KLineModel>
     * @return MutableList<KLineModel>
     */
    fun calcMa(dataList: MutableList<KLineModel>): MutableList<KLineModel> {
        var ma5Num = 0.0
        var ma10Num = 0.0
        var ma20Num = 0.0
        var ma60Num = 0.0

        var ma5: Double
        var ma10: Double
        var ma20: Double
        var ma60: Double
        return calc(dataList) { i ->
            val close = dataList[i].closePrice
            ma5Num += close
            ma10Num += close
            ma20Num += close
            ma60Num += close
            if (i < 5) {
                ma5 = ma5Num / (i + 1)
            } else {
                ma5Num -= dataList[i - 5].closePrice
                ma5 = ma5Num / 5
            }

            if (i < 10) {
                ma10 = ma10Num / (i + 1)
            } else {
                ma10Num -= dataList[i - 10].closePrice
                ma10 = ma10Num / 10
            }

            if (i < 20) {
                ma20 = ma20Num / (i + 1)
            } else {
                ma20Num -= dataList[i - 20].closePrice
                ma20 = ma20Num / 20
            }

            if (i < 60) {
                ma60 = ma60Num / (i + 1)
            } else {
                ma60Num -= dataList[i - 60].closePrice
                ma60 = ma60Num / 60
            }

            dataList[i].ma = MaModel(ma5, ma10, ma20, ma60)
        }
    }

    /**
     * calculate macd
     *
     * @param dataList MutableList<KLineModel>
     * @return MutableList<KLineModel>
     */
    fun calcMacd(dataList: MutableList<KLineModel>): MutableList<KLineModel> {
        var ema12: Double
        var ema26: Double
        var oldEma12 = 0.0
        var oldEma26 = 0.0
        var diff: Double
        var dea: Double
        var oldDea = 0.0
        var macd: Double
        return calc(dataList) { i ->
            val closePrice = dataList[i].closePrice
            if (i == 0) {
                ema12 = closePrice
                ema26 = closePrice
            } else {
                ema12 = (2 * closePrice + 11 * oldEma12) / 13f
                ema26 = (2 * closePrice + 25 * oldEma26) / 27f
            }

            diff = ema12 - ema26
            dea = (diff * 2 + oldDea * 8) / 10.0
            macd = (diff - dea) * 2
            oldEma12 = ema12
            oldEma26 = ema26
            oldDea = dea

            dataList[i].macd = MacdModel(diff, dea, macd)
        }
    }

    /**
     * calculate Boll
     *
     * 参数20
     * @param dataList MutableList<KLineModel>
     * @return MutableList<KLineModel>
     */
    fun calcBoll(dataList: MutableList<KLineModel>): MutableList<KLineModel> {
        var close20 = 0.0 // MA sum

        var ma: Double // middleBB
        var md: Double // Standard Deviation
        var up: Double // upperBB
        var dn: Double // lowerBB

        return calc(dataList) { i ->
            val closePrice = dataList[i].closePrice
            close20 += closePrice
            if (i < 20) {
                ma = close20 / (i + 1)
                md = getBollMd(dataList.subList(0, i + 1), ma)
            } else {
                close20 -= dataList[i - 20].closePrice
                ma = close20 / 20
                md = getBollMd(dataList.subList(i - 19, i + 1), ma)
            }
            up = ma + 2 * md
            dn = ma - 2 * md

            dataList[i].boll = BollModel(up, ma, dn)
        }
    }

    /**
     * calculate kdj
     * @param dataList MutableList<KLineModel>
     * @return MutableList<KLineModel>
     */
    fun calcKdj(dataList: MutableList<KLineModel>): MutableList<KLineModel> {
        var k: Double
        var d: Double
        var j: Double

        // the lowest price within n days
        var ln: Double
        // the highest price within n days
        var hn: Double

        return calc(dataList) { i ->
            // closePrice of n date
            val cn = dataList[i].closePrice

            if (i < 8) {
                ln = getLow(dataList.subList(0, i + 1))
                hn = getHigh(dataList.subList(0, i + 1))
            } else {
                ln = getLow(dataList.subList(i - 8, i + 1))
                hn = getHigh(dataList.subList(i - 8, i + 1))
            }
            val rsv = (cn - ln) / (if (hn - ln == 0.0) 1.0 else hn - ln) * 100
            // k value of n date = 2/3 x k value of n - 1 date + 1/3 x RSV value of n date
            // d value of n date = 2/3 x d value of n - 1 date + 1/3 x k value of n date
            // if k value of n - 1 date or d value of n - 1 date is null, than default value is 50
            // j value = 3 x k value of n date - 2 x d value of n date
            k = 2.0 / 3.0 * (if (i < 8) 50.0 else dataList[i - 1].kdj?.k ?: 50.0) + 1.0 / 3.0 * rsv
            d = 2.0 / 3.0 * (if (i < 8) 50.0 else dataList[i - 1].kdj?.d ?: 50.0) + 1.0 / 3.0 * k
            j = 3.0 * k - 2.0 * d

            dataList[i].kdj = KdjModel(k, d, j)
        }
    }

    /**
     * calculate Rsi
     * @param dataList MutableList<KLineModel>
     * @return MutableList<KLineModel>
     */
    fun calcRsi(dataList: MutableList<KLineModel>): MutableList<KLineModel> {

        var rsi1 = 0.0
        var rsi2 = 0.0
        var rsi3 = 0.0

        var sumCloseA1 = 0.0
        var sumCloseB1 = 0.0

        var sumCloseA2 = 0.0
        var sumCloseB2 = 0.0

        var sumCloseA3 = 0.0
        var sumCloseB3 = 0.0

        var a1: Double
        var b1: Double

        var a2: Double
        var b2: Double

        var a3: Double
        var b3: Double

        return calc(dataList) { i ->
            if (i > 0) {
                val tmp = dataList[i].closePrice - dataList[i - 1].closePrice
                if (tmp > 0) {
                    sumCloseA1 += tmp
                    sumCloseA2 += tmp
                    sumCloseA3 += tmp
                } else {
                    val absTmp = abs(tmp)
                    sumCloseB1 += absTmp
                    sumCloseB2 += absTmp
                    sumCloseB3 += absTmp
                }

                if (i < 6) {
                    a1 = sumCloseA1 / (i + 1)
                    b1 = (sumCloseA1 + sumCloseB1) / (i + 1)
                } else {
                    if (i > 6) {
                        val agoTmp = dataList[i - 6].closePrice - dataList[i - 7].closePrice
                        if (agoTmp > 0) {
                            sumCloseA1 -= agoTmp
                        } else {
                            sumCloseB1 -= abs(agoTmp)
                        }
                    }
                    a1 = sumCloseA1 / 6
                    b1 = (sumCloseA1 + sumCloseB1) / 6
                }
                rsi1 = if (b1 != 0.0) {
                    a1 / b1 * 100
                } else {
                    0.0
                }

                if (i < 12) {
                    a2 = sumCloseA2 / (i + 1)
                    b2 = (sumCloseA2 + sumCloseB2) / (i + 1)
                } else {
                    if (i > 12) {
                        val agoTmp = dataList[i - 12].closePrice - dataList[i - 13].closePrice
                        if (agoTmp > 0) {
                            sumCloseA2 -= agoTmp
                        } else {
                            sumCloseB2 -= abs(agoTmp)
                        }
                    }
                    a2 = sumCloseA2 / 12
                    b2 = (sumCloseA2 + sumCloseB2) / 12
                }
                rsi2 = if (b2 != 0.0) {
                    a2 / b2 * 100
                } else {
                    0.0
                }

                if (i < 24) {
                    a3 = sumCloseA3 / (i + 1)
                    b3 = (sumCloseA3 + sumCloseB3) / (i + 1)
                } else {
                    if (i > 24) {
                        val agoTmp = dataList[i - 24].closePrice - dataList[i - 25].closePrice
                        if (agoTmp > 0) {
                            sumCloseA3 -= agoTmp
                        } else {
                            sumCloseB3 -= abs(agoTmp)
                        }
                    }
                    a3 = sumCloseA3 / 24
                    b3 = (sumCloseA3 + sumCloseB3) / 24
                }
                rsi3 = if (b3 != 0.0) {
                    a3 / b3 * 100
                } else {
                    0.0
                }
            }
            dataList[i].rsi = RsiModel(rsi1, rsi2, rsi3)
        }
    }

    private inline fun calc(dataList: MutableList<KLineModel>, calcIndicator: (index: Int) -> Unit): MutableList<KLineModel> {
        var totalTurnover = 0.0
        var totalVolume = 0.0

        val dataSize = dataList.size
        for (i in 0 until dataSize) {
            val data = dataList[i]
            totalVolume += data.volume
            totalTurnover += data.turnover
            if (totalVolume != 0.0) {
                data.averagePrice = totalTurnover / totalVolume
            }
            calcIndicator(i)
        }
        return dataList
    }

    /**
     * calculate Standard Deviation of Boll
     * @param list MutableList<KLineModel>
     * @param ma Double
     * @return Double
     */
    private fun getBollMd(list: MutableList<KLineModel>, ma: Double): Double {
        val size = list.size
        var sum = 0.0
        for (i in 0 until size) {
            val closeMa = list[i].closePrice - ma
            sum += closeMa * closeMa
        }
        val b = sum > 0
        sum = abs(sum)
        val md = sqrt(sum / size)

        return if (b) md else -1 * md
    }

    /**
     * get highest price
     *
     * @param list
     * @return
     */
    private fun getHigh(list: MutableList<KLineModel>): Double {
        var high = 0.0
        val size = list.size
        if (size > 0) {
            high = list[0].highPrice
            for (i in 0 until size) {
                high = max(list[i].highPrice, high)
            }
        }
        return high
    }

    /**
     * get lowest price
     *
     * @param list
     * @return
     */
    private fun getLow(list: MutableList<KLineModel>): Double {
        var low = 0.0
        val size = list.size
        if (size > 0) {
            low = list[0].lowPrice
            for (i in 0 until size) {
                low = min(list[i].lowPrice, low)
            }
        }
        return low
    }

    /**
     * get highest and lowest price
     * @param list MutableList<KLineModel>
     * @return DoubleArray
     */
    private fun getHighLow(list: MutableList<KLineModel>): DoubleArray {
        var high = 0.0
        var low = 0.0
        val size = list.size
        if (size > 0) {
            high = list[0].highPrice
            low = list[0].lowPrice
            for (i in 0 until size) {
                high = max(list[i].highPrice, high)
                low = min(list[i].lowPrice, low)
            }
        }
        return doubleArrayOf(high, low)
    }
}
