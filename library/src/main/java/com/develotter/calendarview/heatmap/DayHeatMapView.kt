package com.develotter.calendarview.heatmap


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.View
import java.time.LocalDate

/**
 * A custom view that displays a visual representation of heat intensity for a given date.
 *
 * The view renders a colored rectangle where the color's intensity reflects the `heatIntensity` value.
 *  A higher `heatIntensity` results in a more intense color, transitioning from blue to red.  It also includes
 * a ripple effect on click to provide user feedback.
 *
 * @property date The date associated with this heat map view.  Defaults to the current date.
 * @property heatIntensity A value between 0.0 and 1.0 (inclusive) representing the heat intensity.
 *  0.0 indicates no heat (represented by blue), and 1.0 indicates maximum heat (represented by red). Defaults to 0.0.
 *
 */
class DayHeatMapView (context: Context) : View(context) {
    var date: LocalDate = LocalDate.now()
    var heatIntensity: Float = 0f
    fun getHeatColor(): Int {
        return Color.argb(
            255,
            (255 * (1 - heatIntensity)).toInt(),
            (255 * heatIntensity).toInt(),
            0
        )
    }

    init {
        isClickable = true
        setRipple()


    }
    fun addHeatIntensity(intensity: Float) {
        heatIntensity = intensity
        setRipple()
        invalidate()
    }
    fun setRipple(){
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorControlHighlight, typedValue, true)
        foreground = RippleDrawable(ColorStateList.valueOf(getHeatColor()), null, null)
    }
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint().apply {
            color = getHeatColor()
        }
        val margin = 5f
        canvas.drawRect(margin, margin, width.toFloat() - margin, height.toFloat() - margin, paint)
    }
}