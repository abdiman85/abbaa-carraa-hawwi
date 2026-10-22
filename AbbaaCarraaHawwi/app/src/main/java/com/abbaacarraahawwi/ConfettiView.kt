package com.abbaacarraahawwi

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private class Piece(
        var x: Float, var y: Float,
        val vx: Float, val vy: Float,
        val size: Float, val color: Int,
        var rot: Float, val vr: Float
    )

    private val pieces = mutableListOf<Piece>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animator: ValueAnimator? = null
    private val colors = intArrayOf(
        Color.parseColor("#DA121A"), Color.parseColor("#FCDD09"),
        Color.parseColor("#078930"), Color.parseColor("#2196F3"),
        Color.parseColor("#FF9800"), Color.parseColor("#E91E63")
    )

    fun burst() {
        val d = resources.displayMetrics.density
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        animator?.cancel()
        pieces.clear()
        repeat(110) {
            pieces.add(
                Piece(
                    x = Random.nextFloat() * w,
                    y = -Random.nextFloat() * h * 0.4f,
                    vx = (Random.nextFloat() - 0.5f) * 3f * d,
                    vy = (2.5f + Random.nextFloat() * 4.5f) * d,
                    size = (4f + Random.nextFloat() * 5f) * d,
                    color = colors[Random.nextInt(colors.size)],
                    rot = Random.nextFloat() * 360f,
                    vr = (Random.nextFloat() - 0.5f) * 14f
                )
            )
        }

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 4000
            addUpdateListener {
                for (p in pieces) {
                    p.x += p.vx
                    p.y += p.vy
                    p.rot += p.vr
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    pieces.clear()
                    invalidate()
                }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (p in pieces) {
            paint.color = p.color
            canvas.save()
            canvas.rotate(p.rot, p.x, p.y)
            canvas.drawRect(p.x - p.size, p.y - p.size / 2f, p.x + p.size, p.y + p.size / 2f, paint)
            canvas.restore()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
