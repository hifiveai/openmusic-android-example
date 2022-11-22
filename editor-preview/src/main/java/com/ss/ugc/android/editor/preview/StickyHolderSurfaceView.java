package com.ss.ugc.android.editor.preview;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @since 2020-02-26
 */
public class StickyHolderSurfaceView extends SurfaceView {

    private StickySurfaceHolder stickySurfaceHolder;

    public StickyHolderSurfaceView(Context context) {
        super(context);
        init();
    }

    public StickyHolderSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickyHolderSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public StickyHolderSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        stickySurfaceHolder = new StickySurfaceHolder(super.getHolder());
    }

    @Override
    public SurfaceHolder getHolder() {
        return stickySurfaceHolder;
    }

}
