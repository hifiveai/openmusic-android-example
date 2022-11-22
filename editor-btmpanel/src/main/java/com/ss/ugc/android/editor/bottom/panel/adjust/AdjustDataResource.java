package com.ss.ugc.android.editor.bottom.panel.adjust;

import android.annotation.SuppressLint;

import com.ss.ugc.android.editor.base.constants.TypeConstants;
import com.ss.ugc.android.editor.bottom.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *  on 2019-07-21 13:58
 */
public class AdjustDataResource {

    private static final Map<Integer, Float> DEFAULT_ADJUST_VALUE;
    private static final Map<Integer, Boolean> DEFAULT_ADJUST_NEGATIVEABLE;


    static {
        @SuppressLint("UseSparseArrays") Map<Integer, Float> adjustMap = new HashMap<>();
        @SuppressLint("UseSparseArrays") Map<Integer, Boolean> adjustBooleanMap = new HashMap<>();
        // 调节面板的各项  如果是双向的  默认值0的话 其实是0.5
        adjustMap.put(TypeConstants.getTYPE_ADJUST_LD(), 0F);
        adjustMap.put(TypeConstants.getTYPE_ADJUST_DBD(), 0F);
        adjustMap.put(TypeConstants.getTYPE_ADJUST_SW(), 0F);
        adjustMap.put(TypeConstants.getTYPE_ADJUST_BHD(), 0F);
        adjustMap.put(TypeConstants.getTYPE_ADJUST_GG(), 0F);

        adjustMap.put(TypeConstants.getTYPE_ADJUST_TS(), 0F);
        adjustMap.put(TypeConstants.getTYPE_ADJUST_YY(), 0F);
        adjustMap.put(TypeConstants.getTYPE_ADJUST_AJ(), 0F);
        adjustMap.put(TypeConstants.getTYPE_ADJUST_RH(), 0F);
        //曝光
        adjustMap.put(TypeConstants.getTYPE_ADJUST_BGD(), 0F);
        //色调
        adjustMap.put(TypeConstants.getTYPE_ADJUST_SD(), 0F);
        //---------
        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_LD(), true);
        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_DBD(), true);
        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_SW(), true);
        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_BHD(), true);
        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_GG(), true);
        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_BGD(), true);
        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_SD(), true);

        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_TS(), false);
        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_YY(), false);
        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_AJ(), false);
        adjustBooleanMap.put(TypeConstants.getTYPE_ADJUST_RH(), false);
        // 滤镜
        // filter

        DEFAULT_ADJUST_VALUE = Collections.unmodifiableMap(adjustMap);
        DEFAULT_ADJUST_NEGATIVEABLE = Collections.unmodifiableMap(adjustBooleanMap);
    }

    public float getDefaultValue(int type) {
        Float value =  getDefaultMap().get(type);
        return value == null ? 0F : value;
    }

    public boolean getDefaultNegative(int type) {
        Boolean value =  getDefaultNegativeMap().get(type);
        return value == null ? false : value;
    }
    private static final int[] IMAGES = new int[] {
            R.drawable.ic_change_ld,
            R.drawable.ic_change_bg,
            R.drawable.ic_change_sd,
            R.drawable.ic_change_dbd,
            R.drawable.ic_change_bhd,
            R.drawable.ic_change_gg,
            R.drawable.ic_change_sw,
            R.drawable.ic_change_rh,
            R.drawable.ic_change_yy,
            R.drawable.ic_change_ts,
            R.drawable.ic_change_aj,

    };


    private Map<Integer, Float> getDefaultMap() {

        return DEFAULT_ADJUST_VALUE;
    }

    private Map<Integer, Boolean> getDefaultNegativeMap() {

            return DEFAULT_ADJUST_NEGATIVEABLE;
    }



    private String getTestAdjustPath() {
        return "/sdcard/effect_tob/amazingAdjust/";
    }



}
