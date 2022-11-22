package com.ss.ugc.android.editor.bottom.panel.sticker.text

import com.bytedance.ies.nle.editor_jni.NLEStyStickerAnim

const val ANIMATION_TYPE_NONE = 0
const val ANIMATION_TYPE_IN = 1
const val ANIMATION_TYPE_OUT = 2
const val ANIMATION_TYPE_INANDOUT = 3
const val ANIMATION_TYPE_LOOP = 4

fun NLEStyStickerAnim?.getAnimationType(): Int {
    return this?.let {
        if (it.loop
            && it.inAnim != null
            && it.inAnim.hasResourceFile()
        ) {
            // 如果是循环动画
            ANIMATION_TYPE_LOOP
        } else if (!it.loop
            && it.inAnim != null
            && it.inAnim.hasResourceFile()
            && (it.outAnim == null || !it.outAnim.hasResourceFile())
        ) {
            // 如果只有入场动画
            ANIMATION_TYPE_IN
        } else if (!it.loop
            && it.outAnim != null
            && it.outAnim.hasResourceFile()
            && (it.inAnim == null || !it.inAnim.hasResourceFile())
        ) {
            ANIMATION_TYPE_OUT
        } else if (!it.loop
            && it.inAnim != null
            && it.inAnim.hasResourceFile()
            && it.outAnim != null
            && it.outAnim.hasResourceFile()
        ) {
            // 如果是入场&出场动画
            ANIMATION_TYPE_INANDOUT
        } else {
            ANIMATION_TYPE_NONE
        }
    } ?: ANIMATION_TYPE_NONE
}