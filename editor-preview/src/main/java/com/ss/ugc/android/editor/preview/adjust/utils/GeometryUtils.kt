package com.ss.ugc.android.editor.preview.adjust.utils

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import java.lang.StringBuilder
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class GeometryUtils {

    companion object {
        fun isRectContainsRect(src: RectF, dst: RectF): Boolean {
            val srcPointList =
                rect2PointList(
                    src
                )
            val dstPointList =
                rect2PointList(
                    dst
                )
            val values =
                calPolyContainsPoly(
                    srcPointList,
                    dstPointList
                )
            var ret = true
            values.forEach {
                ret = it && ret
            }
            return ret
        }

        fun isPolyContainsPoly(srcList: List<Point>, dstList: List<Point>): Boolean {
            val values =
                calPolyContainsPoly(
                    srcList,
                    dstList
                )
            var ret = true
            val sb = StringBuilder()
            values.forEach {
                sb.append("$it ")
                ret = it && ret
            }
            if (ret.not()) {
//                BLog.w("sliver", "isPolyContainsPoly,$sb  values:$values")
            }
            return ret
        }

        fun calPolyContainsPoly(srcList: List<Point>, dstList: List<Point>): List<Boolean> {
            val retList = mutableListOf<Boolean>()
            dstList.forEach {
                val ret =
                    isPolyContainsPoint(
                        it,
                        srcList
                    )
                retList.add(ret)
            }
            return retList
        }

        private fun isPolyContainsPoint(dst: Point, poly: List<Point>): Boolean {
            return rayArithmetic(
                dst,
                poly
            )
        }

        /**
         * 射线穿过多边形边界的次数为奇数时点在多边形内
         */
        private fun rayArithmetic(dst: Point, poly: List<Point>): Boolean {
            var flag = false
            val list =
                calcXRayCastingDis(
                    dst,
                    poly
                )
            list?.let {
                if (it.isEmpty()) {
                    return false
                }

                it.forEach { value ->
                    if (value > 0) {
                        flag = !flag
                    }
                }
            }

            if (list == null) {
                return true
            }
            return flag
        }

        fun calcXRayCastingDis(p: Point, poly: List<Point>): List<Float>? {
            val px = p.x.toFloat()
            val py = p.y.toFloat()

            var i = 0
            val len = poly.size
            var j = len - 1
            var list: MutableList<Float>? = mutableListOf()
            while (i < len) {
                val p1x = poly[i].x.toFloat()
                val p1y = poly[i].y.toFloat()
                val p2x = poly[j].x.toFloat()
                val p2y = poly[j].y.toFloat()

                // 点与多边形顶点重合
                if ((p1x == px && p1y == py) || (p2x == px && p2y == py)) {
                    return null
                }

                val minX = min(poly[i].x, poly[j].x)
                val maxX = max(poly[i].x, poly[j].x)
                // 点在线上
                if ((py == p1y && py == p2y) && p.x in minX..maxX) {
                    return null
                }

                // 判断线段两端点是否在射线两侧
                if ((p1y < py && p2y >= py) || (p1y >= py && p2y < py)) {
                    // 线段上与射线 Y 坐标相同的点的 X 坐标
                    val x = p1x + (py - p1y) * (p2x - p1x) / (p2y - p1y)

                    // 点在多边形的边上
                    if (x == px) {
                        return null
                    }

                    // 射线穿过多边形的边界
                    list?.add(x - px)
                }

                j = i
                i++
            }
            return list
        }

//        fun calcXRayCastingDis(p: Point, poly: List<Point>): List<Float>? {
//            val px = BigDecimal(p.x)
//            val py = BigDecimal(p.y)
//
//            var i = 0
//            val len = poly.size
//            var j = len - 1
//            var list: MutableList<Float>? = mutableListOf()
//            while (i < len) {
//                val p1x = BigDecimal(poly[i].x)
//                val p1y = BigDecimal(poly[i].y)
//                val p2x = BigDecimal(poly[j].x)
//                val p2y = BigDecimal(poly[j].y)
//
//                // 点与多边形顶点重合
//                if ((p1x == px && p1y == py) || (p2x == px && p2y == py)) {
//                    return null
//                }
//
//                val minX = min(poly[i].x, poly[j].x)
//                val maxX = max(poly[i].x, poly[j].x)
//                // 点在线上
//                if ((py == p1y && py == p2y) && p.x in minX..maxX) {
//                    return null
//                }
//
//                // 判断线段两端点是否在射线两侧
//                if ((p1y < py && p2y >= py) || (p1y >= py && p2y < py)) {
//                    // 线段上与射线 Y 坐标相同的点的 X 坐标
//                    val x = p1x + (py - p1y) * (p2x - p1x) / (p2y - p1y)
//
//                    // 点在多边形的边上
//                    if (x == px) {
//                        return null
//                    }
//
//                    // 射线穿过多边形的边界
//                    list?.add((x - px).toFloat())
//                }
//
//                j = i
//                i++
//            }
//            return list
//        }

//        fun calcYRayCastingDis(p: Point, poly: List<Point>): List<Float>? {
//            val px = BigDecimal(p.x)
//            val py = BigDecimal(p.y)
//
//            var i = 0
//            val len = poly.size
//            var j = len - 1
//            var list: MutableList<Float>? = mutableListOf()
//            while (i < len) {
//                val p1x = BigDecimal(poly[i].x)
//                val p1y = BigDecimal(poly[i].y)
//                val p2x = BigDecimal(poly[j].x)
//                val p2y = BigDecimal(poly[j].y)
//
//                // 点与多边形顶点重合
//                if ((p1x == px && p1y == py) || (p2x == px && p2y == py)) {
//                    return null
//                }
//
//                val minY = min(poly[i].y, poly[j].y)
//                val maxY = max(poly[i].y, poly[j].y)
//                // 点在线上
//                if ((px == p1x && px == p2x) && p.y in minY..maxY) {
//                    return null
//                }
//
//                // 判断线段两端点是否在射线两侧
//                if ((p1x < px && p2x >= px) || (p1x >= px && p2x < px)) {
//                    // 线段上与射线 Y 坐标相同的点的 X 坐标
//                    val y = p1y + (px - p1x) * (p2y - p1y) / (p2x - p1x)
//
//                    // 点在多边形的边上
//                    if (y == py) {
//                        return null
//                    }
//
//                    // 射线穿过多边形的边界
//                    list?.add((y - py).toFloat())
//                }
//
//                j = i
//                i++
//            }
//            return list
//        }

        fun calcYRayCastingDis(p: Point, poly: List<Point>): List<Float>? {
            val px = p.x.toFloat()
            val py = p.y.toFloat()

            var i = 0
            val len = poly.size
            var j = len - 1
            var list: MutableList<Float>? = mutableListOf()
            while (i < len) {
                val p1x = poly[i].x.toFloat()
                val p1y = poly[i].y.toFloat()
                val p2x = poly[j].x.toFloat()
                val p2y = poly[j].y.toFloat()

                // 点与多边形顶点重合
                if ((p1x == px && p1y == py) || (p2x == px && p2y == py)) {
                    return null
                }

                val minY = min(poly[i].y, poly[j].y)
                val maxY = max(poly[i].y, poly[j].y)
                // 点在线上
                if ((px == p1x && px == p2x) && p.y in minY..maxY) {
                    return null
                }

                // 判断线段两端点是否在射线两侧
                if ((p1x < px && p2x >= px) || (p1x >= px && p2x < px)) {
                    // 线段上与射线 Y 坐标相同的点的 X 坐标
                    val y = p1y + (px - p1x) * (p2y - p1y) / (p2x - p1x)

                    // 点在多边形的边上
                    if (y == py) {
                        return null
                    }

                    // 射线穿过多边形的边界
                    list?.add(y - py)
                }

                j = i
                i++
            }
            return list
        }

        fun rect2PointList(rect: RectF): List<Point> {
            val leftTop = Point(rect.left.toInt(), rect.top.toInt())
            val topRight = Point(rect.right.toInt(), rect.top.toInt())
            val rightBottom = Point(rect.right.toInt(), rect.bottom.toInt())
            val leftBottom = Point(rect.left.toInt(), rect.bottom.toInt())

            return listOf(leftTop, topRight, rightBottom, leftBottom)
        }

        fun rect2PointList(rect: Rect): List<Point> {
            val leftTop = Point(rect.left, rect.top)
            val topRight = Point(rect.right, rect.top)
            val rightBottom = Point(rect.right, rect.bottom)
            val leftBottom = Point(rect.left, rect.bottom)

            return listOf(leftTop, topRight, rightBottom, leftBottom)
        }

        fun getRotateWithMinScale(
            internal: RectF,
            externalRectF: RectF,
            internalCenter: PointF,
            externalCenter: PointF,
            angle: Float
        ): Float {
            val h = internal.height()
            val w = internal.width()
            val H = externalRectF.height()
            val W = externalRectF.width()
            val radian = Math.toRadians(angle.toDouble())
            val cos = abs(cos(radian))
            val sin = abs(sin(radian))
            val scaleW = (w * cos + h * sin + 2 * (
                    abs(internalCenter.y - externalCenter.y) * sin + abs(internalCenter.x - externalCenter.x) * cos
                    )) / W
            val scaleH = (h * cos + w * sin + 2 * (
                    abs(internalCenter.y - externalCenter.y) * cos + abs(internalCenter.x - externalCenter.x) * sin
                    )) / H
            return max(scaleH, scaleW).toFloat()
        }
    }
}
