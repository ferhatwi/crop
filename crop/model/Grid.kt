package io.github.ferhatwi.crop.model

import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class Grid(
    var visible: Boolean = true,
    var row: Int = 3,
    var column: Int = 3,
    var paint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.White.copy(alpha = 0.5f).toArgb()
        strokeWidth = 1f
    }
)
