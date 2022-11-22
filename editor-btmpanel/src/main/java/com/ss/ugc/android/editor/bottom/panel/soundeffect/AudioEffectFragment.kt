package com.ss.ugc.android.editor.bottom.panel.soundeffect

import android.os.Bundle

//import androidx.core.app.FragmentStatePagerAdapter
//import androidx.core.view.ViewPager.OnPageChangeListener
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ss.ugc.android.editor.base.fragment.BasePanelFragment
import com.ss.ugc.android.editor.base.monitior.ReportConstants
import com.ss.ugc.android.editor.base.monitior.ReportUtils
import com.ss.ugc.android.editor.base.music.data.MusicCollection
import com.ss.ugc.android.editor.base.music.tools.MusicUtils
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.bottom.R
import kotlinx.android.synthetic.main.btm_panel_audio_effect.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * @date: 2021/6/21
 * @desc: 音效面板Fragment
 */
class AudioEffectFragment : BasePanelFragment<BaseEditorViewModel>(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_audio_effect
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_sound_effect))
        requestData()
    }

    private fun requestData() {
        launch {
            val soundEffectsCollection = MusicUtils.getSoundEffectsCollection()
            if (soundEffectsCollection.isNullOrEmpty()) {
                setViewState(false)
            }else{
                setupViewPager(soundEffectsCollection)
                setViewState(true)
            }
        }
    }

    private fun setViewState(isSuccess: Boolean) {
        if (isSuccess) {
            soundsTab.visibility = View.VISIBLE
            viewpager.visibility = View.VISIBLE
            soundsError.visibility = View.GONE
        } else {
            soundsTab.visibility = View.GONE
            viewpager.visibility = View.GONE
            soundsError.visibility = View.VISIBLE
        }
    }

    private fun setupViewPager(collections: List<MusicCollection>) {
        viewpager.apply {
            adapter = object : FragmentStatePagerAdapter(childFragmentManager) {
                override fun getItem(position: Int): Fragment {
                    return AudioEffectListFragment.newInstance(
                            collections[position],
                            getPageTitle(position)?.toString(),
                    )
                }

                override fun getCount(): Int {
                    return collections.size
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    val language = Locale.getDefault().language
                    return if (!TextUtils.equals(language, "zh") && !TextUtils.isEmpty(collections[position].nameEn)) {
                        collections[position].nameEn
                    } else {
                        collections[position].name
                    }
                }
            }
            val pageChangeListener = object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(position: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    val tabName = (adapter as FragmentStatePagerAdapter).getPageTitle(position)?.toString() ?: ""
                    ReportUtils.doReport(
                            ReportConstants.AUDIO_TAB_ENTER_EVENT,
                            mutableMapOf("tab_name" to tabName)
                    )
                }
            }
            addOnPageChangeListener(pageChangeListener)
            pageChangeListener.onPageSelected(0)
            tab_text.setupWithViewPager(this)
//            ThemeStore.setSelectedTabIndicatorColor(tab_text)
        }
    }

    override fun provideEditorViewModel(): BaseEditorViewModel {
        return BaseEditorViewModel(requireActivity())
    }
}