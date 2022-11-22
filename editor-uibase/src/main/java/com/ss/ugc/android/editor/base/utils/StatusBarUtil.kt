package com.ss.ugc.android.editor.base.utils

import android.R.id
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout.LayoutParams
import androidx.annotation.ColorInt
import com.ss.ugc.android.editor.core.utils.DLog
import java.lang.reflect.Field

/**
 * @date: 2021/7/16
 * @desc: 状态栏适配工具类
 */
object StatusBarUtil {

    private const val TAG = "YPStatusBarUtil"

    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param color 颜色
     */
    fun setStatusBarColor(activity: Activity?, color: Int) {
        if (activity == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //解决华为手机等状态栏上面有一个蒙层问题
            try {
                val decorViewClazz = Class.forName("com.android.internal.policy.DecorView")
                val field: Field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor")
                field.isAccessible = true
                field.setInt(activity.window.decorView, Color.TRANSPARENT) //改为透明
            } catch (e: Exception) {
                DLog.e(TAG, "setStatusBarColor" + e.message)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window?.statusBarColor = color
        } else
            activity.window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                val decorView = decorView as ViewGroup
                val count = decorView.childCount
                if (count > 0 && null != decorView.getChildAt(count - 1)) {
                    decorView.getChildAt(count - 1).setBackgroundColor(color)
                } else {
                    val statusView: View = createStatusBarView(activity, color)
                    decorView.addView(statusView)
                }
                setRootView(activity)
            }
    }

    private fun createStatusBarView(activity: Activity, @ColorInt color: Int): View {
        val statusBarView = View(activity)
        val params = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity))
        statusBarView.layoutParams = params
        statusBarView.setBackgroundColor(color)
        return statusBarView
    }

    private fun setRootView(activity: Activity?) {
        if (activity != null) {
            val parent = activity.findViewById<ViewGroup>(id.content)
            if (parent != null) {
                val rootView = parent.getChildAt(0) as ViewGroup
                rootView.fitsSystemWindows = true
                rootView.clipToPadding = true
            }
        }
    }


    @TargetApi(19)
    fun setStatusTextColorLight(activity: Activity?, @ColorInt color: Int, isLightStatusText: Boolean, fitsystem: Boolean) {
        if (activity == null) return
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val window: Window? = activity.window
            if (window != null) {
                val decorView: View = window.decorView
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                var option = 0
                if (!fitsystem) {
                    option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
                if (!isLightStatusText) {
                    option = option or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                decorView.systemUiVisibility = option
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
            val window: Window? = activity.window
            if (window != null) {
                val decorView: View = window.decorView
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                val option: Int = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                decorView.systemUiVisibility = if (fitsystem) 0 else option
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            }
        } else
            activity.window?.apply {
                val attributes: WindowManager.LayoutParams = attributes
                if (attributes != null) {
                    val flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    if (fitsystem) {
                        attributes.flags = 0
                    } else {
                        attributes.flags = attributes.flags or flagTranslucentStatus
                    }
                    setAttributes(attributes)
                }
            }
        setStatusBarColor(activity, color)
    }

    //获取状态栏高度
    fun getStatusBarHeight(context: Context?): Int {
        var result = 0
        if (context != null) {
            val re: Resources = context.resources
            val resourceId: Int = re.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = re.getDimensionPixelSize(resourceId)
            }
        }
        return result
    }
}