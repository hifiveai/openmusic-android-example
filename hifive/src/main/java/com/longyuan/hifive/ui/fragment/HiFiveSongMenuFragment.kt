package com.longyuan.hifive.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.longyuan.hifive.R
import com.longyuan.hifive.adapter.HiFiveSongMenuAdapter
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.longyuan.hifive.HiFiveConfig
import com.longyuan.hifive.ui.HiFiveActivity
import kotlinx.android.synthetic.main.hifive_music_song_menu_fragment.*


/**
 * #duxiaxing
 * #date: 2022/7/14
 */
class HiFiveSongMenuFragment : BaseFragment(){

    override fun getContentView(): Int = R.layout.hifive_music_song_menu_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        val position = arguments?.getInt("position") ?: (HiFiveConfig.musicMenu.size - 1)
        hiFiveMusicSongMenuRv.layoutManager = GridLayoutManager(context,3)
        context?.let { hiFiveMusicSongMenuRv.adapter = HiFiveSongMenuAdapter(it){ model ->
            if (HiFiveConfig.isNetSystemUsable(activity)){
                (activity as HiFiveActivity).goMenuList(model)
            }else HiFiveConfig.showToast(activity,HiFiveConfig.networkErrorMsg)
        } }

        (hiFiveMusicSongMenuRv.adapter as HiFiveSongMenuAdapter).addData(HiFiveConfig.musicMenu[position])
    }

    companion object{
        fun createMenuView(position : Int) : HiFiveSongMenuFragment {
            return HiFiveSongMenuFragment().apply {
                Bundle().let {
                    it.putInt("position", position)
                    arguments = it
                }
            }
        }
    }
}