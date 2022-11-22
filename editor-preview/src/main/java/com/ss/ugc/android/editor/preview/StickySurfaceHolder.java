package com.ss.ugc.android.editor.preview;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2020-02-26
 */
class StickySurfaceHolder implements SurfaceHolder {

    private static final String TAG = "StickySurfaceHolder";

    private final SurfaceHolder target;

    private boolean created = false;
    private int changedFormat = -1;
    private int changedWidth = -1;
    private int changedHeight = -1;

    private List<Callback> callbacks = new ArrayList<>();

    private Callback innerCallback = new Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            created = true;
            Log.d(TAG, "surfaceCreated");

            for (Callback callback : callbacks) {
                callback.surfaceCreated(holder);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            changedFormat = format;
            changedWidth = width;
            changedHeight = height;
            Log.d(TAG, "surfaceChanged : " + width + ", " + height);

            for (Callback callback : callbacks) {
                callback.surfaceChanged(holder, format, width, height);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            created = false;
            Log.d(TAG, "surfaceDestroyed");

            for (Callback callback : callbacks) {
                callback.surfaceDestroyed(holder);
            }
        }
    };

    public StickySurfaceHolder(SurfaceHolder target) {
        this.target = target;
        this.target.addCallback(innerCallback);
    }

    // MainThread
    @Override
    public void addCallback(Callback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }

        if (created) {
            callback.surfaceCreated(this);
            if (changedFormat != -1 && changedWidth != -1 && changedHeight != -1) {
                callback.surfaceChanged(this, changedFormat, changedWidth, changedHeight);
            }
        }
    }

    @Override
    public void removeCallback(Callback callback) {
        callbacks.remove(callback);
    }

    @Override
    public boolean isCreating() {
        return target.isCreating();
    }

    @Override
    public void setType(int type) {
        // target.setType(type);
    }

    @Override
    public void setFixedSize(int width, int height) {
        target.setFixedSize(width, height);
    }

    @Override
    public void setSizeFromLayout() {
        target.setSizeFromLayout();
    }

    @Override
    public void setFormat(int format) {
        target.setFormat(format);
    }

    @Override
    public void setKeepScreenOn(boolean screenOn) {
        target.setKeepScreenOn(screenOn);
    }

    @Override
    public Canvas lockCanvas() {
        return target.lockCanvas();
    }

    @Override
    public Canvas lockCanvas(Rect dirty) {
        return target.lockCanvas(dirty);
    }

    @Override
    public void unlockCanvasAndPost(Canvas canvas) {
        target.unlockCanvasAndPost(canvas);
    }

    @Override
    public Rect getSurfaceFrame() {
        return target.getSurfaceFrame();
    }

    @Override
    public Surface getSurface() {
        return target.getSurface();
    }
}
