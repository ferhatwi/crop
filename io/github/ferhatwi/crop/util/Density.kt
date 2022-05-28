package io.github.ferhatwi.crop.util

import android.view.View
import kotlin.math.roundToInt

internal fun View.toDp(int: Int) : Int = toDp(int.toFloat()).roundToInt()

fun View.toDp(float: Float) : Float = resources.displayMetrics.density*float