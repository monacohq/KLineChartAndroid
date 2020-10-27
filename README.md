
# KLineChart
A kline UI widget for android. Support analysis indicators and style changes, as well as customize the drawing of something you want to display.

## Usage
Put a widget in the XML layout, set some attributes
```xml
<com.crypto.exchange.klinechart.KLineChartView
    android:id="@+id/kline"
    android:layout_width="match_parent"
    android:layout_height="350dp"
    android:layout_marginTop="20dp"
    app:candle_style="solid"/>
```

Add data and set some attributes in the code.

#### Kotlin
```kotlin
k_line_chart.candle.candleStyle = Candle.CandleStyle.SOLID
k_line_chart.addData(dataList)
```
#### Java
```java
k_line_chart.getCandle().setCandleStyle(Candle.CandleStyle.SOLID)
k_line_chart.addData(dataList)
```

## Download
### gradle
```groovy
implementation 'com.github.monacohq:KLineChartAndroid:1.0.0'
```
### maven
```xml
<dependency>
  <groupId>com.github.monacohq</groupId>
  <artifactId>KLineChartAndroid</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```
## Indicator
### Supported by default
<table>
    <tbody>
        <tr>
            <th>MA</th>
            <th>VOL</th>
            <th>MACD</th>
            <th>BOLL</th>
            <th>KDJ</th>
            <th>KD</th>
            <th>RSI</th>
        </tr>
        <tr>
            <th>✅</th>
            <th>✅</th>
            <th>✅</th>
            <th>✅</th>
            <th>✅</th>
            <th>✅</th>
            <th>✅</th>
        </tr>
    </tbody>
</table>
The main chart supports all technical analysis indicators, generally only set NO, MA, BOLL and SAR. NO means no display.

## Attributes
Attributes can be changed to change the appearance of the chart.
[Here is attributes detail](./ATTRIBUTE-DETAIL.md)

## Thanks
  + [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)

## License
Copyright (c) 2019 lihu

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
