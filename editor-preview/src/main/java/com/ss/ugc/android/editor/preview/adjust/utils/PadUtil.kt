package com.ss.ugc.android.editor.preview.adjust.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.preview.adjust.ModuleCommon
import com.ss.ugc.android.editor.preview.adjust.OrientationListener
import com.ss.ugc.android.editor.preview.adjust.OrientationManager
import kotlin.math.max
import kotlin.math.min

object PadUtil {
    private const val PAD_SW = 600

    private const val PAD_SH = 900

    // Smartisan Keymap code
    // 锤子键盘布局对应的FLAG
    const val MOTION_FLAG_MAC_MODE = 0x800
    // const val KEY_FLAG_MAC_MODE = 0x10

    private val screenSize = Point()
    private val display =
        (ModuleCommon.application.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

    private fun getSW(): Int {
        return getScreenDip(
            true
        )
    }

    private fun getSH(): Int {
        return getScreenDip(
            false
        )
    }

    private fun getScreenDip(isWidth: Boolean): Int {
        val dm = DisplayMetrics()
        display.getMetrics(dm)
        val width = dm.widthPixels
        val height = dm.heightPixels
        return if (isWidth) {
            (min(width, height) / dm.density).toInt()
        } else {
            (max(width, height) / dm.density).toInt()
        }
    }

    private fun isHuaweiDevice(): Boolean {
        return "HUAWEI" == Build.MANUFACTURER || "huawei" == Build.MANUFACTURER
    }

    private data class StaticParams(
        val isPad: Boolean, // 是否平板
        val realWidth: Int, // 屏幕真实宽度(px)
        val sw: Int, // 屏幕宽度(dip)
        val sh: Int, // 屏幕高度(dip)
        val t: Float, // 插值系数
        val viewScale: Float, // 横向缩放比例
        val landViewScale: Float // 横向缩放比例
    )

    private val staticParams: StaticParams?

    init {
        /**
         * 只有华为Pad有平行视界，需要动态获取参数
         * 所以，对于"非华为设备"，以及"华为设备中不可能为pad的设备"，可以获取静态的参数
         *
         * 目前手头的华为手机是360dp, 然后手头的华为平板为640dp
         * 为此，我们给定一个阈值（400dp), 低于这个阈值的华为设备则认定为"不是pad设备"
         * 如果华为真的生产出一台sw低于400的华为平板（不太可能），那我们只能说这个台平板我们尚未适配了，
         * 然后反馈让我们适配的话，加个型号判断即可。
         *
         * 如此，绝大部分情况下不需要每次调用都实时计算相关参数。
         */
        val sw = getSW()
        // 对于非华为的平板，会通过第二个条件
        if ((sw <= 400) || !isHuaweiDevice()) {
            val sh =
                getSH()
            val size = SizeUtil.getScreenSize(ModuleCommon.application)
            val realScreenWidth = min(size.x, size.y)
            val rangedSW = min(max(sw, 640), 834)
            val t = (rangedSW - 640) / (834 - 640).toFloat()
            val viewScale = rangedSW / 834F
            val landViewScale = sh / 1194F
            staticParams =
                StaticParams(
                    sw >= PAD_SW,
                    realScreenWidth,
                    sw,
                    sh,
                    t,
                    viewScale,
                    landViewScale
                )
        } else {
            staticParams = null
        }
    }

    val isPad: Boolean
        get() {
            return staticParams?.isPad ?: (sw >= PAD_SW || isInMagicWindow)
        }

    val isLkp: Boolean = display.displayId > 0

    val isHuawei: Boolean =
        isHuaweiDevice()

    val realWidth: Int
        get() {
            return if (staticParams != null) {
                staticParams.realWidth
            } else {
                val size = SizeUtil.getScreenSize(ModuleCommon.application)
                min(size.x, size.y)
            }
        }

    // If is pad, allow rotation
    fun getScreenWidth(): Int {
        return if (isPad) {
            display.getRealSize(
                screenSize
            )
            screenSize.x
        } else {
            realWidth
        }

    }

    /**
     * 纵向有多少dp
     */
    val sh: Int
        get() {
            return staticParams?.sh ?: getSH()
        }

    /**
     * 横向有多少dp
     */
    val sw: Int
        get() {
            return staticParams?.sw ?: getSW()
        }

    /**
     * 设计稿是以 iPad Pro 分辨率（1194x834)给的，
     * 但是我们的当前要适配的平板分辨率为1024x640(dp)，
     * 设计稿中以834的宽度摆放元素时间距很大，按照此间距，640下显示不完整，
     * 这时候我们需要根据情况做压缩，有两种策略：
     *
     * 1、整体按比例压缩，比如编辑页面的一些滑杆，长度按照百分比设置（可能横竖屏不一样）。
     *    这种情况，我们可以根据给的设计稿View的尺寸乘以 viewScale 即可。
     *
     * 2、View大小不变，间距按等比压缩
     *    比方说首页，在分辨率为834时间距72，View之间比较宽松，
     *    但是在640下需要压缩到36才能较完整地显示，View之间比较紧凑；
     *    为了确保640下可以完整显示，同时在800+等分辨时显示又不至于太紧凑，
     *    我们可以给一个范围[minValue, maxValue]，然后根据sw算一个插值
     *
     */
    private fun getRangedSW(): Int {
        return min(max(sw, 640), 834)
    }

    private val t: Float
        get() {
            return staticParams?.t ?: (getRangedSW() - 640) / (834 - 640).toFloat()
        }

    val viewScale: Float
        get() {
            return staticParams?.viewScale ?: getRangedSW() / 834F
        }
    val landViewScale: Float
        get() {
            return staticParams?.landViewScale ?: getSH() / 1194F
        }

    fun getRangeValue(minValue: Float, maxValue: Float): Float {
        return minValue + (maxValue - minValue) * t
    }

    fun observeOrientationChange(view: View, onChanged: (orientation: Int) -> Unit) {
        if (!isPad) {
            return
        }
        val orientationListener = object :
            OrientationListener {
            override fun onOrientationChanged(orientation: Int) {
                onChanged(orientation)
            }
        }
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                OrientationManager.unregister(
                    orientationListener
                )
            }

            override fun onViewAttachedToWindow(v: View?) {
                OrientationManager.register(
                    orientationListener
                )
            }
        })
    }

    fun isLandscape(orientation: Int) = orientation == Configuration.ORIENTATION_LANDSCAPE

    // 判断是否在华为平行视界模式
    // 若打开应用一开始竖屏可能会判为非平行视界，故不用by lazy
    val isInMagicWindow: Boolean
        get() {
            // staticParams不为空，说明不是华为平板，则肯定不是"平行视界模式"了
            if (staticParams != null) {
                return false
            }
            val config = ModuleCommon.application.resources.configuration.toString()
            return config.contains("hwMultiwindow-magic") || config.contains("hw-magic-windows")
        }

    // 判断是否在华为平行世界横屏分屏模式
    val isInSplitMode: Boolean
        get() {
            if (staticParams != null) {
                return false
            }
            if (!isInMagicWindow) {
                return false
            }
            return getSH() < PAD_SH
        }
}
