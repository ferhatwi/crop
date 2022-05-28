package io.github.ferhatwi.crop.util

import android.content.res.Resources
import android.graphics.Canvas
import android.util.DisplayMetrics
import android.util.Log
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "BitmapLoadUtils"

object BitmapLoadUtils {

    fun calculateMaxBitmapSize(): Int {
        val displayMetrics: DisplayMetrics by lazy { Resources.getSystem().displayMetrics }
        val size = displayMetrics.run { widthPixels to heightPixels }

        val width = size.first
        val height = size.second

        // Twice the device screen diagonal as default
        var maxBitmapSize =
            sqrt(width.toDouble().pow(2) + height.toDouble().pow(2)).toInt()

        // Check for max texture size via Canvas
        val canvas = Canvas()
        val maxCanvasSize = min(canvas.maximumBitmapWidth, canvas.maximumBitmapHeight)
        if (maxCanvasSize > 0) {
            maxBitmapSize = min(maxBitmapSize, maxCanvasSize)
        }

        // Check for max texture size via GL
        val maxTextureSize = EglUtils.maxTextureSize
        if (maxTextureSize > 0) {
            maxBitmapSize = min(maxBitmapSize, maxTextureSize)
        }
        Log.d(TAG, "maxBitmapSize: $maxBitmapSize")
        return maxBitmapSize
    }

}

