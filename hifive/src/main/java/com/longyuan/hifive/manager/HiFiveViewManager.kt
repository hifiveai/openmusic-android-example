package com.longyuan.hifive.manager

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.longyuan.hifive.HiFiveConfig
import com.longyuan.hifive.R
import com.longyuan.hifive.ui.fragment.HiFiveSongMenuFragment
import com.ss.ugc.android.editor.picker.utils.ScreenUtils

/**
 * #duxiaxing
 * #date: 2022/8/8
 */
internal object HiFiveViewManager {
    /**
     * 初始化歌单ViewPager设置
     */
    fun initMenu(vp: ViewPager) : List<Fragment>{
        val menuItemWith = (ScreenUtils.getScreenWidth(vp.context) - ScreenUtils.dp2px(vp.context,64f))/3
        val menuHeight = menuItemWith + ScreenUtils.dp2px(vp.context,56f)
        vp.visibility = when{
            HiFiveConfig.musicMenuData.size < 1 -> View.GONE
            else -> View.VISIBLE
        }
        when{
            HiFiveConfig.musicMenuData.size == 0 -> {}
            HiFiveConfig.musicMenuData.size < 4 -> { vp.layoutParams.height = menuHeight }
            HiFiveConfig.musicMenuData.size < 7 -> { vp.layoutParams.height = menuHeight*2 }
            else -> { vp.layoutParams.height = menuHeight*3 }
        }
        val fragments = arrayListOf<Fragment>()
        for (i in 0 until HiFiveConfig.musicMenu.size){
            fragments.add(HiFiveSongMenuFragment.createMenuView(i))
        }
        return fragments
    }

    /**
     * 初始化与歌单ViewPager关联的指示器
     */
    fun initPoint(linearLayout: LinearLayout, size : Int) {
        linearLayout.removeAllViews()
        linearLayout.apply {
            addView(createPoint(linearLayout.context,true))
            for (i in 1 until size){
                addView(createPoint(linearLayout.context,false))
            }
        }
    }

    /**
     * 创建指示器View
     */
    private fun createPoint(context: Context, isSelect : Boolean) : View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ScreenUtils.dp2px(context,6f),
                ScreenUtils.dp2px(context,6f)).apply {
                marginStart = ScreenUtils.dp2px(context,2f)
                marginEnd = ScreenUtils.dp2px(context,2f)
            }
            setBackgroundResource(if (isSelect) R.drawable.hifive_menu_point_select else R.drawable.hifive_menu_point_unselect)
        }
    }

    /**
     * 设置选中的指示器
     */
    fun setIndicator(linearLayout: LinearLayout, position : Int){
        for (i in 0 until linearLayout.childCount){
            linearLayout.getChildAt(i).setBackgroundResource(
                if (i == position) R.drawable.hifive_menu_point_select else R.drawable.hifive_menu_point_unselect)
        }
    }

    /**
     * 设置TextView状态
     */
    fun updateTypeView(tv1 : TextView, tv2 : TextView){
        tv1.textSize = 22f
        tv1.setTextColor(Color.WHITE)
        tv2.textSize = 20f
        tv2.setTextColor(Color.parseColor("#a3ffffff"))
    }
}