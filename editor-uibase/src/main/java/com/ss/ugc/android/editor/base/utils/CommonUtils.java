package com.ss.ugc.android.editor.base.utils;

import com.ss.ugc.android.editor.core.utils.DLog;

import java.text.SimpleDateFormat;

/**
 * 通用工具
 */
public class CommonUtils {
    private static long lastClicked = 0;
    private static long S_INTERVAL = -1;

    /**
     * 判断快速点击
     *
     * @return
     */
    public static boolean isFastClick() {
        checkDefInterval();
        if (System.currentTimeMillis() - lastClicked < S_INTERVAL) {
            return true;
        }
        lastClicked = System.currentTimeMillis();
        return false;
    }


    public static boolean isFastClick(int interval) {
        DLog.d("tmsg", Long.toString(System.currentTimeMillis()));
        DLog.d("res_t", Long.toString(System.currentTimeMillis() - lastClicked));
        if (System.currentTimeMillis() - lastClicked < interval) {
            return true;
        }
        lastClicked = System.currentTimeMillis();
        return false;
    }

    public static synchronized String createtFileName(String suffix) {
        java.util.Date dt = new java.util.Date(System.currentTimeMillis());
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String fileName = fmt.format(dt);
        fileName = fileName + suffix; //extension, you can change it.
        return fileName;
    }

    private static void checkDefInterval() {
        if (S_INTERVAL <= 0) {
            switch (DeviceLevelUtil.INSTANCE.getDeviceLevel()) {
                case DeviceLevelUtil.LEVEL_MID:
                    S_INTERVAL = 500;
                    break;
                case DeviceLevelUtil.LEVEL_HIGH:
                    S_INTERVAL = 200;
                    break;
                case DeviceLevelUtil.LEVEL_LOW:
                default:
                    S_INTERVAL = 800;
                    break;
            }
        }
    }
}
