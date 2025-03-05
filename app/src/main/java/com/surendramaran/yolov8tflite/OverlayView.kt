package com.surendramaran.yolov8tflite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.util.LinkedList
import kotlin.math.max
import android.graphics.Bitmap

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results = listOf<BoundingBox>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var bounds = Rect()

    init {
        initPaints()
    }

    fun clear() {
        results = listOf()
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, isAboveBox: Boolean) {
        textBackgroundPaint.getTextBounds(text, 0, text.length, bounds)
        val textWidth = bounds.width()
        val textHeight = bounds.height()

        if (isAboveBox) {
            // Рисуем текст сверху
            canvas.drawRect(
                x,
                y - boxPaint.strokeWidth,
                x + textWidth + BOUNDING_RECT_TEXT_PADDING,
                y - textHeight - BOUNDING_RECT_TEXT_PADDING - boxPaint.strokeWidth,
                textBackgroundPaint
            )
            canvas.drawText(text, x, y - boxPaint.strokeWidth, textPaint)
        } else {
            // Рисуем текст снизу
            canvas.drawRect(
                x,
                y + BOUNDING_RECT_TEXT_PADDING,
                x + textWidth + BOUNDING_RECT_TEXT_PADDING,
                y + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )
            canvas.drawText(text, x, y + BOUNDING_RECT_TEXT_PADDING + textHeight, textPaint)
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        results.forEach {
            val left = it.x1 * width
            val top = it.y1 * height
            val right = it.x2 * width
            val bottom = it.y2 * height

            it.plate?.let { plateBitmap ->
                // Рисуем изображение слева от рамки (или где требуется)
                val plateWidth = (right - left) / 2 // Пример: половина ширины рамки
                val plateHeight = (bottom - top) / 2 // Пример: половина высоты рамки

                // Масштабируем и рисуем
                val scaledBitmap = Bitmap.createScaledBitmap(
                    plateBitmap,
                    plateWidth.toInt(),
                    plateHeight.toInt(),
                    false
                )
                canvas.drawBitmap(scaledBitmap, left - plateWidth, top - plateHeight, null)
            }

            // Рисуем рамку вокруг обнаруженного объекта
            canvas.drawRect(left, top, right, bottom, boxPaint)

            // Рисуем основное имя класса (clsName) над рамкой
            drawText(canvas, it.clsName, left, top, isAboveBox = true)

            // Рисуем текст с номером (plateText) под рамкой
            drawText(canvas, it.plateText, left, bottom, isAboveBox = false)
        }
    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        results = boundingBoxes
        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}