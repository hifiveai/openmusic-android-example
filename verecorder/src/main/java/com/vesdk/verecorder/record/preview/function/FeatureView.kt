package com.vesdk.verecorder.record.preview.function

import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import  androidx.lifecycle.ViewModelProvider
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.util.Size
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import com.vesdk.vebase.log.LogKit
import com.dmcbig.mediapicker.utils.ScreenUtils
import com.ss.android.vesdk.VEFocusSettings
import com.vesdk.vebase.Constant
import com.vesdk.verecorder.record.preview.function.CameraViewHelper.SimpleChangeListener
import com.vesdk.verecorder.record.preview.model.Resolution
import com.vesdk.verecorder.record.preview.viewmodel.PreviewModel
import kotlin.math.min

/**
 * 处理曝光、对焦
 */
class FeatureView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val cameraViewHelper by lazy {
        CameraViewHelper(context, this)
    }

    private val gesture by lazy {
        GestureDetector(context, gestureListener)
    }


    private var lastScale = 1f //记录上次的缩放比例，下次缩放时是在此基础上进行的

    private var zoomListener: OnZoomListener? = null

    interface OnZoomListener{
        fun zoom(fl: Float)
    }

    private var focusEnableListener: OnFocusEnable? = null

    interface OnFocusEnable {
        fun focusEnable(): Boolean
    }

    fun setOnZoomListener(listener: OnZoomListener?) {
        this.zoomListener = listener
    }

    fun setOnFocusEnable(listener: OnFocusEnable?) {
        this.focusEnableListener = listener
    }

    private val scaleGesture by lazy {
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override
            fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = detector.scaleFactor //缩放因子，两指靠拢时小于1
                val x = detector.focusX
                val y = detector.focusY //中心点坐标
                LogKit.d(Constant.TAG,"缩放手势  onScale，$scale x:$x y:$y")

                var temp = lastScale * scale
                if ( lastScale * scale <= 1.0f ){
                    temp = 0f
                }

                var zoomTemp = min(60f, temp)
//                zoomTemp = max(zoomTemp, 0f)

                previewModel.zoom(zoomTemp)
                zoomListener?.zoom(zoomTemp)

                return super.onScale(detector)
            }

            override
            fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                LogKit.d(Constant.TAG, "缩放手势---  onScaleBegin，" + detector.scaleFactor ) //始终是1
                return super.onScaleBegin(detector)
            }

            override
            fun onScaleEnd(detector: ScaleGestureDetector) {
                super.onScaleEnd(detector)

                lastScale *= detector.scaleFactor
                if (lastScale <= 1f ){
                    lastScale = 1f
                }
                lastScale = min(60f, lastScale)
//                lastScale = max(lastScale, 0f)
                LogKit.d(Constant.TAG,"缩放手势---  onScaleEnd，最终scale比例 ${detector.scaleFactor}  scale值 $lastScale")
            }
        })

    }

    private val previewModel by lazy {
        val appCompatActivity = context as AppCompatActivity
        ViewModelProvider(appCompatActivity,
                ViewModelProvider.AndroidViewModelFactory.getInstance(appCompatActivity.application))
                .get(PreviewModel::class.java)
    }

    private val surfaceSize by lazy {
        Size(ScreenUtils.getScreenWidth(context), ScreenUtils.getScreenHeight(context))
    }

    init {
        previewModel.curConfig.observe(context as AppCompatActivity, Observer {
            it?.let {
                hideFocusIcon()
                updateParams(it)
            }
        })

        cameraViewHelper.setExposureListener(object : SimpleChangeListener() {
            override fun onChanged(level: Int) {
                previewModel.setExposureCompensation(100 - level)
            }
        })
    }

    private fun updateParams(config: Resolution) {
        layoutParams = layoutParams?.apply {
            width = surfaceSize.width
            height = (width * config.ratio).toInt()
        }
    }


    private val gestureListener by lazy {
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                return e?.let {
                    val focusEnable = focusEnableListener == null || focusEnableListener?.focusEnable() == true
                    if (focusEnable && focusAtPoint(e)) {
                        previewModel.setExposureCompensation(50)
                        cameraViewHelper.showFocusIcon(e.x.toInt(), e.y.toInt())
                    }
                    true
                } ?: false
            }

            override fun onLongPress(e: MotionEvent?) {
                e?.let {
                    val focusEnable = focusEnableListener == null || focusEnableListener?.focusEnable() == true
                    if (focusEnable && focusAtPoint(e)) {
                        previewModel.setExposureCompensation(50)
                        cameraViewHelper.showFocusIcon(e.x.toInt(), e.y.toInt())
                    }
                }
            }
        }
    }

    private fun focusAtPoint(e: MotionEvent?): Boolean {
        return e?.let {
            //焦点位置，相对于View
            //预览view的宽高
            val width: Int = measuredWidth
            val height: Int = measuredHeight
            //屏幕密度
            val displayDensity = context.resources.displayMetrics.density
            val settings = VEFocusSettings.Builder(e.x.toInt(), e.y.toInt(), width, height, displayDensity).build()
            previewModel.focusAtPoint(settings)
        } ?: false
    }


    private fun hideFocusIcon() {
        cameraViewHelper.hideFocusIcon()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gesture.onTouchEvent(event)
        scaleGesture.onTouchEvent(event)
        cameraViewHelper.onTouchEvent(event)
        return true
    }

}
