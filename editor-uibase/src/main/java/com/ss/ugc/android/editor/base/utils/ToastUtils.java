package com.ss.ugc.android.editor.base.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.ugc.android.editor.base.R;
import com.ss.ugc.android.editor.core.utils.DLog;
import com.ss.ugc.android.editor.core.utils.IToast;
import com.ss.ugc.android.editor.core.utils.Toaster;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * time : 2020/5/25
 * author : tanxiao
 * description :
 * 提示工具类(默认CK的样式)，建议使用{@link IToast}
 */
@Deprecated
public class ToastUtils implements IToast {

    private static Toast sToast = null;

    public static void toastRet(Context context, int ret, String fail, String succeed) {
        if (ret < 0) {
            Toast.makeText(context, fail, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, succeed, Toast.LENGTH_LONG).show();
        }
    }

    public static void toast(String msg) {
        toast(mAppContext, msg);
    }

    public static void toast(Context context, String msg) {
        toast(context, msg, Toast.LENGTH_LONG);
    }

    public static void toast(Context context, String msg, int duration) {
        if (sToast != null) {
            sToast.cancel();
            sToast = null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.editor_toast_layout, null);
        TextView title = view.findViewById(R.id.editor_toast_title);
        title.setText(msg);

        sToast = new Toast(context);
        sToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, SizeUtil.INSTANCE.dp2px(150));
        sToast.setDuration(duration);
        sToast.setView(view);
        sToast.show();
    }

    private static Context mAppContext = null;

    public static void init(Context context) {
        mAppContext = context;
        Toaster.init(context);
        Toaster.setToast(new ToastUtils());
    }

    public static void show(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (null == mAppContext) {
            DLog.d("ToastUtils not inited with Context");
            return;
        }

        toast(mAppContext, msg, Toast.LENGTH_SHORT);
    }

    @Override
    public void onToast(@NotNull Context context, @NotNull String message, @Nullable Integer duration) {
        if (duration == null) {
            duration = Toast.LENGTH_SHORT;
        }
        toast(context, message, duration);
    }
}
