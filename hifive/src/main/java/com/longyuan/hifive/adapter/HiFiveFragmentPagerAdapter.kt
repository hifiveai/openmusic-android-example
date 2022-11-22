package com.longyuan.hifive.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


/**
 * #duxiaxing
 * #date: 2022/8/8
 */
class HiFiveFragmentPagerAdapter(fragmentManager: FragmentManager, var list : List<Fragment>) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getCount(): Int = list.size

    override fun getItem(position: Int): Fragment = list[position]
}