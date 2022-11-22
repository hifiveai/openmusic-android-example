package com.vesdk.verecorder.record.demo;

import android.content.Context;
import android.content.res.TypedArray;
import  androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import  androidx.core.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vesdk.vebase.log.LogKit;
import com.vesdk.vebase.Constant;
import com.vesdk.vebase.DemoApplication;
import com.vesdk.verecorder.R;


public class ButtonView extends FrameLayout {
    public static final float WH_RATIO = 1F;

    private int colorOn = ActivityCompat.getColor(DemoApplication.context(), R.color.tv_bottom_color);
    private int colorOff = ActivityCompat.getColor(DemoApplication.context(), R.color.colorWhite);

    private LinearLayout llContent;
    private ImageView iv;
    private TextView tvTitle;
    private TextView tvDesc;
    private View vPoint;

    private boolean isOn = false;
    private boolean isPointOn = false;

    public ButtonView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ButtonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAttr(context, attrs);
    }

    public ButtonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initAttr(context, attrs);
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener l) {
        llContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (l != null) {
                    l.onClick(ButtonView.this);
                }
            }
        });
    }

    private void init(Context context) {
        llContent = (LinearLayout) LayoutInflater
                .from(context).inflate(R.layout.recorder_view_face_options, this, false);
        addView(llContent);

//        llContent.post(new Runnable() {
//            @Override
//            public void run() {
//                int height = llContent.getHeight();
//                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) llContent.getLayoutParams();
//                lp.width = (int) (height * WH_RATIO);
//                llContent.setLayoutParams(lp);
//            }
//        });


        iv = findViewById(R.id.iv_face_options);
        tvTitle = findViewById(R.id.tv_title_face_options);
        tvDesc = findViewById(R.id.tv_desc_face_options);
        vPoint = findViewById(R.id.v_face_options);

//        colorOn = ActivityCompat.getColor(context, R.color.colorWhite);
//        colorOff = ActivityCompat.getColor(context, R.color.colorGrey);
        colorOn = ActivityCompat.getColor(context, R.color.tv_bottom_color);
        colorOff = ActivityCompat.getColor(context, R.color.colorWhite);


    }

    private void initAttr(Context context, AttributeSet attr) {
        TypedArray arr = context.obtainStyledAttributes(attr, R.styleable.ButtonView);

        int resource = arr.getResourceId(R.styleable.ButtonView_src, 0);
        String title = arr.getString(R.styleable.ButtonView_title);
        String desc = arr.getString(R.styleable.ButtonView_desc);
        iv.setImageResource(resource);
        tvTitle.setText(title);
        if (desc == null || desc.isEmpty()) {
            tvDesc.setVisibility(GONE);
        } else {
            tvDesc.setVisibility(VISIBLE);
            tvDesc.setText(desc);
        }

        arr.recycle();
    }

    public void setIcon(int iconResource) {
        LogKit.INSTANCE.d(Constant.TAG,"icon:" + iconResource);
        iv.setImageResource(iconResource);
    }

    public void setTitle(String title) {
        LogKit.INSTANCE.d(Constant.TAG,"title:" + title );
        if (title.isEmpty()) {
            tvTitle.setVisibility(GONE);
        } else {
            tvTitle.setVisibility(VISIBLE);
            tvTitle.setText(title);
        }
    }

    public void setDesc(String desc) {
        if (desc == null || desc.isEmpty()) {
            tvDesc.setVisibility(GONE);
        } else {
            tvDesc.setVisibility(VISIBLE);
            tvDesc.setText(desc);
        }
    }

    public void change(boolean on) {
        if (on) {
            on();
        } else {
            off();
        }
    }

    public void on() {
        isOn = true;
        setColor(colorOn);
    }

    public void off() {
        isOn = false;
        setColor(colorOff);
    }

    public void pointChange(boolean on) {
        isPointOn = on;
        if (on) {
            vPoint.setBackgroundResource(R.drawable.bg_face_options_point);
        } else {
            vPoint.setBackgroundResource(0);
        }
    }

    public boolean isOn() {
        return isOn;
    }

    public boolean isPointOn() {
        return isPointOn;
    }

    private void setColor(int color) {
        LogKit.INSTANCE.d(Constant.TAG,"color:" + color);
        // 1.这种方式在21之前的手机上测试不生效，改用iv.setColorFilter(color);这种方式
//        Drawable drawable = iv.getDrawable();
//        DrawableCompat.setTint(drawable, color);
//        iv.setImageDrawable(drawable);

        iv.setColorFilter(color); //这个方式在oppo 5.1的手机上有问题 美颜面板点击滑动的时候
        //---
//        ColorStateList csl=getResources().getColorStateList(R.color.colorAccent);
//        DrawableCompat.setTintList(drawable,csl);

        tvTitle.setTextColor(color);
        tvDesc.setTextColor(color);
    }
}
