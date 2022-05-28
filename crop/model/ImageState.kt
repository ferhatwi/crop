package io.github.ferhatwi.crop.model

import android.graphics.RectF

data class ImageState(
    val cropRect: RectF,
    val imageRect: RectF,
    var scale: Float,
    val angle: Float
)