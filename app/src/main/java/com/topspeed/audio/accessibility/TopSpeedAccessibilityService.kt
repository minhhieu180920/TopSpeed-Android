package com.topspeed.audio.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Accessibility Service cho TopSpeed Audio Racing
 * Cung cấp hỗ trợ TalkBack và cử chỉ đặc biệt cho người khiếm thị
 */
class TopSpeedAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "TopSpeedAccessibility"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.i(TAG, "Accessibility service connected")

        // Configure service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.DEFAULT
            notificationTimeout = 100
        }

        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        // Handle accessibility events
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                // View received focus - announce it
                val text = event.text?.firstOrNull()?.toString()
                text?.let {
                    Log.d(TAG, "Focused: $text")
                }
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                // View clicked
                Log.d(TAG, "View clicked")
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    /**
     * Perform gesture at screen location
     */
    fun performGestureAt(x: Float, y: Float, duration: Long = 200L) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x, y)
                lineTo(x, y)
            }

            val stroke = GestureDescription.StrokeDescription(path, 0, duration)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            dispatchGesture(gesture, null, null)
        }
    }

    /**
     * Find node by text content
     */
    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null

        // Search for node with matching text
        return findNodeByTextRecursive(rootNode, text)
    }

    private fun findNodeByTextRecursive(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString() == text) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByTextRecursive(child, text)
            if (result != null) {
                return result
            }
        }

        return null
    }

    /**
     * Get bounds of a node
     */
    fun getNodeBounds(node: AccessibilityNodeInfo): Rect {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        return bounds
    }

    /**
     * Perform click on node
     */
    fun performClick(node: AccessibilityNodeInfo): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    /**
     * Perform focus on node
     */
    fun performFocus(node: AccessibilityNodeInfo): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
    }

    /**
     * Announce text via TalkBack
     */
    fun announce(text: String) {
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        event.text.add(text)
        sendAccessibilityEvent(event)
    }

    override fun onDestroy() {
        Log.i(TAG, "Accessibility service destroyed")
        super.onDestroy()
    }
}
