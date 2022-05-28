package io.github.ferhatwi.crop.view

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.ferhatwi.crop.callback.OverlayViewChangeListener
import io.github.ferhatwi.crop.model.Frame
import io.github.ferhatwi.crop.model.Grid
import io.github.ferhatwi.crop.util.RectUtils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class OverlayView (context: Context) : View(context) {
    val cropViewRect = RectF()
    private val mTempRect = RectF()
    private var mThisWidth = 0
    private var mThisHeight = 0
    private lateinit var mCropGridCorners: FloatArray
    private lateinit var mCropGridCenter: FloatArray

    var grid = Grid()
    set(value) {
        field = value
        if (field.row != value.row) mGridPoints = null
        if (field.column != value.column) mGridPoints = null
    }

    var frame = Frame()

    private var mTargetAspectRatio = 0f
    private var mGridPoints: FloatArray? = null

    private var mCircleDimmedLayer = false
    private var mDimmedColor = Color.Transparent.toArgb()
    private val mCircularPath = Path()
    private val mDimmedStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)



    @FreestyleMode
    private var mFreestyleCropMode = DEFAULT_FREESTYLE_CROP_MODE
    private var mPreviousTouchX = -1f
    private var mPreviousTouchY = -1f
    private var mCurrentTouchCornerIndex = -1
    private var mTouchPointThreshold = 0
    private var mCropRectMinSize = 0
    private var mCropRectCornerTouchAreaLineLength = 0
    var overlayViewChangeListener: OverlayViewChangeListener? = null
    private var mShouldSetupCropBounds = false
    /***
     * Please use the new method [getFreestyleCropMode][.getFreestyleCropMode] method as we have more than 1 freestyle crop mode.
     */
    /***
     * Please use the new method [setFreestyleCropMode][.setFreestyleCropMode] method as we have more than 1 freestyle crop mode.
     */
    @get:Deprecated("")
    @set:Deprecated("")
    var isFreestyleCropEnabled: Boolean
        get() = mFreestyleCropMode == FREESTYLE_CROP_MODE_ENABLE
        set(freestyleCropEnabled) {
            mFreestyleCropMode =
                if (freestyleCropEnabled) FREESTYLE_CROP_MODE_ENABLE else FREESTYLE_CROP_MODE_DISABLE
        }

    @get:FreestyleMode
    var freestyleCropMode: Int
        get() = mFreestyleCropMode
        set(mFreestyleCropMode) {
            this.mFreestyleCropMode = mFreestyleCropMode
            postInvalidate()
        }

    /**
     * Setter for [.mCircleDimmedLayer] variable.
     *
     * @param circleDimmedLayer - set it to true if you want dimmed layer to be an circle
     */
    fun setCircleDimmedLayer(circleDimmedLayer: Boolean) {
        mCircleDimmedLayer = circleDimmedLayer
    }



    /**
     * Setter for [.mDimmedColor] variable.
     *
     * @param dimmedColor - desired color of dimmed area around the crop bounds
     */
    fun setDimmedColor(@ColorInt dimmedColor: Int) {
        mDimmedColor = dimmedColor
    }



    /**
     * This method sets aspect ratio for crop bounds.
     *
     * @param targetAspectRatio - aspect ratio for image crop (e.g. 1.77(7) for 16:9)
     */
    fun setTargetAspectRatio(targetAspectRatio: Float) {
        mTargetAspectRatio = targetAspectRatio
        if (mThisWidth > 0) {
            setupCropBounds()
            postInvalidate()
        } else {
            mShouldSetupCropBounds = true
        }
    }

    /**
     * This method setups crop bounds rectangles for given aspect ratio and view size.
     * [.mCropViewRect] is used to draw crop bounds - uses padding.
     */
    fun setupCropBounds() {
        val height = (mThisWidth / mTargetAspectRatio).toInt()
        if (height > mThisHeight) {
            val width = (mThisHeight * mTargetAspectRatio).toInt()
            val halfDiff = (mThisWidth - width) / 2
            cropViewRect[(paddingLeft + halfDiff).toFloat(), paddingTop.toFloat(), (
                    paddingLeft + width + halfDiff).toFloat()] =
                (paddingTop + mThisHeight).toFloat()
        } else {
            val halfDiff = (mThisHeight - height) / 2
            cropViewRect[paddingLeft.toFloat(), (paddingTop + halfDiff).toFloat(), (
                    paddingLeft + mThisWidth).toFloat()] =
                (paddingTop + height + halfDiff).toFloat()
        }
        if (overlayViewChangeListener != null) {
            overlayViewChangeListener!!.onCropRectUpdated(cropViewRect)
        }
        updateGridPoints()
    }

    private fun updateGridPoints() {
        mCropGridCorners = RectUtils.getCornersFromRect(
            cropViewRect
        )
        mCropGridCenter = RectUtils.getCenterFromRect(cropViewRect)
        mGridPoints = null
        mCircularPath.reset()
        mCircularPath.addCircle(
            cropViewRect.centerX(), cropViewRect.centerY(),
            min(cropViewRect.width(), cropViewRect.height()) / 2f, Path.Direction.CW
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {

            mThisWidth = width - paddingRight - paddingLeft
            mThisHeight = height - paddingBottom - paddingTop
            if (mShouldSetupCropBounds) {
                mShouldSetupCropBounds = false
                setTargetAspectRatio(mTargetAspectRatio)
            }
        }
    }

    /**
     * Along with image there are dimmed layer, crop bounds and crop guidelines that must be drawn.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDimmedLayer(canvas)
        drawCropGrid(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (cropViewRect.isEmpty || mFreestyleCropMode == FREESTYLE_CROP_MODE_DISABLE) {
            return false
        }
        var x = event.x
        var y = event.y
        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN) {
            mCurrentTouchCornerIndex = getCurrentTouchIndex(x, y)
            val shouldHandle = mCurrentTouchCornerIndex != -1
            if (!shouldHandle) {
                mPreviousTouchX = -1f
                mPreviousTouchY = -1f
            } else if (mPreviousTouchX < 0) {
                mPreviousTouchX = x
                mPreviousTouchY = y
            }
            return shouldHandle
        }
        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_MOVE) {
            if (event.pointerCount == 1 && mCurrentTouchCornerIndex != -1) {
                x = min(max(x, paddingLeft.toFloat()), (width - paddingRight).toFloat())
                y = min(max(y, paddingTop.toFloat()), (height - paddingBottom).toFloat())
                updateCropViewRect(x, y)
                mPreviousTouchX = x
                mPreviousTouchY = y
                return true
            }
        }
        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
            mPreviousTouchX = -1f
            mPreviousTouchY = -1f
            mCurrentTouchCornerIndex = -1
            if (overlayViewChangeListener != null) {
                overlayViewChangeListener!!.onCropRectUpdated(cropViewRect)
            }
        }
        return false
    }

    /**
     * * The order of the corners is:
     * 0------->1
     * ^        |
     * |   4    |
     * |        v
     * 3<-------2
     */
    private fun updateCropViewRect(touchX: Float, touchY: Float) {
        mTempRect.set(cropViewRect)
        when (mCurrentTouchCornerIndex) {
            0 -> mTempRect[touchX, touchY, cropViewRect.right] = cropViewRect.bottom
            1 -> mTempRect[cropViewRect.left, touchY, touchX] = cropViewRect.bottom
            2 -> mTempRect[cropViewRect.left, cropViewRect.top, touchX] = touchY
            3 -> mTempRect[touchX, cropViewRect.top, cropViewRect.right] = touchY
            4 -> {
                mTempRect.offset(touchX - mPreviousTouchX, touchY - mPreviousTouchY)
                if (mTempRect.left > left && mTempRect.top > top && mTempRect.right < right && mTempRect.bottom < bottom) {
                    cropViewRect.set(mTempRect)
                    updateGridPoints()
                    postInvalidate()
                }
                return
            }
        }
        val changeHeight = mTempRect.height() >= mCropRectMinSize
        val changeWidth = mTempRect.width() >= mCropRectMinSize
        cropViewRect[if (changeWidth) mTempRect.left else cropViewRect.left, if (changeHeight) mTempRect.top else cropViewRect.top, if (changeWidth) mTempRect.right else cropViewRect.right] =
            if (changeHeight) mTempRect.bottom else cropViewRect.bottom
        if (changeHeight || changeWidth) {
            updateGridPoints()
            postInvalidate()
        }
    }

    /**
     * * The order of the corners in the float array is:
     * 0------->1
     * ^        |
     * |   4    |
     * |        v
     * 3<-------2
     *
     * @return - index of corner that is being dragged
     */
    private fun getCurrentTouchIndex(touchX: Float, touchY: Float): Int {
        var closestPointIndex = -1
        var closestPointDistance = mTouchPointThreshold.toDouble()
        var i = 0
        while (i < 8) {
            val distanceToCorner = sqrt(
                (touchX - mCropGridCorners[i]).toDouble().pow(2.0)
                        + (touchY - mCropGridCorners[i + 1]).toDouble().pow(2.0)
            )
            if (distanceToCorner < closestPointDistance) {
                closestPointDistance = distanceToCorner
                closestPointIndex = i / 2
            }
            i += 2
        }
        return if (mFreestyleCropMode == FREESTYLE_CROP_MODE_ENABLE && closestPointIndex < 0 && cropViewRect.contains(
                touchX,
                touchY
            )
        ) {
            4
        } else closestPointIndex

//        for (int i = 0; i <= 8; i += 2) {
//
//            double distanceToCorner;
//            if (i < 8) { // corners
//                distanceToCorner = sqrt(pow(touchX - mCropGridCorners[i], 2)
//                        + pow(touchY - mCropGridCorners[i + 1], 2));
//            } else { // center
//                distanceToCorner = sqrt(pow(touchX - mCropGridCenter[0], 2)
//                        + pow(touchY - mCropGridCenter[1], 2));
//            }
//            if (distanceToCorner < closestPointDistance) {
//                closestPointDistance = distanceToCorner;
//                closestPointIndex = i / 2;
//            }
//        }
    }

    /**
     * This method draws dimmed area around the crop bounds.
     *
     * @param canvas - valid canvas object
     */
    protected fun drawDimmedLayer(canvas: Canvas) {
        canvas.save()
        if (mCircleDimmedLayer) {
            canvas.clipPath(mCircularPath, Region.Op.DIFFERENCE)
        } else {
            canvas.clipRect(cropViewRect, Region.Op.DIFFERENCE)
        }
        canvas.drawColor(mDimmedColor)
        canvas.restore()
        if (mCircleDimmedLayer) { // Draw 1px stroke to fix antialias
            canvas.drawCircle(
                cropViewRect.centerX(), cropViewRect.centerY(),
                min(cropViewRect.width(), cropViewRect.height()) / 2f, mDimmedStrokePaint
            )
        }
    }

    /**
     * This method draws crop bounds (empty rectangle)
     * and crop guidelines (vertical and horizontal lines inside the crop bounds) if needed.
     *
     * @param canvas - valid canvas object
     */
    protected fun drawCropGrid(canvas: Canvas) {
        if (grid.visible) {
            if (mGridPoints == null && !cropViewRect.isEmpty) {
                mGridPoints = FloatArray(grid.row * 4 + grid.column * 4)
                var index = 0
                for (i in 0 until grid.row) {
                    mGridPoints!![index++] = cropViewRect.left
                    mGridPoints!![index++] =
                        cropViewRect.height() * ((i.toFloat() + 1.0f) / (grid.row + 1).toFloat()) + cropViewRect.top
                    mGridPoints!![index++] = cropViewRect.right
                    mGridPoints!![index++] =
                        cropViewRect.height() * ((i.toFloat() + 1.0f) / (grid.row + 1).toFloat()) + cropViewRect.top
                }
                for (i in 0 until grid.column) {
                    mGridPoints!![index++] =
                        cropViewRect.width() * ((i.toFloat() + 1.0f) / (grid.column + 1).toFloat()) + cropViewRect.left
                    mGridPoints!![index++] = cropViewRect.top
                    mGridPoints!![index++] =
                        cropViewRect.width() * ((i.toFloat() + 1.0f) / (grid.column + 1).toFloat()) + cropViewRect.left
                    mGridPoints!![index++] = cropViewRect.bottom
                }
            }
            if (mGridPoints != null) {
                canvas.drawLines(mGridPoints!!, grid.paint)
            }
        }
        if (frame.visible) {
            canvas.drawRect(cropViewRect, frame.edgePaint)
        }
        if (mFreestyleCropMode != FREESTYLE_CROP_MODE_DISABLE) {
            canvas.save()
            mTempRect.set(cropViewRect)
            mTempRect.inset(
                mCropRectCornerTouchAreaLineLength.toFloat(),
                -mCropRectCornerTouchAreaLineLength.toFloat()
            )
            canvas.clipRect(mTempRect, Region.Op.DIFFERENCE)
            mTempRect.set(cropViewRect)
            mTempRect.inset(
                -mCropRectCornerTouchAreaLineLength.toFloat(),
                mCropRectCornerTouchAreaLineLength.toFloat()
            )
            canvas.clipRect(mTempRect, Region.Op.DIFFERENCE)
            canvas.drawRect(cropViewRect, frame.cornerPaint)
            canvas.restore()
        }
    }


    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(
        FREESTYLE_CROP_MODE_DISABLE,
        FREESTYLE_CROP_MODE_ENABLE,
        FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH
    )
    annotation class FreestyleMode
    companion object {
        const val FREESTYLE_CROP_MODE_DISABLE = 0
        const val FREESTYLE_CROP_MODE_ENABLE = 1
        const val FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH = 2
        const val DEFAULT_SHOW_CROP_FRAME = true
        const val DEFAULT_SHOW_CROP_GRID = true
        const val DEFAULT_CIRCLE_DIMMED_LAYER = false
        const val DEFAULT_FREESTYLE_CROP_MODE = FREESTYLE_CROP_MODE_DISABLE
        const val DEFAULT_CROP_GRID_ROW_COUNT = 2
        const val DEFAULT_CROP_GRID_COLUMN_COUNT = 2
    }

    init {
        mTouchPointThreshold = 30
        mCropRectMinSize = 100
        mCropRectCornerTouchAreaLineLength = 10
    }

}
