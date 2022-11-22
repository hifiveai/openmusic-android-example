package com.ss.ugc.android.editor.bottom.panel.sticker.text

import kotlin.math.cos
import kotlin.math.sin

data class ShadowPoint(
    val x: Float = 0F,
    val y: Float = 0F
) {
    companion object {
        fun transientValue(shadowDistance: Float, shadowAngle: Float): ShadowPoint {
            val angleArgs = getRealAngle(shadowAngle)
            return ShadowPoint(
                (cos(angleArgs.first) * shadowDistance / 5.55 * angleArgs.second).toFloat(),
                (sin(angleArgs.first) * shadowDistance / 5.55 * angleArgs.third).toFloat()
            )
        }

        fun transientDistance(shadowAngle: Float, x: Float): Float {
            val angleArgs = getRealAngle(shadowAngle)
            return (x / cos(angleArgs.first) * 5.55 / angleArgs.second).toFloat()
        }

        private fun getRealAngle(shadowAngle: Float): Triple<Double, Int, Int> {
            return when {
                shadowAngle > 0 && shadowAngle <= 90 -> {
                    // 第一象限
                    Triple(shadowAngle * Math.PI / 180, 1, 1)
                }
                shadowAngle > 90 && shadowAngle <= 180 -> {
                    // 第二象限
                    Triple((180 - shadowAngle) * Math.PI / 180, -1, 1)
                }
                shadowAngle >= -180 && shadowAngle <= -90 -> {
                    // 第三象限
                    Triple((180 + shadowAngle) * Math.PI / 180, -1, -1)
                }
                shadowAngle > -90 && shadowAngle <= 0 -> {
                    // 第四象限
                    Triple((shadowAngle * -1) * Math.PI / 180, 1, -1)
                }
                else -> {
                    Triple(0.0, 0, 0)
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShadowPoint

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}