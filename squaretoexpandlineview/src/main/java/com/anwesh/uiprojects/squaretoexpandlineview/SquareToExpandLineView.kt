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
    val sci : Float = sc.divideScale(i, lines / 2)
    save()
    translate(size * (1f - 2 * i) * sci, y)
    drawLine(0f, 0f, size / 2, 0f, paint)
    restore()
}

fun Canvas.drawDoubleExpand(i : Int, sc : Float, size : Float, paint : Paint) {
    for (j in 0..(lines / 2 - 1)) {
        drawExpandLine(j, sc.divideScale(j, lines / 2), -size / 2 + size * i, size, paint)
    }
}

fun Canvas.drawSquareExpandLine(sc : Float, size : Float, paint : Paint) {
    for (j in 0..(lines / 2 - 1)) {
        drawDoubleExpand(j, sc, size, paint)
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

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}