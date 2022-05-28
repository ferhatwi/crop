package io.github.ferhatwi.crop.callback;

import android.graphics.RectF;

public interface OverlayViewChangeListener {

    void onCropRectUpdated(RectF cropRect);

}