package com.ss.ugc.android.editor.bottom.theme

import com.ss.ugc.android.editor.base.theme.BottomUIConfig

/**
 * @date: 2021/3/31
 *
 * config bottom bar
 *
 * 1）function item list, completely use your own function item list, this will replace the default list
 * 2) UI config.
 */
class BottomPanelConfig private constructor(builder: Builder) {

    var themeConfigure = builder.themeConfigure

    class Builder {

        var themeConfigure: BottomUIConfig? = null

        fun setBottomPanelConfig(themeConfigure: BottomUIConfig) = apply {
            this.themeConfigure = themeConfigure
        }

        fun build(): BottomPanelConfig {
            return BottomPanelConfig(this)
        }
    }
}