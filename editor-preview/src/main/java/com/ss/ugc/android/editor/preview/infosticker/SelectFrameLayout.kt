package com.ss.ugc.android.editor.preview.infosticker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.ss.ugc.android.editor.base.utils.SizeUtil

/**
 *
 *
 * @version 1.0
 * @since 2019/4/15 11:42 PM
 */
class SelectFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val strokePadding = SizeUtil.dp2px(18F)
    private val boxes: MutableList<RectF> = mutableListOf()

    private val pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0F)
    private val shadowColor = Color.parseColor("#66000000")

    companion object{
        var BORDERCOLOR = Color.WHITE
        var STROKEWIDTH = SizeUtil.dp2px(1F).toFloat()
    }

    fun setRectColor(@ColorInt color: Int){
        BORDERCOLOR = color
    }
    fun setRectStrokeWidth(float: Float){
        STROKEWIDTH = float
    }

    init {
        paint.color = BORDERCOLOR
        paint.isDither = true
        paint.style = Paint.Style.STROKE
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        paint.strokeWidth = STROKEWIDTH
        paint.setShadowLayer(2 * STROKEWIDTH, 0F, 0F, Color.parseColor("#4C000000"))
        setWillNotDraw(false)
    }

    fun setPaintColor(@ColorInt color :Int){
        Log.e("dfdfgfg--setPaintColor",color.toString())
        paint.color = color

    }

    fun setPaintStrokeWidth(float: Float){
        paint.strokeWidth = SizeUtil.dp2px(float).toFloat()

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.pathEffect = null
        canvas?.drawRect(
            strokePadding,
            strokePadding,
            measuredWidth - strokePadding,
            measuredHeight - strokePadding,
            paint
        )
        paint.pathEffect = pathEffect
        paint.setShadowLayer(5F, 0F, 0F, shadowColor)
        val adjustRotate = rotation != 0f//有旋转角度时需要矫正下旋转中心点
        //逆旋转view本身旋转角度，因为他是根据view中心点旋转的，而文本框是基于文本本身中心点
        if (adjustRotate) {
            canvas?.save()
            canvas?.rotate(-rotation, width / 2f, height / 2f)
        }
        boxes.forEach {
            //重新针对每个文字框旋转，旋转中心点是方框的中心点
            if (adjustRotate) {
                canvas?.save()
                canvas?.rotate(rotation, it.centerX(), it.centerY())
            }
            canvas?.drawRect(it, paint)
            if (adjustRotate) canvas?.restore()
        }
        if (adjustRotate) canvas?.restore()
        paint.setShadowLayer(0F, 0F, 0F, Color.TRANSPARENT)
    }

    /**
     * [textBounds] rectF 相对于画布的宽高,画布和父布局宽高相等
     * 这里 draw 之前把相对于父布局的坐标先转换成相对于自身的宽高
     */
    fun setTextItemRect(textBounds: List<RectF>) {
        boxes.clear()
        boxes.addAll(textBounds.map {
            val layoutParams = layoutParams as LayoutParams
            val l = it.left - layoutParams.leftMargin
            val t = it.top - layoutParams.topMargin
            RectF(l, t, l + it.width(), t + it.height())
        })
        invalidate()
    }

    private fun Canvas.drawRect(left: Int, top: Int, right: Int, bottom: Int, paint: Paint) {
        drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
    }
}
