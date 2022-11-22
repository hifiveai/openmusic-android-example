package com.ss.ugc.android.editor.bottom.panel.audio

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import kotlinx.android.synthetic.main.btm_panel_audio.*

/**
 * time : 2020/12/20
 *
 * description :
 * 文字贴纸
 *
 */
class AudioFragment : BaseUndoRedoFragment<AudioViewModel>() {

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_audio
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVP()
    }

    private fun initVP() {
        setPanelName(getString(R.string.ck_audio))
        hideBottomBar()
        iv_ok.setOnClickListener {
            closeFragment()
        }
        val localMusic =
            Fragment.instantiate(requireContext(), LocalMusicFragment::class.java.canonicalName!!)
        val bundle = Bundle()
        bundle.putInt("type", 1)
        localMusic.arguments = bundle

        val myMusic =
            Fragment.instantiate(requireContext(), LocalMusicFragment::class.java.canonicalName!!)
        val bundle2 = Bundle()
        bundle2.putInt("type", 2)
        myMusic.arguments = bundle2

        viewpager.apply {
            val fragments = arrayListOf<Fragment>(localMusic, myMusic)
            val titles = arrayListOf(
                context.getString(R.string.ck_local),
                context.getString(R.string.ck_music_library)
            )
            adapter = object : FragmentStatePagerAdapter(childFragmentManager) {
                override fun getItem(position: Int): Fragment {
                    return fragments[position]
                }

                override fun getCount(): Int {
                    return fragments.size
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return titles[position]
                }

            }
            tab_text.setupWithViewPager(this)
        }
    }

    override fun provideEditorViewModel(): AudioViewModel {
        return EditViewModelFactory.viewModelProvider(this).get(AudioViewModel::class.java)
    }

    override fun onUpdateUI() {
    }
}