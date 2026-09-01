/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.setupwizard.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.hypot

class RevealHoleView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val holePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    var holeCenterX = Float.NaN
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    var holeCenterY = Float.NaN
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    var holeRadius = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    @ColorInt
    var holeBackgroundColor = resolveBackgroundColor(context)
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private companion object {
        fun resolveBackgroundColor(context: Context): Int {
            val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.colorBackground))
            val color = a.getColor(0, Color.BLACK)
            a.recycle()
            return color
        }
    }

    private fun centerX() = if (holeCenterX.isNaN()) width / 2f else holeCenterX

    private fun centerY() = if (holeCenterY.isNaN()) height / 2f else holeCenterY

    fun fullRadius(): Float {
        val cx = centerX()
        val cy = centerY()
        return maxOf(
            hypot(cx, cy),
            hypot(width - cx, cy),
            hypot(cx, height - cy),
            hypot(width - cx, height - cy),
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(holeBackgroundColor)
        if (holeRadius > 0f) {
            canvas.drawCircle(centerX(), centerY(), holeRadius, holePaint)
        }
    }
}
