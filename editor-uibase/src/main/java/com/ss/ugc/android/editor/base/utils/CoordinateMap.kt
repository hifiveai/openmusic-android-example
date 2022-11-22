package com.ss.ugc.android.editor.base.utils

/**
 * time : 2020/12/24
 *
 * description :
 * 贴纸
 * NLE & VE 坐标转换
 *
 * NLE Coordinate:
 * * Information sticker coordinate system:
 *
 *                                (0，1)
 *       Screen or DrawBoard    ^ Y axis
 *      (-1.0, 1.0) ------------|-------------    (1, 1)
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *         (-1, 0) |            | origin(0,0)|
 *                 |--------------------------> X (1, 0)
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *         (-1,-1) |            |            |    (1,-1)
 *                 -------------|-------------
 *                               (0, -1)
 *-------------------------------------------------------------------
 *
 *  * VE Coordinate:
 * * Information sticker coordinate system:
 *
 *                                (0.5，0)
 *       Screen or DrawBoard
 *           (0, 0) ------------|-------------    (1, 0)
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *        (0, 0.5) |            | origin(0.5,0.5)|
 *                 |--------------------------> y (1, 0.5)
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *                 |            |            |
 *           (0,1) |            |            |    (1,1)
 *                 -------------|-------------
 *                              v X axis
 *                               (0.5, 1)
 *
 */

fun Float.toVeX(): Float {
    return this / 2 + 0.5f
}

fun Float.toVeY(): Float {
    return 0.5f - this / 2
}

fun Float.toNleX(): Float {
    return (this - 0.5f) * 2
}

fun Float.toNleY(): Float {
    return (0.5f - this) * 2
}

