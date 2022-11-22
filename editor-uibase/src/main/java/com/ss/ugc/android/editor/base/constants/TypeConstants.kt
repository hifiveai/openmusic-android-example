package com.ss.ugc.android.editor.base.constants

/**
 * @date: 2021/4/6
 */
class TypeConstants {

    companion object{
        @JvmStatic
        var OFFSET = 16
        @JvmStatic
        var MASK = 0xFFFF.inv()
        @JvmStatic
        var SUB_OFFSET = 8
        @JvmStatic
        var SUB_MASK = 0xFF.inv()

        // 一级菜单
        //The second menu

        // 一级菜单
        //The second menu
        @JvmStatic
        var TYPE_CLOSE = -1

        // Beautify face 美颜
        @JvmStatic
        var TYPE_BEAUTY_FACE = 1 shl OFFSET

        // Beautify reshape 美型
        @JvmStatic
        var TYPE_BEAUTY_RESHAPE = 2 shl OFFSET

        // Beautify body 美体
        @JvmStatic
        var TYPE_BEAUTY_BODY = 3 shl OFFSET

        // Makeup 美妆
        @JvmStatic
        var TYPE_MAKEUP = 4 shl OFFSET

        // Filiter 滤镜
        @JvmStatic
        var TYPE_FILTER = 5 shl OFFSET

        @JvmStatic
        var TYPE_RATIO = 6 shl OFFSET

        @JvmStatic
        var TYPE_CUT = 7 shl OFFSET

        @JvmStatic
        var TYPE_ADJUST = 8 shl OFFSET

        @JvmStatic
        var TYPE_ADJUST_LD = TYPE_ADJUST + (1 shl SUB_OFFSET)

        @JvmStatic
        var TYPE_ADJUST_DBD = TYPE_ADJUST + (2 shl SUB_OFFSET)
        @JvmStatic
        var TYPE_ADJUST_SW = TYPE_ADJUST + (3 shl SUB_OFFSET)
        @JvmStatic
        var TYPE_ADJUST_BHD = TYPE_ADJUST + (4 shl SUB_OFFSET)
        @JvmStatic
        var TYPE_ADJUST_TS = TYPE_ADJUST + (5 shl SUB_OFFSET)
        @JvmStatic
        var TYPE_ADJUST_GG = TYPE_ADJUST + (6 shl SUB_OFFSET)
        @JvmStatic
        var TYPE_ADJUST_YY = TYPE_ADJUST + (7 shl SUB_OFFSET)
        @JvmStatic
        var TYPE_ADJUST_AJ = TYPE_ADJUST + (8 shl SUB_OFFSET)
        @JvmStatic
        var TYPE_ADJUST_RH = TYPE_ADJUST + (9 shl SUB_OFFSET)
        @JvmStatic
        var TYPE_ADJUST_SD = TYPE_ADJUST + (10 shl SUB_OFFSET)
        @JvmStatic
        var TYPE_ADJUST_BGD = TYPE_ADJUST + (11 shl SUB_OFFSET)


    }
}