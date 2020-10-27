package com.crypto.klinechart.internal

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Build
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.AnimationUtils
import com.crypto.klinechart.KLineChartView
import com.crypto.klinechart.internal.utils.Utils
import kotlin.math.abs
import kotlin.math.sqrt

internal class TouchEvent(
    private val chart: KLineChartView,
    private val dataProvider: DataProvider,
    private val viewPortHandler: ViewPortHandler
) : View.OnTouchListener {
    private companion object {
        /**
         * TOUCH_NO
         */
        const val TOUCH_NO = 0

        /**
         * TOUCH_DRAG
         */
        const val TOUCH_DRAG = 1

        /**
         * TOUCH_ZOOM
         */
        const val TOUCH_ZOOM = 2

        /**
         * TOUCH_POST_ZOOM
         */
        const val TOUCH_POST_ZOOM = 3

        /**
         * TOUCH_CROSS
         */
        const val TOUCH_CROSS = 4

        /**
         * TOUCH_CROSS_CANCEL
         */
        const val TOUCH_CROSS_CANCEL = 5

        /**
         * ZOOM_MIN_DIST
         */
        const val ZOOM_MIN_DIST = 10f

        /**
         * CROSS_EVENT_MIN_RADIUS
         */
        const val CROSS_EVENT_MIN_RADIUS = 30f
    }

    /**
     * touch mode
     */
    private var touchMode = TOUCH_NO

    /**
     * touch start point
     */
    private var touchStartPoint = PointF()

    /**
     * touch move point
     */
    private var touchMovePoint = PointF()

    /**
     * touch crosshairs point
     */
    private var touchCrosshairsPoint = PointF()

    /**
     * distance between 2 touched points
     */
    private var savedDist = 1f

    /**
     *  horizontal distance between 2 touched points
     */
    private var savedXDist = 1f

    /**
     * min scale pointer distance
     */
    private val minScalePointerDistance = Utils.convertDpToPixel(3.5f)

    /**
     * drag trigger distance
     */
    private val dragTriggerDist = Utils.convertDpToPixel(3f)

    /**
     * number of bar when touching
     */
    private var touchRange = 120

    /**
     * touch start data visible min position
     */
    private var touchStartDataVisibleMinPos = 0

    /**
     * velocity tracker
     */
    private var velocityTracker: VelocityTracker? = null

    /**
     * deceleration velocity x
     */
    private var decelerationVelocityX = 0f

    /**
     * deceleration current x
     */
    private var decelerationCurrentX = 0f

    /**
     *  deceleration last time
     */
    private var decelerationLastTime = 0L

    private val runnable = Runnable {
        if (this.touchMode == TOUCH_NO || this.touchMode == TOUCH_CROSS_CANCEL) {
            this.touchMode = TOUCH_CROSS
            this.touchCrosshairsPoint.set(this.touchStartPoint.x, this.touchStartPoint.y)
            this.dataProvider.calcCurrentDataIndex(this.touchCrosshairsPoint.x)
            this.dataProvider.crossPoint.y = this.touchCrosshairsPoint.y
            this.chart.invalidate()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (this.dataProvider.dataList.size == 0) {
            return false
        }
        if (this.chart.decelerationEnable) {
            if (this.velocityTracker == null) {
                this.velocityTracker = VelocityTracker.obtain()
            }
            velocityTracker?.addMovement(event)
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                this.touchStartPoint.set(event.x, event.y)
                this.touchMovePoint.set(event.rawX, event.rawY)
                if (!checkEventAvailability()) {
                    return false
                }

                this.decelerationVelocityX = 0f
                if (this.touchMode == TOUCH_CROSS) {
                    val crossRadius = distance(event.x, this.touchCrosshairsPoint.x, event.y, this.touchCrosshairsPoint.y)
                    if (crossRadius < CROSS_EVENT_MIN_RADIUS) {
                        return performCross(event)
                    } else {
                        this.touchMode = TOUCH_CROSS_CANCEL
                        this.dataProvider.crossPoint.y = -1f
                        this.chart.invalidate()
                    }
                } else {
                    this.touchMode = TOUCH_NO
                }

                this.chart.parent?.requestDisallowInterceptTouchEvent(true)
                this.chart.removeCallbacks(this.runnable)
                this.chart.postDelayed(this.runnable, 200)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {

                if (!checkEventAvailability()) {
                    return false
                }
                if (event.pointerCount >= 2) {
                    if (this.touchMode != TOUCH_CROSS) {
                        this.chart.parent?.requestDisallowInterceptTouchEvent(true)
                        this.savedDist = spacing(event)
                        this.savedXDist = getXDist(event)
                        if (this.savedDist > ZOOM_MIN_DIST) {
                            this.touchMode = TOUCH_ZOOM
                        }

                        this.touchRange = this.dataProvider.getVisibleDataCount()

                        this.touchStartDataVisibleMinPos = this.dataProvider.getVisibleDataMinPos()
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {

                if (!checkEventAvailability()) {
                    return false
                }
                if (abs(event.rawY - this.touchMovePoint.y) > 50 &&
                    abs(event.rawX - this.touchMovePoint.x) < 150 &&
                    (this.touchMode == TOUCH_NO || this.touchMode == TOUCH_CROSS_CANCEL)
                ) {
                    this.chart.parent?.requestDisallowInterceptTouchEvent(false)
                }

                when (this.touchMode) {
                    TOUCH_ZOOM -> {
                        this.chart.parent?.requestDisallowInterceptTouchEvent(true)
                        return performZoom(event)
                    }
                    TOUCH_DRAG -> {
                        this.chart.parent?.requestDisallowInterceptTouchEvent(true)
                        return performDrag(event)
                    }
                    TOUCH_CROSS -> {
                        this.chart.parent?.requestDisallowInterceptTouchEvent(true)
                        return performCross(event)
                    }
                    TOUCH_CROSS_CANCEL -> {
                        this.chart.removeCallbacks(this.runnable)
                    }
                    TOUCH_NO -> {
                        val distance = abs(distance(event.x, this.touchStartPoint.x, event.y, this.touchStartPoint.y))
                        if (distance > this.dragTriggerDist) {
                            val distanceX = abs(event.x - this.touchStartPoint.x)
                            val distanceY = abs(event.y - this.touchStartPoint.y)
                            if (distanceY <= distanceX) {
                                this.dataProvider.crossPoint.y = -1f
                                this.touchMode = TOUCH_DRAG
                                this.chart.invalidate()
                            }
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {

                if (!checkEventAvailability()) {
                    return false
                }

                this.chart.removeCallbacks(this.runnable)
                if (abs(this.touchStartPoint.x - event.x) < 30 && abs(this.touchStartPoint.y - event.y) < 30) {
                    if (this.touchMode == TOUCH_NO) {

                        this.chart.parent?.requestDisallowInterceptTouchEvent(true)
                        this.touchMode = TOUCH_CROSS
                        return performCross(event)
                    }
                }

                if (this.touchMode == TOUCH_DRAG) {
                    velocityTracker?.let {
                        val pointerId = event.getPointerId(0)
                        it.computeCurrentVelocity(1000, 8000f)
                        val velocityX = it.getXVelocity(pointerId)
                        if (abs(velocityX) > 50) {

                            this.decelerationLastTime = AnimationUtils.currentAnimationTimeMillis()

                            this.decelerationCurrentX = event.x

                            this.decelerationVelocityX = velocityX

                            postInvalidateOnAnimation()
                        }
                    }
                }
                recycleVelocityTracker()

                if (this.touchMode != TOUCH_CROSS) {

                    this.touchMode = TOUCH_NO
                    this.dataProvider.crossPoint.y = -1f
                    this.chart.parent?.requestDisallowInterceptTouchEvent(false)

                    this.chart.invalidate()
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (!checkEventAvailability()) {
                    return false
                }
                velocityTrackerPointerUpCleanUpIfNecessary(event)
                if (this.touchMode == TOUCH_CROSS) {
                    return performCross(event)
                } else {
                    this.touchMode = TOUCH_POST_ZOOM
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                recycleVelocityTracker()
            }
        }
        return true
    }

    /**
     * perform drag even
     *
     * @param event
     * @return
     */
    private fun performDrag(event: MotionEvent): Boolean {
        val moveDist = event.x - this.touchMovePoint.x
        val isConsume = this.dataProvider.calcDrag(moveDist, this.touchMovePoint, event.x, this.chart.noMore, this.chart.loadMoreListener)
        if (isConsume) {
            this.chart.invalidate()
        }
        return isConsume
    }

    /**
     * perform zoom event
     * @param event MotionEvent
     */
    private fun performZoom(event: MotionEvent): Boolean {
        if (event.pointerCount >= 2) {
            val totalDist = spacing(event)
            if (totalDist > this.minScalePointerDistance) {
                val xDist = getXDist(event)
                // xDist scale
                val scaleX = xDist / this.savedXDist
                val isConsume = this.dataProvider.calcZoom(scaleX, this.touchRange, this.touchStartDataVisibleMinPos)
                if (isConsume) {
                    this.chart.invalidate()
                }
                return isConsume
            }
        }
        return true
    }

    /**
     * perform crosshairs moving event
     * @param event MotionEvent
     */
    private fun performCross(event: MotionEvent): Boolean {
        this.touchCrosshairsPoint.set(event.x, event.y)
        this.dataProvider.calcCurrentDataIndex(this.touchCrosshairsPoint.x)
        this.dataProvider.crossPoint.y = this.touchCrosshairsPoint.y
        this.chart.invalidate()
        return true
    }

    /**
     * perform Scroll
     */
    fun computeScroll() {
        if (this.decelerationVelocityX == 0f) { return }

        val currentTime = AnimationUtils.currentAnimationTimeMillis()
        this.decelerationVelocityX *= 0.9f

        val timeInterval = (currentTime - this.decelerationLastTime) / 1000f
        val distanceX = this.decelerationVelocityX * timeInterval

        this.decelerationCurrentX += distanceX

        val event = MotionEvent.obtain(
            currentTime, currentTime, MotionEvent.ACTION_MOVE,
            this.decelerationCurrentX, 0f, 0
        )

        performDrag(event)

        event.recycle()

        this.decelerationLastTime = currentTime

        if (abs(this.decelerationVelocityX) >= 1) {
            postInvalidateOnAnimation()
        } else {
            this.decelerationVelocityX = 0f
        }
    }

    /**
     * recycle velocity tracker
     */
    private fun recycleVelocityTracker() {
        this.velocityTracker?.apply {
            recycle()
            velocityTracker = null
        }
    }

    /**
     * whether velocity tracker need clean up or not
     * @param ev MotionEvent
     */
    private fun velocityTrackerPointerUpCleanUpIfNecessary(ev: MotionEvent) {
        this.velocityTracker?.let {
            val upIndex = ev.actionIndex
            val id1 = ev.getPointerId(upIndex)
            it.computeCurrentVelocity(1000, 8000f)
            val x1 = it.getXVelocity(id1)
            val y1 = it.getYVelocity(id1)

            for (i in 0 until ev.pointerCount) {
                if (i != upIndex) {
                    val id2 = ev.getPointerId(i)
                    val x = x1 * it.getXVelocity(id2)
                    val y = y1 * it.getYVelocity(id2)

                    val dot = x + y
                    if (dot < 0) {
                        it.clear()
                        break
                    }
                }
            }
        }
    }

    private fun postInvalidateOnAnimation() {
        if (Build.VERSION.SDK_INT >= 16) {
            this.chart.postInvalidateOnAnimation()
        } else {
            this.chart.postInvalidateDelayed(10)
        }
    }

    /**
     * check event availability
     */
    private fun checkEventAvailability(): Boolean {
        return !(
            this.touchStartPoint.x < this.viewPortHandler.contentLeft() ||
                this.touchStartPoint.x > this.viewPortHandler.contentRight() ||
                this.touchStartPoint.y < this.viewPortHandler.contentTop() ||
                this.touchStartPoint.y > this.viewPortHandler.contentBottom()
            )
    }

    /**
     * get moving distance
     *
     * @param event
     * @return
     */
    private fun spacing(event: MotionEvent): Float {
        if (event.pointerCount < 2) {
            return 0f
        }
        val x = abs(event.getX(event.getPointerId(0)) - event.getX(event.getPointerId(1))).toDouble()
        val y = abs(event.getY(event.getPointerId(0)) - event.getY(event.getPointerId(1))).toDouble()
        return sqrt(x * x + y * y).toFloat()
    }

    /**
     * get x distance between 2 points
     * @param event MotionEvent
     * @return Float
     */
    private fun getXDist(event: MotionEvent): Float {
        return abs(event.getX(0) - event.getX(1))
    }

    /**
     * calculate distance between 2 points
     * @param eventX Float
     * @param startX Float
     * @param eventY Float
     * @param startY Float
     * @return Float
     */
    private fun distance(eventX: Float, startX: Float, eventY: Float, startY: Float): Float {
        val dx = eventX - startX
        val dy = eventY - startY
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}
