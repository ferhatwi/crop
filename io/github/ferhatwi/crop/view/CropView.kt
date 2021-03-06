package io.github.ferhatwi.crop.view

import android.content.Context
import android.graphics.RectF
import android.widget.FrameLayout
import androidx.core.view.setPadding
import io.github.ferhatwi.crop.callback.CropBoundsChangeListener
import io.github.ferhatwi.crop.callback.OverlayViewChangeListener
import io.github.ferhatwi.crop.util.toDp

class CropView (context: Context) : FrameLayout(context) {
    var cropImageView: GestureCropImageView
        private set
    val overlayView: OverlayView
    private fun setListenersToViews() {
        cropImageView.cropBoundsChangeListener = object : CropBoundsChangeListener {
            override fun onCropAspectRatioChanged(cropRatio: Float) {
                overlayView.setTargetAspectRatio(cropRatio)
            }
        }
        overlayView.overlayViewChangeListener = object : OverlayViewChangeListener {
            override fun onCropRectUpdated(cropRect: RectF) {
                cropImageView.setCropRect(cropRect)
            }
        }
    }

    override fun shouldDelayChildPressedState() = false

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    fun resetCropImageView() {
        removeView(cropImageView)
        cropImageView = GestureCropImageView(context)
        setListenersToViews()
        cropImageView.setCropRect(overlayView.cropViewRect)
        addView(cropImageView, 0)
    }

    init {
        cropImageView = GestureCropImageView(context)
        overlayView = OverlayView(context)


        overlayView.setPadding(toDp(16))

        addView(cropImageView)
        addView(overlayView)

        setListenersToViews()
    }
}