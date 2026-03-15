package com.assistantia.overlay

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class TextAccessibilityService : AccessibilityService() {

    companion object {
        var instance: TextAccessibilityService? = null
    }

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun getTextInRegion(x: Int, y: Int, w: Int, h: Int): String {
        val root = rootInActiveWindow ?: return ""
        val texts = mutableListOf<String>()
        collectTexts(root, x, y, x + w, y + h, texts)
        root.recycle()
        return texts.joinToString(" ")
    }

    private fun collectTexts(
        node: AccessibilityNodeInfo,
        left: Int, top: Int, right: Int, bottom: Int,
        texts: MutableList<String>
    ) {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val overlaps = bounds.left < right && bounds.right > left &&
                       bounds.top < bottom && bounds.bottom > top

        if (overlaps) {
            val text = node.text?.toString()
            if (!text.isNullOrBlank()) texts.add(text)
            val desc = node.contentDescription?.toString()
            if (!desc.isNullOrBlank() && desc != text) texts.add(desc)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectTexts(child, left, top, right, bottom, texts)
            child.recycle()
        }
    }
}
