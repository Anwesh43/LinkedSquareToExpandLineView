package com.anwesh.uiprojects.squaretoexpandlineview

/**
 * Created by anweshmishra on 20/06/19.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val nodes : Int = 5
val lines : Int = 4
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val foreColor : Int = Color.parseColor("#01579B")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawExpandLine( i : Int, sc : Float, y : Float, size : Float, paint : Paint) {
    save()
    translate((size / 2) * (1f - 2 * i) * sc, y)
    drawLine(0f, 0f, (size / 2) * (1f - 2 * i), 0f, paint)
    restore()
}

fun Canvas.drawDoubleExpand(i : Int, sc : Float, size : Float, paint : Paint) {
    for (j in 0..(lines / 2 - 1)) {
        save()
        translate((size / 2) * (1f - 2 * j), 0f)
        drawLine(0f, 0f, 0f, -size / 2 + size * i, paint)
        restore()
        drawExpandLine(j, sc.divideScale(j, lines / 2), -size / 2 + size * i, size, paint)
    }
}

fun Canvas.drawSquareExpandLine(sc : Float, size : Float, paint : Paint) {
    for (j in 0..(lines / 2 - 1)) {
        drawDoubleExpand(j, sc.divideScale(j, lines / 2), size, paint)
    }
}

fun Canvas.drawSTENode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(w / 2, gap * (i + 1))
    rotate(90f * sc2)
    drawSquareExpandLine(sc1, size, paint)
    restore()
}

class SquareToExpandLineView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class STENode(var i : Int, val state : State = State()) {

        private var next : STENode? = null
        private var prev : STENode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = STENode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSTENode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : STENode {
            var curr : STENode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SquareToExpandLine(var i : Int) {

        private val root : STENode = STENode(0)
        private var curr : STENode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SquareToExpandLineView) {

        private val animator : Animator = Animator(view)
        private val sel : SquareToExpandLine = SquareToExpandLine(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            sel.draw(canvas, paint)
            animator.animate {
                sel.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            sel.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity: Activity) : SquareToExpandLineView {
            val view : SquareToExpandLineView = SquareToExpandLineView(activity)
            activity.setContentView(view)
            return view
        }
    }
}