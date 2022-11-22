package com.ss.ugc.android.editor.bottom.panel.audio.record

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.RelativeLayout
import androidx.annotation.ColorRes
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.bottom.R
import kotlinx.android.synthetic.main.layout_recording_btn.view.recordInIv
import kotlinx.android.synthetic.main.layout_recording_btn.view.recordOutIv

class AudioRecordButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var animationIn: Animation? = null
    private var animationOut: Animation? = null
    private var animationStrokeIn: Animation? = null
    private var animationStrokeOut: Animation? = null

    private var callback: Callback? = null
    private var holding = false

    private val pressRunnable = Runnable {
        holding = true
        isPressed = true
        callback?.hold()
        playAnimation()
    }

    init {
        val rootView =
            LayoutInflater.from(context).inflate(R.layout.layout_recording_btn, this, true)
        animationIn = loadAnimation(context, R.anim.anim_recording_in)
        animationStrokeIn = loadAnimation(context, R.anim.anim_recording_in_stroke)
        animationOut = loadAnimation(context, R.anim.anim_recording_out)
        animationStrokeOut = loadAnimation(context, R.anim.anim_recording_out_stroke)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        if (!isEnabled) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                postDelayed(pressRunnable, 200)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (holding) {
                    isPressed = false
                    holding = false
                    callback?.release()
                    pauseAnimation()
                } else {
                    removeCallbacks(pressRunnable)
                }
            }
        }
        return true
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    interface Callback {
        fun hold()
        fun release()
    }

    fun setRecordColor( @ColorRes colorRes : Int){
        val grad: GradientDrawable = recordInIv.background as GradientDrawable
        grad.setColor(ContextCompat.getColor(context, colorRes))
        recordInIv.background = grad


        val gradOut: GradientDrawable = recordOutIv.background as GradientDrawable
        gradOut.setStroke(SizeUtil.dp2px(2f),ContextCompat.getColor(context, colorRes))
        recordOutIv.background = gradOut

    }

    private fun playAnimation() {
        recordInIv?.startAnimation(animationIn)
        recordOutIv?.startAnimation(animationStrokeIn)
    }

    private fun pauseAnimation() {
        recordInIv?.startAnimation(animationOut)
        recordOutIv?.startAnimation(animationStrokeOut)
    }
}
