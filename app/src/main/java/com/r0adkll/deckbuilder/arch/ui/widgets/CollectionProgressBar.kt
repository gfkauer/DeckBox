package com.r0adkll.deckbuilder.arch.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.ftinc.kit.extensions.color
import com.ftinc.kit.extensions.dp
import com.r0adkll.deckbuilder.R

class CollectionProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var trackColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var progressColor: Int = Color.BLUE
        set(value) {
            field = value
            invalidate()
        }

    var borderColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var borderWidth: Float = dp(1)
        set(value) {
            field = value
            paint.strokeWidth = value
            invalidate()
        }

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderBounds = RectF()

    init {
        paint.strokeWidth = dp(1)

        val a = context.obtainStyledAttributes(attrs, R.styleable.CollectionProgressBar, defStyleAttr, 0)
        trackColor = a.getColor(R.styleable.CollectionProgressBar_trackColor, color(R.color.white50))
        progressColor = a.getColor(R.styleable.CollectionProgressBar_progressColor, color(R.color.secondaryColor))
        borderColor = a.getColor(R.styleable.CollectionProgressBar_borderColor, Color.WHITE)
        borderWidth = a.getDimension(R.styleable.CollectionProgressBar_borderWidth, dp(1))
        a.recycle()

        if (isInEditMode) {
            @Suppress("MagicNumber")
            progress = .33f
        }
    }

    override fun onDraw(canvas: Canvas) {
        val radius = (height - (paddingTop + paddingBottom)) / 2f

        // Render Track
        paint.color = trackColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(getRenderBounds(), radius, radius, paint)

        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(getRenderBounds(), radius, radius, paint)

        if (progress > 0f) {
            // Render Progress
            paint.color = progressColor
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(getRenderBounds(progress), radius, radius, paint)

            paint.color = borderColor
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(getRenderBounds(progress), radius, radius, paint)
        }
    }

    private fun getRenderBounds(progress: Float = 1f): RectF {
        val left = paddingStart.toFloat()
        val top = paddingTop.toFloat()
        val bottom = (height - (paddingTop + paddingBottom)).toFloat()
        val right = (width - (paddingStart + paddingEnd)) * progress
        renderBounds.set(left, top, right, bottom)
        return renderBounds
    }
}
