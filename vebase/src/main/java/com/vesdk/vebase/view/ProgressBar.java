package com.vesdk.vebase.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.vesdk.vebase.log.LogKit;
import com.vesdk.vebase.Constant;
import com.vesdk.vebase.R;


/**
 *  on 2019-07-26 16:59
 * 能够显示数值的 ProgressBar
 * ProgressBar which can displaying values
 */
public class ProgressBar extends View {
    public static final int DEFAULT_RADIUS = 25;
    public static final int DEFAULT_LINE_HEIGHT = 8;
    public static final int DEFAULT_ACTIVE_COLOR = Color.parseColor("#BBFFFFFF");
    public static final int DEFAULT_INACTIVE_COLOR = Color.parseColor("#30000000");
    public static final int DEFAULT_CIRCLE_COLOR = Color.parseColor("#FFFFFF");
    public static final int DEFAULT_TEXT_COLOR = Color.parseColor("#555555");
    public static final int DEFAULT_TEXT_PADDING = 8;
    public static final int DEFAULT_DELAY_SHOW_PROGRESS = 500;
    public static final int DEFAULT_MAX_TEXT_HEIGHT = 50;
    public static final int DEFAULT_MAX_TEXT_SIZE = 30;
    public static final int DEFAULT_ANIMATION_TIME = 15;
    public static final int MAX_PROGRESS = 100;

    // view 基本数据
    // view base data
    private int mWidth;
    private int mLeftPadding;
    private int mRightPadding;
    private int mLinePosition;
    private Paint mPaint;
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect mTextBounds = new Rect();

    private Runnable mShowProgressAction;
    private boolean isTouch;

    // 绘制 line 和 circle 相关
    // draw line and circle
    private int mLineHeight = DEFAULT_LINE_HEIGHT;
    private int mActiveLineColor = DEFAULT_ACTIVE_COLOR;
    private int mInactiveLineColor = DEFAULT_INACTIVE_COLOR;
    private int mCircleRadius = DEFAULT_RADIUS;
    private int mCircleColor = DEFAULT_CIRCLE_COLOR;
    private int mTextColor = DEFAULT_TEXT_COLOR;
    private int mCircleStrokeWidth = 0;
    private int mCircleStrokeColor = DEFAULT_CIRCLE_COLOR;

    private float mProgress = 0F;
    private boolean mNegativeable = false;

    // 展示进度的动画相关
    // show progress animation
    private boolean isShowText;
    private int mDelayShowText = DEFAULT_DELAY_SHOW_PROGRESS;
    private int mMaxTextHeight = DEFAULT_MAX_TEXT_HEIGHT;
    private float mTextHeight;
    private int mMaxTextSize = DEFAULT_MAX_TEXT_SIZE;
    private float mTextSize;
    private int mMaxTextPadding = DEFAULT_TEXT_PADDING;
    private int mTextPadding;
    private int mAnimationTime = DEFAULT_ANIMATION_TIME;
    private float mTextSizeSlot;
    private float mTextHeightSlot;
    private int max = MAX_PROGRESS;

    private OnProgressChangedListener mListener;

    public ProgressBar(Context context) {
        super(context);
        init();
    }

    public ProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init();
    }

    // 从 xml 中加载初始数据
    // load init data form xml
    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar);
        mActiveLineColor = ta.getColor(R.styleable.ProgressBar_activeLineColor, DEFAULT_ACTIVE_COLOR);
        mInactiveLineColor = ta.getColor(R.styleable.ProgressBar_inactiveLineColor, DEFAULT_INACTIVE_COLOR);
        mCircleColor = ta.getColor(R.styleable.ProgressBar_circleColor, DEFAULT_CIRCLE_COLOR);
        mCircleStrokeColor = ta.getColor(R.styleable.ProgressBar_circleStrokeColor, DEFAULT_CIRCLE_COLOR);
        mTextColor = ta.getColor(R.styleable.ProgressBar_textColor, DEFAULT_TEXT_COLOR);
        mLineHeight = ta.getDimensionPixelSize(R.styleable.ProgressBar_lineHeight, DEFAULT_LINE_HEIGHT);
        mCircleStrokeWidth = ta.getDimensionPixelSize(R.styleable.ProgressBar_circleStrokeWidth, 0);
        mCircleRadius = ta.getDimensionPixelSize(R.styleable.ProgressBar_circleRadius, DEFAULT_RADIUS);
        mDelayShowText = ta.getDimensionPixelSize(R.styleable.ProgressBar_delayShowText, DEFAULT_DELAY_SHOW_PROGRESS);
        mMaxTextPadding = ta.getDimensionPixelSize(R.styleable.ProgressBar_textPadding, DEFAULT_TEXT_PADDING);
        mAnimationTime = ta.getInt(R.styleable.ProgressBar_animationTime, DEFAULT_ANIMATION_TIME);
        mTextSize = ta.getDimensionPixelSize(R.styleable.ProgressBar_textSize, DEFAULT_MAX_TEXT_SIZE);
        mTextHeight = ta.getDimensionPixelSize(R.styleable.ProgressBar_textHeight, DEFAULT_MAX_TEXT_HEIGHT);
        mProgress = Math.max(0, Math.min(1, ta.getFloat(R.styleable.ProgressBar_progress, 0)));
        ta.recycle();
    }

    // 初始化
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // 延迟显示 text Runnable
        // delay to show text
        mShowProgressAction = new Runnable() {
            @Override
            public void run() {
                isShowText = false;
                postInvalidate();
                LogKit.INSTANCE.d(Constant.TAG,"mShowProgressAction--------");
            }
        };
    }

    // 初始化各基准值大小
    private void initSize(int w, int h) {
        mWidth = w;

        mPaint.setTextSize(mMaxTextSize);
        int maxTextWidth = (int) mPaint.measureText(String.valueOf(max)) / 2;
        int padding = Math.max(maxTextWidth, mCircleRadius);
        mLeftPadding = padding + getPaddingStart();
        mRightPadding = padding + getPaddingEnd();

        mTextHeightSlot = mMaxTextHeight * 1F / mAnimationTime;
        mTextSizeSlot = mMaxTextSize * 1F / mAnimationTime;
        mTextPadding = (int) (mMaxTextPadding + (1 - mTextSize * 1F / mMaxTextSize) * (mCircleRadius - mMaxTextPadding));
        mLinePosition = h / 2;
    }

    // 设置监听器
    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        mListener = listener;
    }

    // 设置是否为双向
    public void setNegativeable(boolean negativeable) {
        if (mNegativeable == negativeable) return;

        mNegativeable = negativeable;
    }

    // 设置进度值
    public void setProgress(float progress) {
        mProgress = clip(progress);
        invalidate();
        if (mListener != null) {
            mListener.onProgressChanged(this, progress, false, MotionEvent.ACTION_UP);
        }
    }

    public void setMax(int max) {
        this.max = max;
    }

    // 获取进度值
    public float getProgress() {
        return mProgress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawLine(canvas);
        drawCircle(canvas);
        drawText1(canvas);
    }

    // 绘制进度线
    private void drawLine(Canvas canvas) {
        Paint paint = mPaint;
        float startX, endX, startY, endY;
        startY = endY = mLinePosition;

        paint.setStrokeWidth(mLineHeight);
        if (mNegativeable) {
            startX = mLeftPadding;
            endX = mWidth - mRightPadding;
            paint.setColor(mInactiveLineColor);
            canvas.drawLine(startX, startY, endX, endY, paint);

            paint.setColor(mActiveLineColor);
            if (mProgress < 0.5) {
                startX = mLeftPadding + (mWidth - mLeftPadding - mRightPadding) * mProgress;
                endX = mLeftPadding + (mWidth - mLeftPadding - mRightPadding) * 0.5f;
            } else {
                startX = mLeftPadding + (mWidth - mLeftPadding - mRightPadding) * 0.5f;
                endX = mLeftPadding + (mWidth - mLeftPadding - mRightPadding) * mProgress;
            }
            canvas.drawLine(startX, startY, endX, endY, paint);

        } else {
            paint.setColor(mActiveLineColor);
            startX = mLeftPadding;
            endX = mLeftPadding + (mWidth - mLeftPadding - mRightPadding) * mProgress;
            canvas.drawLine(startX, startY, endX, endY, paint);

            paint.setColor(mInactiveLineColor);
            startX = endX;
            endX = mWidth - mRightPadding;
            canvas.drawLine(startX, startY, endX, endY, paint);
        }


    }

    float cx;
    float cy;

    private void drawCircle(Canvas canvas) {
        if (isShowText) {
            calculateTextHeightAndSize();
        }
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setColor(mCircleStrokeColor);
        mCirclePaint.setStrokeWidth(mCircleStrokeWidth);
        cx = mLeftPadding + (mWidth - mLeftPadding - mRightPadding) * mProgress;
        cy = mLinePosition;
        canvas.drawCircle(cx, cy, mCircleRadius - mCircleStrokeWidth / 2F, mCirclePaint);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(mCircleColor);
        canvas.drawCircle(cx, cy, mCircleRadius - mCircleStrokeWidth, mCirclePaint);
    }

    private void drawText1(Canvas canvas) {
        if (true) {
            mTextPaint.setColor(mTextColor);
            mTextPaint.setTextSize(mTextSize);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            String text = customText(mProgress);
            mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);

            float cx = mLeftPadding + (mWidth - mLeftPadding - mRightPadding) * mProgress;
            float cy1 = cy - mCircleRadius / 2F - mTextBounds.height();
            canvas.drawText(text, cx, cy1, mTextPaint);
        }
    }

    private String customText(float mProgress) {
        if (customTextListener != null) {
            return customTextListener.customText(mProgress);
        } else {
            float value = 0.0f;
            if (mNegativeable) {
                value = (mProgress - 0.5f) * max;
            } else {
                value = mProgress * max;
            }
            return String.format("%.0f", value);
        }
    }


    // 当进度数字需要展示时，计算当前数字的高度及大小，以实现动画效果
    // When the progress number needs to be displayed, calculate the height and size of the current number to achieve animation effect
    private void calculateTextHeightAndSize() {
        if (isTouch && mTextHeight < mMaxTextHeight) {
            // 上升动画
            // Rise in the animation
            mTextHeight += mTextHeightSlot;
            mTextSize += mTextSizeSlot;
        } else if (!isTouch && mTextHeight > 0) {
            // 下落动画
            // Whereabouts of the animation
            mTextHeight -= mTextHeightSlot;
            mTextSize -= mTextSizeSlot;

            if (mTextHeight <= 0 && mTextSize <= 0) {
                isShowText = false;
            }
        }
        // 截取为标准值
        // Intercept to the standard value
        if (mTextSize > mMaxTextSize) {
            mTextSize = mMaxTextSize;
        } else if (mTextSize < 0) {
            mTextSize = 0;
        }
        if (mTextHeight > mMaxTextHeight) {
            mTextHeight = mMaxTextHeight;
        } else if (mTextHeight < 0) {
            mTextHeight = 0;
        }
        postInvalidate();
        mTextPadding = (int) (mMaxTextPadding + (1 - mTextSize * 1F / mMaxTextSize) * (mCircleRadius - mMaxTextPadding));
    }

    // 根据用户当前触摸的位置计算进度值
    // Calculate the progress value based on the user's current touch location
    private float calculateProgress(int x) {
        // 考虑到左右两边的 padding
        if (x < mLeftPadding) {
            x = mLeftPadding;
        } else if (x > mWidth - mRightPadding) {
            x = mWidth - mRightPadding;
        }
        return mProgress = 1F * (x - mLeftPadding) / (mWidth - mLeftPadding - mRightPadding);
    }

    private float clip(float progress) {
        if (progress > 1) {
            progress = 1F;
        } else if (progress < 0) {
            progress = 0F;
        }
        return progress;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouch = true;
                postDelayed(mShowProgressAction, mDelayShowText);
                calculateProgress((int) event.getX());
                if (mListener != null) {
                    mListener.onProgressChanged(this, mProgress, true, MotionEvent.ACTION_DOWN);
                }
                postInvalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                calculateProgress((int) event.getX());
                if (mListener != null) {
                    mListener.onProgressChanged(this, mProgress, true, MotionEvent.ACTION_MOVE);
                }
                postInvalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isTouch = false;
                if (!isShowText) {
                    removeCallbacks(mShowProgressAction);
                }
                calculateProgress((int) event.getX());
                if (mListener != null) {
                    mListener.onProgressChanged(this, mProgress, true, MotionEvent.ACTION_UP);
                    if (mListener instanceof  OnProgressChangedActionListener){
                        ((OnProgressChangedActionListener)mListener).onActionUp(mProgress);
                    }
                }

                postInvalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        initSize(w, h);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public interface OnProgressChangedListener {
        void onProgressChanged(ProgressBar progressBar, float progress, boolean isFormUser, int eventAction);
    }

    public interface OnProgressChangedActionListener extends  OnProgressChangedListener {
        void onProgressChanged(ProgressBar progressBar, float progress, boolean isFormUser, int eventAction);

        void onActionUp(float progress);
    }


    public interface OnNeedDrawCustomTextListener {
        String customText(float progress);
    }

    private OnNeedDrawCustomTextListener customTextListener;

    public void setCustomTextListener(OnNeedDrawCustomTextListener customTextListener) {
        this.customTextListener = customTextListener;
    }
}
