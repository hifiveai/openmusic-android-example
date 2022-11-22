package com.ss.ugc.android.editor.preview.adjust

import kotlin.properties.ReadWriteProperty

/**
 * @since 17/6/13
 * @author joffychim
 */
object Delegate {
    fun <T> notNullSingleValue(): ReadWriteProperty<Any?, T> = NotNullSingleValueVar()
}