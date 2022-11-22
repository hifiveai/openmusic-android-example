package com.ss.ugc.android.editor.base.utils;


//import com.ss.android.vesdk.VEVersionUtil;
import com.bytedance.ies.nlemediajava.utils.VESDKUtils;
import com.ss.ugc.android.editor.base.BuildConfig;

/**
 * time : 2020/5/15
 * author : tanxiao
 * description :
 */
public class SDKInfoUtil {
    public static String getInfoText() {
        StringBuilder builder = new StringBuilder();
        builder.append("FLAVOR:").append(BuildConfig.FLAVOR);
//        builder.append(" PRODUCT:" + BuildConfig.PRODUCT);
//        builder.append(" ABI:" + BuildConfig.ABI);
        builder.append(" TYPE:" + BuildConfig.BUILD_TYPE);
        builder.append(" VERSION:" + VESDKUtils.Companion.getVEVersion());
        return builder.toString();
    }
}
