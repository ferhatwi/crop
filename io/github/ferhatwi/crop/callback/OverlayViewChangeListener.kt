package io.github.ferhatwi.crop.callback

import android.graphics.RectF

interface OverlayViewChangeListener {
    fun onCropRectUpdated(cropRect: RectF)
}