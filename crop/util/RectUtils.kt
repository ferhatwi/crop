package io.github.ferhatwi.crop.util

import android.graphics.RectF

object RectUtils {
    /**
     * Gets a float array of the 2D coordinates representing a rectangles
     * corners.
     * The order of the corners in the float array is:
     * 0------->1
     * ^        |
     * |        |
     * |        v
     * 3<-------2
     *
     * @param r the rectangle to get the corners of
     * @return the float array of corners (8 floats)
     */
    fun getCornersFromRect(r: RectF): FloatArray {
        return floatArrayOf(
            r.left, r.top,
            r.right, r.top,
            r.right, r.bottom,
            r.left, r.bottom
        )
    }

    /**
     * Gets a float array of two lengths representing a rectangles width and height
     * The order of the corners in the input float array is:
     * 0------->1
     * ^        |
     * |        |
     * |        v
     * 3<-------2
     *
     * @param corners the float array of corners (8 floats)
     * @return the float array of width and height (2 floats)
     */
    fun getRectSidesFromCorners(corners: FloatArray): FloatArray {
        return floatArrayOf(
            Math.sqrt(
                Math.pow(
                    (corners[0] - corners[2]).toDouble(),
                    2.0
                ) + Math.pow((corners[1] - corners[3]).toDouble(), 2.0)
            ).toFloat(), Math.sqrt(
                Math.pow(
                    (corners[2] - corners[4]).toDouble(),
                    2.0
                ) + Math.pow((corners[3] - corners[5]).toDouble(), 2.0)
            ).toFloat()
        )
    }

    fun getCenterFromRect(r: RectF): FloatArray {
        return floatArrayOf(r.centerX(), r.centerY())
    }

    /**
     * Takes an array of 2D coordinates representing corners and returns the
     * smallest rectangle containing those coordinates.
     *
     * @param array array of 2D coordinates
     * @return smallest rectangle containing coordinates
     */
    fun trapToRect(array: FloatArray): RectF {
        val r = RectF(
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY
        )
        var i = 1
        while (i < array.size) {
            val x = Math.round(array[i - 1] * 10) / 10f
            val y = Math.round(array[i] * 10) / 10f
            r.left = Math.min(x, r.left)
            r.top = Math.min(y, r.top)
            r.right = Math.max(x, r.right)
            r.bottom = Math.max(y, r.bottom)
            i += 2
        }
        r.sort()
        return r
    }
}