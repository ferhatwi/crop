package io.github.ferhatwi.crop.util

object CubicEasing {
    fun easeOut(time: Float, start: Float, end: Float, duration: Float): Float {
        var timeX = time
        return end * (((timeX / duration - 1.0f).also {
            timeX = it
        }) * timeX * timeX + 1.0f) + start
    }

    fun easeIn(time: Float, start: Float, end: Float, duration: Float): Float {
        var timeX = time
        return end * duration.let { timeX /= it; timeX } * timeX * timeX + start
    }

    fun easeInOut(time: Float, start: Float, end: Float, duration: Float): Float {
        var timeX = time
        return if ((duration / 2.0f).let { timeX /= it; timeX } < 1.0f) end / 2.0f * timeX * timeX * timeX + start else end / 2.0f * (2.0f.let { timeX -= it; timeX } * timeX * timeX + 2.0f) + start
    }


}