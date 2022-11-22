package com.ss.ugc.android.editor.main.template


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TemplateViewPagerAdapter(fragmentManager: FragmentManager, val fragmentList: List<Fragment>, val tabTitleList:MutableList<String>) :
    FragmentStatePagerAdapter(fragmentManager) {
    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(postion: Int): Fragment {
        return fragmentList[postion]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitleList[position]
    }
}