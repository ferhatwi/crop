package io.github.ferhatwi.crop.model

import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class Frame(
    var visible: Boolean = true,
    var edgePaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.White.copy(alpha = 0.5f).toArgb()
        strokeWidth = 1f
    },
    var cornerPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.White.copy(alpha = 0.5f).toArgb()
        strokeWidth = 3f
    }
)
