package com.ss.ugc.android.editor.bottom.panel.speed

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.bottom.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class CurveSpeedView : View{
    private var curvePoints: MutableList<PointF> = mutableListOf()
    private var viewPoints: MutableList<PointF> = mutableListOf()
    private var currentPlayX = 0F
    private var tempPoint = PointF()
    var selectPointIndex = 0
        set(value) {
            field = value
            if(selectPointIndex == 0 || selectPointIndex == viewPoints.size-1){
                if(editMode != EDIT_POINT_MODE_DISABLE){
                    changeEditPointMode(EDIT_POINT_MODE_DISABLE)
                }
            } else if(selectPointIndex == -1) {
                if(editMode != EDIT_POINT_MODE_ADD){
                    changeEditPointMode(EDIT_POINT_MODE_ADD)
                }
            } else {
                if(editMode != EDIT_POINT_MODE_DELETE){
                    changeEditPointMode(EDIT_POINT_MODE_DELETE)
                }
            }
        }

    private var editMode = -1
    var editModeChange: ((Int) -> Unit)? = null
    var pointListChange: ((Float, List<PointF>) -> Unit)? = null
    var progressChange:((Float, Int) -> Unit)? = null

    private var path = Path()
    private var horizontalPadding = SizeUtil.dp2px(10F)
    private var verticalPadding = SizeUtil.dp2px(10F)
    //外框
    private var rectBoxRect = RectF()
    private var rectBoxPaint = Paint().apply {
        color = resources.getColor(R.color.transparent_20p_white)
        strokeWidth = SizeUtil.dp2px(1F).toFloat()
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    
    //辅助线
    private var assistPathPaint = Paint().apply {
        color = resources.getColor(R.color.transparent_20p_white)
        strokeWidth = SizeUtil.dp2px(1F).toFloat()
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    //文本
    val textRect = Rect()
    private var textPaint = Paint().apply {
        color = resources.getColor(R.color.transparent_50p_white)
        textSize = SizeUtil.dp2px(10F).toFloat()
        isAntiAlias = true
    }
    //贝塞尔曲线
    // 起始点
    private var startPoint = PointF()
    // 结束点
    private var endPoint = PointF()
    //下贝塞尔控制点
    private var downBezierControlPoint = PointF()
    //上贝塞尔控制点
    private var upBezierControlPoint = PointF()
    private var curvePathPaint = Paint().apply {
        color = resources.getColor(R.color.tab_red)
        strokeWidth = SizeUtil.dp2px(1F).toFloat()
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    //控制点
    private var touchPointIndex = -1
    private var controlPointRect = RectF()
    private var controlPointRadius = SizeUtil.dp2px(9F).toFloat()
    private var controlPointPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = SizeUtil.dp2px(2F).toFloat()
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    //控制光标
    private var controlPathPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = SizeUtil.dp2px(2F).toFloat()
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    companion object {
        const val EDIT_POINT_MODE_ADD = 1
        const val EDIT_POINT_MODE_DELETE = 2
        const val EDIT_POINT_MODE_DISABLE = 3
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val text = "10 x"
        textPaint.getTextBounds(text, 0, text.length, textRect)
        rectBoxRect.set(
            horizontalPadding.toFloat(),
            verticalPadding.toFloat(),
            (width - horizontalPadding).toFloat(),
            (height - verticalPadding).toFloat()
        )
        currentPlayX = rectBoxRect.left
        curvePoints2ViewPoints()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        //绘制外框
        canvas.drawRect(rectBoxRect, rectBoxPaint)
        //绘制辅助虚线
        drawAssistPath(canvas)
        //绘制文字
        drawText(canvas)
        drawCurvePath(canvas)
        //绘制控制点
        drawControlPoints(canvas)
        //绘制控制光标
        drawControlPath(canvas)
    }

    private fun drawAssistPath(canvas: Canvas){
        drawDashLine(
            canvas,
            rectBoxRect.left,
            verticalPadding + rectBoxRect.height() / 4.0F,
            rectBoxRect.right,
            verticalPadding + rectBoxRect.height() / 4.0F,
            (rectBoxRect.width() / SizeUtil.dp2px(6F)).toInt(),
            SizeUtil.dp2px(3F).toFloat(),
            path,
            assistPathPaint
        )
        drawDashLine(
            canvas,
            rectBoxRect.left,
            verticalPadding + rectBoxRect.height() / 2.0F,
            rectBoxRect.right,
            verticalPadding + rectBoxRect.height() / 2.0F,
            (rectBoxRect.width() / SizeUtil.dp2px(6F)).toInt(),
            SizeUtil.dp2px(3F).toFloat(),
            path,
            assistPathPaint
        )

        drawDashLine(
            canvas,
            rectBoxRect.left,
            verticalPadding + rectBoxRect.height() / 4.0F * 3,
            rectBoxRect.right,
            verticalPadding + rectBoxRect.height() / 4.0F * 3,
            (rectBoxRect.width() / SizeUtil.dp2px(6F)).toInt(),
            SizeUtil.dp2px(3F).toFloat(),
            path,
            assistPathPaint
        )
    }

    private fun drawDashLine(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        dividerCount: Int,
        emptyX: Float,
        path: Path,
        paint: Paint
    ) {
        path.reset()
        val width = endX - startX
        val dividerWidth = width / dividerCount
        for (i in 0..dividerCount) {
            val dividerStart = startX + i * (emptyX + dividerWidth)
            if (dividerStart >= endX) {
                path.lineTo(endX, endY)
                break
            }
            path.moveTo(dividerStart, startY)
            path.lineTo((dividerStart + dividerWidth).coerceAtMost(endX), startY)
        }
        canvas.drawPath(path, paint)
    }

    private fun drawText(canvas: Canvas){
        canvas.drawText(
            "10 X",
            rectBoxRect.left + SizeUtil.dp2px(6F),
            rectBoxRect.top + SizeUtil.dp2px(6F) + textRect.height(),
            textPaint
        )

        canvas.drawText(
            "0.1 X",
            rectBoxRect.left + SizeUtil.dp2px(6F),
            verticalPadding + rectBoxRect.height() - SizeUtil.dp2px(6F),
            textPaint
        )
    }

    private fun drawCurvePath(canvas: Canvas){
        path.reset()
        viewPoints.forEachIndexed { index, pointF ->
            if (index + 1 >= viewPoints.size) {
                return@forEachIndexed
            }
            val nextPointF = viewPoints[index + 1]
            if (pointF.y > nextPointF.y) {
                startPoint = pointF
                endPoint = nextPointF
            } else {
                startPoint = nextPointF
                endPoint = pointF
            }
            downBezierControlPoint.set((startPoint.x + endPoint.x) / 2, startPoint.y)
            upBezierControlPoint.set((startPoint.x + endPoint.x) / 2, endPoint.y)
            path.moveTo(startPoint.x, startPoint.y)
            path.cubicTo(
                downBezierControlPoint.x,
                downBezierControlPoint.y,
                upBezierControlPoint.x,
                upBezierControlPoint.y,
                endPoint.x,
                endPoint.y
            )
        }
        canvas.drawPath(path, curvePathPaint)
    }

    private fun drawControlPoints(canvas: Canvas){
        viewPoints.forEachIndexed { index, pointF ->
            if (index != touchPointIndex && index != selectPointIndex) {
                controlPointPaint.color = Color.WHITE
                canvas.drawCircle(pointF.x, pointF.y, controlPointRadius, controlPointPaint)
                controlPointPaint.color = Color.BLACK
                canvas.drawCircle(
                    pointF.x,
                    pointF.y,
                    controlPointRadius - SizeUtil.dp2px(2F),
                    controlPointPaint
                )
            }else{
                controlPointPaint.color = resources.getColor(R.color.tab_red)
                canvas.drawCircle(pointF.x, pointF.y, controlPointRadius, controlPointPaint)
            }
        }
    }

    private fun drawControlPath(canvas: Canvas){
        canvas.drawLine(
            currentPlayX,
            rectBoxRect.top,
            currentPlayX,
            rectBoxRect.bottom,
            controlPathPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchPointIndex = touchPointIndex(event.x, event.y)
                if (touchPointIndex >= 0) {
                    selectPointIndex = touchPointIndex
                }
                val progress = (currentPlayX - horizontalPadding) / rectBoxRect.width()
                progressChange?.invoke(progress, touchPointIndex)
                parent.requestDisallowInterceptTouchEvent(true)
                currentPlayX = min(rectBoxRect.right, max(rectBoxRect.left, event.x))
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                currentPlayX = min(rectBoxRect.right, max(rectBoxRect.left, event.x))
                if (touchPointIndex >= 0 && touchPointIndex < viewPoints.size) {
                    val pointF = viewPoints[touchPointIndex]
                    validMove(event.x, event.y, pointF)
                    currentPlayX = pointF.x
                } else {
                    selectPointIndex = viewPoints.indexOfFirst {
                        it.x - controlPointRadius <= currentPlayX && it.x + controlPointRadius >= currentPlayX
                    }
                }
                val progress = (currentPlayX - horizontalPadding) / rectBoxRect.width()
                progressChange?.invoke(progress, touchPointIndex)
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (touchPointIndex >= 0) {
                    viewPoints2CurvePoints()
                }
                touchPointIndex = -1
                selectPointIndex = viewPoints.indexOfFirst {
                    it.x - controlPointRadius <= currentPlayX && it.x + controlPointRadius >= currentPlayX
                }
                invalidate()
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        return true
    }

    /**
     * 检查拖动合法性
     */
    private fun validMove(x: Float, y: Float, pointF: PointF) {
        // point x,y 需要在框内
        pointF.x = min(max(x, rectBoxRect.left), rectBoxRect.right)
        pointF.y = min(max(y, rectBoxRect.top), rectBoxRect.bottom)
        // 最前最后两个点不可左右移动
        when (touchPointIndex) {
            0 -> pointF.x = rectBoxRect.left
            viewPoints.size - 1 -> pointF.x = rectBoxRect.right
            else -> {
                val prePoint = viewPoints[touchPointIndex - 1]
                val nextPoint = viewPoints[touchPointIndex + 1]
                pointF.x = min(nextPoint.x - controlPointRadius, max(prePoint.x + controlPointRadius, x))
            }
        }
    }

    /**
     *  判断当前点击的点是哪个点
     *  @return -1 为未选中
     */
    private fun touchPointIndex(x: Float, y: Float): Int {
        viewPoints.forEachIndexed { index, pointF ->
            controlPointRect.set(
                pointF.x - controlPointRadius * 1.5F,
                pointF.y - controlPointRadius * 1.5F,
                pointF.x + controlPointRadius * 1.5F,
                pointF.y + controlPointRadius * 1.5F
            )
            if (controlPointRect.contains(x, y)) {
                return index
            }
        }
        return -1
    }

    fun setPoints(points: List<PointF>) {
        selectPointIndex = 0
        currentPlayX = rectBoxRect.left
        curvePoints.clear()
        curvePoints.addAll(points)
        curvePoints.sortBy { it.x }
        curvePoints2ViewPoints()
        invalidate()
    }

    private fun curvePoints2ViewPoints() {
        viewPoints.clear()
        curvePoints.forEach { pointF ->
            val viewPointF = PointF()
            curvePoint2ViewPoint(pointF, viewPointF)
            viewPoints.add(viewPointF)
        }
    }

    private fun curvePoint2ViewPoint(curvePoint: PointF, viewPoint: PointF){
        val circleX = curvePoint.x * rectBoxRect.width() + horizontalPadding
        val circleY = if (curvePoint.y >= 1) {
            (verticalPadding + rectBoxRect.height() / 2.0 - (curvePoint.y - 1) / 9.0 * rectBoxRect.height() / 2.0).toFloat()
        } else {
            (verticalPadding + rectBoxRect.height() / 2.0 + (1 - curvePoint.y) / 0.9 * rectBoxRect.height() / 2.0).toFloat()
        }
        viewPoint.set(circleX, circleY)
    }

    private fun viewPoints2CurvePoints() {
        curvePoints.clear()
        viewPoints.forEach { pointF ->
            val absolutePointF = PointF()
            viewPoint2CurvePoint(pointF, absolutePointF)
            curvePoints.add(absolutePointF)
        }
        val progress = (currentPlayX - horizontalPadding) / rectBoxRect.width()
        pointListChange?.invoke(progress, curvePoints)
    }

    private fun viewPoint2CurvePoint(viewPoint: PointF, curvePoint: PointF) {
        val pointX = (viewPoint.x - horizontalPadding) / rectBoxRect.width()
        val pointY = if (viewPoint.y >= rectBoxRect.height() / 2.0 + verticalPadding) {
            ((viewPoint.y - verticalPadding - rectBoxRect.height() / 2.0) / (rectBoxRect.height() / 2.0) * (-0.9) + 1).toFloat()
        } else {
            ((rectBoxRect.height() / 2.0 + verticalPadding - viewPoint.y) / (rectBoxRect.height() / 2.0) * 9.0 + 1).toFloat()
        }
        curvePoint.set(pointX, pointY)
    }

    private fun changeEditPointMode(mode: Int){
        this.editMode = mode
        editModeChange?.invoke(mode)
    }

    fun getEditPointMode(): Int{
        return editMode
    }

    fun addControlPoint(){
        val index = viewPoints.indexOfFirst {
            it.x >= currentPlayX
        }
        if (index > 0 && index < viewPoints.size) {
            val startPointF = viewPoints[index - 1]
            val endPointF = viewPoints[index]
            // 绘制方向为 从 y 值大的点开始绘制，所以 t 也需要按照开始绘制方向计算
            val t = if (startPointF.y > endPointF.y) {
                (currentPlayX - startPointF.x) / (endPointF.x - startPointF.x)
            } else {
                (endPointF.x - currentPlayX) / (endPointF.x - startPointF.x)
            }
            val y = getCubicBezierY(max(startPointF.y, endPointF.y), min(startPointF.y, endPointF.y), t)
            val addPointF = PointF(currentPlayX, y)
            viewPoints.add(index, addPointF)
            selectPointIndex = index
            viewPoints2CurvePoints()
            invalidate()
        }
    }

    fun deleteControlPoint(){
        if (selectPointIndex > 0 && selectPointIndex < viewPoints.size) {
            viewPoints.removeAt(selectPointIndex)
            selectPointIndex = viewPoints.indexOfFirst {
                it.x - controlPointRadius <= currentPlayX && it.x + controlPointRadius >= currentPlayX
            }
            viewPoints2CurvePoints()
            invalidate()
        }
    }

    private fun getCubicBezierY(startPointY: Float, endPointY: Float, t: Float): Float {
        val p0 = startPointY
        val p1 = startPointY
        val p2 = endPointY
        val p3 = endPointY
        val y =
            (1 - t).pow(3) * p0 +
                    3 * (1 - t).pow(2) * t * p1 +
                    3 * (1 - t) * t.pow(2) * p2 +
                    t.pow(3) * p3
        return y
    }

    fun getPointSpeed(index: Int): Float {
        if (index >= 0 && index <= viewPoints.size - 1) {
            viewPoint2CurvePoint(viewPoints[index], tempPoint)
            return tempPoint.y
        }
        return 1.0F
    }

    fun setPlayProgress(progress: Float) {
        currentPlayX = horizontalPadding + progress * rectBoxRect.width()
        selectPointIndex = viewPoints.indexOfFirst {
            it.x - controlPointRadius <= currentPlayX && it.x + controlPointRadius >= currentPlayX
        }
        invalidate()
    }
}