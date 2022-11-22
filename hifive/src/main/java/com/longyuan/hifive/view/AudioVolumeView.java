package com.longyuan.hifive.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.longyuan.hifive.manager.OnSeekChangeListener;
import com.longyuan.hifive.model.AudioVolumeInfo;
import com.ss.ugc.android.editor.picker.utils.ScreenUtils;

/**
 * #duxiaxing
 * #date: 2022/7/25
 */
public class AudioVolumeView  extends View {

    private AudioVolumeInfo audioVolumeInfo;
    private Paint barPaint;

    private float mLastXIntercept = 0f;
    private float mLastYIntercept = 0f;
    private int viewAllWidth;
    private OnSeekChangeListener listener;
    private int currentT = -1;

    public AudioVolumeView(Context context) {
        super(context);
    }

    public AudioVolumeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioVolumeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AudioVolumeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private int lineWidth;
    private int lineCount;
    private int[] lineHeights;

    private void init() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0) return;
        lineWidth = ScreenUtils.dp2px(getContext(), 1);
        lineCount = width / (lineWidth * 2);//线条多宽，间距就多宽
        int dataCount = audioVolumeInfo.mHeightsAtThisZoomLevel.length;
        if (dataCount < lineCount) {
            //数据条目 < 可绘制条目
            lineCount = dataCount;
            lineHeights = new int[lineCount];
            for (int i = 0; i < lineCount; i++) {
                int lineHeight = (int) (audioVolumeInfo.mHeightsAtThisZoomLevel[i] * height / 2);
                if (lineHeight == 0) lineHeight = 1;
                lineHeights[i] = lineHeight;
            }
        } else {
            lineHeights = new int[lineCount];
            float scale = dataCount * 1f / lineCount;
            for (int i = 0; i < lineCount; i++) {
                int dataIndex = (int) (i * scale);
                if (dataIndex > dataCount - 1) dataIndex = dataCount - 1;//非标准计算可能会越界
                int lineHeight = (int) (audioVolumeInfo.mHeightsAtThisZoomLevel[dataIndex] * height / 2);
                if (lineHeight == 0) lineHeight = 1;
                lineHeights[i] = lineHeight;
            }
        }
        Log.i("tag","view count ============>lineCount:" + lineCount + ",dataCount:" + dataCount + ",lineHeights:" + lineHeights.length);
        barPaint = new Paint();
        barPaint.setAntiAlias(false);
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setStrokeWidth(lineWidth);
        barPaint.setColor(Color.WHITE);
    }

    public void setAudioVolumeInfo(AudioVolumeInfo audioVolumeInfo) {
        this.audioVolumeInfo = audioVolumeInfo;
        currentT = -1;
        barPaint = null;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewAllWidth = MeasureSpec.getSize(widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawReallyLines(canvas);
    }

    /**
     * 画真实的线
     */
    private void drawReallyLines(Canvas canvas) {
        if (audioVolumeInfo == null) return;
        int centerY = getMeasuredHeight() / 2;

        if (barPaint == null) {
            init();
        }
        for (int i = 0; i < lineCount; i++) {
            if (i <= currentT){
                barPaint.setColor(0xffFFD600);
            }else {
                barPaint.setColor(Color.WHITE);
            }
            float x = i * lineWidth * 2;
            float topY = centerY - lineHeights[i];
            float bottomY = centerY + lineHeights[i];
            canvas.drawLine(x, topY, x, bottomY, barPaint);
        }
    }

    private boolean isTouchDown = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                isTouchDown = true;
                if (listener != null) {
                    listener.onStartTrackingTouch();
                    float refan = event.getX();
                    if (refan < 0) {
                        refan = 0;
                    } else if (refan > viewAllWidth) {
                        refan = viewAllWidth;
                    }
                    listener.onProgressChanged((int) (refan / viewAllWidth * 100), true);
                }
            case MotionEvent.ACTION_MOVE:
                currentT = (int) ((event.getX() / viewAllWidth) * lineCount);
                if (currentT > lineCount) {
                    currentT = lineCount;
                }
                invalidate();
                if (listener != null) {
                    float refan = event.getX();
                    if (refan < 0) {
                        refan = 0;
                    } else if (refan > viewAllWidth) {
                        refan = viewAllWidth;
                    }
                    listener.onProgressChanged((int) (refan / viewAllWidth * 100), true);
                }
                return true;
            case MotionEvent.ACTION_UP:
                isTouchDown = false;
                if (listener != null) {
                    float refan = event.getX();
                    if (refan < 0) {
                        refan = 0;
                    } else if (refan > viewAllWidth) {
                        refan = viewAllWidth;
                    }
                    listener.onStopTrackingTouch((int) (refan / viewAllWidth * 100));
                }
        }
        return super.onTouchEvent(event);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = x - mLastXIntercept;
                float deltaY = y - mLastYIntercept;
                if (Math.abs(deltaX) < 1 && Math.abs(deltaY) >10 * Math.abs(deltaX)) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            default:
                break;
        }
        mLastXIntercept = x;
        mLastYIntercept = y;
        return super.dispatchTouchEvent(event);
    }

    /**
     * 设置当前进度
     *
     * @param current 进度
     */
    public void setCurrent(int current) {
        if (isTouchDown)return;
        currentT = lineCount * current / 100;
        invalidate();
        if (listener != null) {
            listener.onProgressChanged(current, false);
        }
    }

    public int getProgress(){
        return currentT;
    }

    /**
     * 设置监听器,跟seekbar的滑动监听一样
     *
     * @param listener 监听器
     */
    public void setOnSeekBarChangeListener(OnSeekChangeListener listener) {
        this.listener = listener;
    }

}
