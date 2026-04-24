package com.topspeed.audio.input

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.util.Log

/**
 * Điều khiển cử chỉ cho game racing
 * Hỗ trợ: single tap, double tap, swipe, long press, multi-touch
 */
class GestureController(
    private val gestureListener: GestureListener
) {
    companion object {
        private const val TAG = "GestureController"
        private const val SWIPE_THRESHOLD = 100f
        private const val SWIPE_VELOCITY_THRESHOLD = 100f
        private const val TAP_TOLERANCE = 50f
        private const val DOUBLE_TAP_TIMEOUT = 300L
        private const val LONG_PRESS_TIMEOUT = 500L
    }

    // Gesture detector
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector

    // Touch state tracking
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var touchStartTime = 0L
    private var pointerCount = 0
    private var isSwiping = false
    private var swipeStartX = 0f
    private var swipeStartY = 0f

    // Multi-touch tracking
    private val activePointers = mutableMapOf<Int, Pair<Float, Float>>()

    // Gesture callbacks
    interface GestureListener {
        fun onSingleTap(x: Float, y: Float)
        fun onDoubleTap(x: Float, y: Float)
        fun onSwipeDown(x: Float, y: Float)
        fun onSwipeUp(x: Float, y: Float)
        fun onSwipeLeft(x: Float, y: Float)
        fun onSwipeRight(x: Float, y: Float)
        fun onThreeFingerSwipeUp()
        fun onTwoFingerTap()
        fun onLongPress(x: Float, y: Float)
    }

    init {
        gestureDetector = GestureDetector(null, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                Log.d(TAG, "onSingleTapUp at (${e.x}, ${e.y})")
                gestureListener.onSingleTap(e.x, e.y)
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                Log.d(TAG, "onDoubleTap at (${e.x}, ${e.y})")
                gestureListener.onDoubleTap(e.x, e.y)
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                Log.d(TAG, "onLongPress at (${e.x}, ${e.y})")
                gestureListener.onLongPress(e.x, e.y)
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (isSwiping) {
                    val deltaX = e2.x - swipeStartX
                    val deltaY = e2.y - swipeStartY

                    Log.d(TAG, "onFling: deltaX=$deltaX, deltaY=$deltaY, velocityX=$velocityX, velocityY=$velocityY")

                    // Xác định hướng swipe dựa trên khoảng cách
                    if (kotlin.math.abs(deltaX) > kotlin.math.abs(deltaY)) {
                        // Horizontal swipe
                        if (deltaX > SWIPE_THRESHOLD) {
                            gestureListener.onSwipeRight(swipeStartX, swipeStartY)
                        } else if (deltaX < -SWIPE_THRESHOLD) {
                            gestureListener.onSwipeLeft(swipeStartX, swipeStartY)
                        }
                    } else {
                        // Vertical swipe
                        if (deltaY > SWIPE_THRESHOLD) {
                            gestureListener.onSwipeDown(swipeStartX, swipeStartY)
                        } else if (deltaY < -SWIPE_THRESHOLD) {
                            gestureListener.onSwipeUp(swipeStartX, swipeStartY)
                        }
                    }

                    isSwiping = false
                    return true
                }
                return false
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                // Track scroll for swipe detection
                if (!isSwiping && e1 != null) {
                    isSwiping = true
                    swipeStartX = e1.x
                    swipeStartY = e1.y
                }
                return false
            }
        })

        scaleGestureDetector = ScaleGestureDetector(null, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                // Pinch zoom - có thể dùng cho zoom camera nếu cần
                return true
            }
        })
    }

    /**
     * Xử lý touch event từ View
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                pointerCount = 1
                touchStartTime = System.currentTimeMillis()
                lastTouchX = event.x
                lastTouchY = event.y
                activePointers.clear()
                activePointers[pointerId] = Pair(event.x, event.y)
                Log.d(TAG, "ACTION_DOWN: pointerCount=$pointerCount")
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                pointerCount++
                activePointers[pointerId] = Pair(event.getX(pointerIndex), event.getY(pointerIndex))
                Log.d(TAG, "ACTION_POINTER_DOWN: pointerCount=$pointerCount")

                // Detect 2-finger tap
                if (pointerCount == 2) {
                    // Check if both fingers are tapping (not moving much)
                    Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (pointerCount == 2 && !isSwiping) {
                            Log.d(TAG, "Two finger tap detected")
                            gestureListener.onTwoFingerTap()
                        }
                    }, DOUBLE_TAP_TIMEOUT)
                }

                // Detect 3-finger swipe up
                if (pointerCount == 3) {
                    swipeStartX = event.getX(0)
                    swipeStartY = event.getY(0)
                    isSwiping = true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                // Track finger movement for swipe detection
                if (pointerCount >= 3) {
                    val currentX = event.getX(0)
                    val currentY = event.getY(0)
                    val deltaY = swipeStartY - currentY
                    val deltaX = kotlin.math.abs(currentX - swipeStartX)

                    // 3-finger swipe up detection
                    if (deltaY > SWIPE_THRESHOLD && deltaX < SWIPE_THRESHOLD) {
                        Log.d(TAG, "Three finger swipe up detected")
                        gestureListener.onThreeFingerSwipeUp()
                        isSwiping = false
                        pointerCount = 0
                    }
                }

                // Update active pointers
                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)
                    activePointers[id] = Pair(event.getX(i), event.getY(i))
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                pointerCount--
                activePointers.remove(pointerId)
                Log.d(TAG, "ACTION_UP: pointerCount=$pointerCount")

                if (action == MotionEvent.ACTION_UP) {
                    // Single finger release - check for tap vs swipe
                    val touchDuration = System.currentTimeMillis() - touchStartTime
                    if (touchDuration < LONG_PRESS_TIMEOUT && !isSwiping) {
                        // It was a tap (already handled by onSingleTapUp)
                    }
                    isSwiping = false
                }
            }
        }

        // Let gesture detector process the event
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)

        return true
    }

    /**
     * Enable accessibility mode với larger touch targets
     */
    fun enableAccessibilityMode() {
        // Có thể adjust sensitivity trong accessibility mode
        Log.d(TAG, "Accessibility mode enabled")
    }

    /**
     * Disable accessibility mode
     */
    fun disableAccessibilityMode() {
        Log.d(TAG, "Accessibility mode disabled")
    }
}
