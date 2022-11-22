package com.vesdk.verecorder.record.demo.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vesdk.verecorder.R;


public class CustomLinearLayout extends LinearLayout implements View.OnClickListener {

    private TextView textView = null;
    private ListView listView = null;

    public CustomLinearLayout(Context context) {
        super(context);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private TextView tv_speed_value, tv_time_value;
    private TextView speed_slow_p, speed_slow, speed_normal, speed_fast, speed_fast_p;
    private TextView time_15, time_60, time_free;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();


        llBottom1 = (LinearLayout) findViewById(R.id.ll_bottom1);
        llBottom2 = (LinearLayout) findViewById(R.id.ll_bottom2);
        llBottom3 = (LinearLayout) findViewById(R.id.ll_bottom3);

        tv_time_value = (TextView) findViewById(R.id.tv_time_value);
        tv_speed_value = (TextView) findViewById(R.id.tv_speed_value);
        findViewById(R.id.tv_speed).setOnClickListener(this);
        findViewById(R.id.tv_time).setOnClickListener(this);
        findViewById(R.id.tv_speed_expand).setOnClickListener(this);
        findViewById(R.id.tv_time_expand).setOnClickListener(this);

        speed_slow_p = findViewById(R.id.speed_slow_p);
        speed_slow_p.setOnClickListener(this);
        speed_slow = findViewById(R.id.speed_slow);
        speed_slow.setOnClickListener(this);
        speed_normal = findViewById(R.id.speed_normal);
        speed_normal.setOnClickListener(this);
        speed_fast = findViewById(R.id.speed_fast);
        speed_fast.setOnClickListener(this);
        speed_fast_p = findViewById(R.id.speed_fast_p);
        speed_fast_p.setOnClickListener(this);

        time_15 = findViewById(R.id.time_15);
        time_15.setOnClickListener(this);
        time_60 = findViewById(R.id.time_60);
        time_60.setOnClickListener(this);
        time_free = findViewById(R.id.time_free);
        time_free.setOnClickListener(this);
    }

    private LinearLayout llBottom1, llBottom2, llBottom3;


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.tv_speed) {
            llBottom1.setVisibility(View.GONE);
            llBottom2.setVisibility(View.VISIBLE);
            llBottom3.setVisibility(View.GONE);
            showColor();
            return;
        } else if (id == R.id.tv_time) {
            llBottom1.setVisibility(View.GONE);
            llBottom2.setVisibility(View.GONE);
            llBottom3.setVisibility(View.VISIBLE);
            showColor();
            return;

        } else if (id == R.id.speed_slow_p) {
            mSelectedSpeed = SpeedStatus.SPEED_SLOW_P;
            tv_speed_value.setText(getContext().getString(R.string.ck_speed_extremely_slow));
            speed = 0.33f;
        } else if (id == R.id.speed_slow) {
            mSelectedSpeed = SpeedStatus.SPEED_SLOW;
            tv_speed_value.setText(getContext().getString(R.string.ck_speed_slow));
            speed = 0.5f;
        } else if (id == R.id.speed_normal) {
            mSelectedSpeed = SpeedStatus.SPEED_NORMAL;
            tv_speed_value.setText(getContext().getString(R.string.ck_speed_standard));
            speed = 1f;
        } else if (id == R.id.speed_fast) {
            mSelectedSpeed = SpeedStatus.SPEED_FAST;
            tv_speed_value.setText(getContext().getString(R.string.ck_speed_quick));
            speed = 2f;
        } else if (id == R.id.speed_fast_p) {
            mSelectedSpeed = SpeedStatus.SPEED_FAST_P;
            tv_speed_value.setText(getContext().getString(R.string.ck_speed_extremely_quick));
            speed = 3f;
        } else if (id == R.id.time_15) {
            tv_time_value.setText("15s");
            mSelectedTime = TimeStatus.TIME_15;
            duration = 15;
        } else if (id == R.id.time_60) {
            tv_time_value.setText("60s");
            mSelectedTime = TimeStatus.TIME_60;
            duration = 60;
        } else if (id == R.id.time_free) {
            tv_time_value.setText(getContext().getString(R.string.ck_free_duration));
            mSelectedTime = TimeStatus.TIME_free;
            duration = -1;
        }

        showBottom();
        listener.onClick(duration, speed, mSelectedTime, mSelectedSpeed);
    }

    private void showColor() {

        time_15.setTextColor(Color.parseColor("#ffffff"));
        time_60.setTextColor(Color.parseColor("#ffffff"));
        time_free.setTextColor(Color.parseColor("#ffffff"));

        speed_fast_p.setTextColor(Color.parseColor("#ffffff"));
        speed_fast.setTextColor(Color.parseColor("#ffffff"));
        speed_normal.setTextColor(Color.parseColor("#ffffff"));
        speed_slow.setTextColor(Color.parseColor("#ffffff"));
        speed_slow_p.setTextColor(Color.parseColor("#ffffff"));

        if (mSelectedTime == TimeStatus.TIME_15) {
            time_15.setTextColor(Color.parseColor("#E36E55"));
        }
        if (mSelectedTime == TimeStatus.TIME_60) {
            time_60.setTextColor(Color.parseColor("#E36E55"));
        }
        if (mSelectedTime == TimeStatus.TIME_free) {
            time_free.setTextColor(Color.parseColor("#E36E55"));
        }
        if (mSelectedSpeed == SpeedStatus.SPEED_SLOW_P) {
            speed_slow_p.setTextColor(Color.parseColor("#E36E55"));
        }
        if (mSelectedSpeed == SpeedStatus.SPEED_SLOW) {
            speed_slow.setTextColor(Color.parseColor("#E36E55"));
        }
        if (mSelectedSpeed == SpeedStatus.SPEED_NORMAL) {
            speed_normal.setTextColor(Color.parseColor("#E36E55"));
        }
        if (mSelectedSpeed == SpeedStatus.SPEED_FAST) {
            speed_fast.setTextColor(Color.parseColor("#E36E55"));
        }
        if (mSelectedSpeed == SpeedStatus.SPEED_FAST_P) {
            speed_fast_p.setTextColor(Color.parseColor("#E36E55"));
        }

    }



    public void showBottom() {
        llBottom1.setVisibility(View.VISIBLE);
        llBottom2.setVisibility(View.GONE);
        llBottom3.setVisibility(View.GONE);
    }


    public void setonClick(OnCustomClickItem listener) {
        this.listener = listener;
    }

    public void setDurationHide(boolean isHide) {
        findViewById(R.id.tv_time).setVisibility( isHide ? GONE: VISIBLE);
        findViewById(R.id.tv_time_value).setVisibility( isHide ? GONE: VISIBLE);
    }

    public OnCustomClickItem listener;

    public interface OnCustomClickItem {

        void onClick(int duration, float speed, TimeStatus mSelectedTime, SpeedStatus mSelectedSpeed);
    }


    private int duration = -1;
    private float speed = 1.0f;
    public SpeedStatus mSelectedSpeed = SpeedStatus.SPEED_NORMAL;
    public TimeStatus mSelectedTime = TimeStatus.TIME_free;

    public enum SpeedStatus {
        SPEED_SLOW_P,
        SPEED_SLOW,
        SPEED_NORMAL,
        SPEED_FAST,
        SPEED_FAST_P,
    }

    public enum TimeStatus {
        TIME_15,
        TIME_60,
        TIME_free
    }
}