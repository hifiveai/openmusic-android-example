package com.ss.ugc.android.editor.preview.adjust

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @since 17/6/13
 * @author joffychim
 * 非空，只能赋值一次
 */
internal class NotNullSingleValueVar<T> : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = if (this.value == null) value
        else return/*throw IllegalStateException(property.name + " is already initialized!")*/
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException(property.name + " is not initialized!")
    }
}