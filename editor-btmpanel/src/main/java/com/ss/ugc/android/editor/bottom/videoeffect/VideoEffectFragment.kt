package com.ss.ugc.android.editor.bottom.videoeffect

import android.annotation.SuppressLint
import android.os.Bundle

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.LogUtils
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import kotlinx.android.synthetic.main.fragment_video_effect.*

/**
 * @date: 2021/2/25
 */

class VideoEffectFragment : BaseUndoRedoFragment<VideoEffectViewModel> {
    companion object {

        const val TAG_WorkPos = "WORK_TYPE"
    }

    enum class WorkPos(val value: Int) {
        NONE(-1),
        REPLACE(0),
        COPY(1),
        CHANGE_WORK_OBJ(2),
        DELETE(3),
        ADD(4)
    }

    //主函数
    constructor()
    private var workPos = WorkPos.NONE.value


    @SuppressLint("ValidFragment")
    constructor(workPos: WorkPos) : this() {
        var args = Bundle()

        args.putInt(TAG_WorkPos, workPos.value)
        this.arguments = args
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workPos = arguments?.getInt(TAG_WorkPos) ?: WorkPos.NONE.value
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(TAG_WorkPos, workPos)
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.fragment_video_effect
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_effect))
        initObserver()
        viewModel.fetchPanelInfo(resourceConfig?.videoEffectPanel
            ?: DefaultResConfig.VIDEOEFFECT_PANEL)
    }

    private fun initObserver() {
        viewModel.categoryInfoList.observe(viewLifecycleOwner) {
            val fragments: ArrayList<Fragment> = ArrayList()
            val titles = ArrayList<String>()
            if (it.isNullOrEmpty()) {
                titles.add(getString(R.string.ck_basic))
                fragments.add(Fragment.instantiate(requireContext(), EffectItemFragment::class.java.canonicalName!!,
                    Bundle().apply {
                        putInt(TAG_WorkPos, workPos)
                    })
                )
            } else {
                for (categoryInfo in it) {
                    titles.add(categoryInfo.name)
                    fragments.add(Fragment.instantiate(requireContext(), EffectItemFragment::class.java.canonicalName!!,
                        Bundle().apply {
                            putString("type", categoryInfo.key)
                            putInt(TAG_WorkPos, workPos)
                        })
                    )
                }
            }
            vp.apply {
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
                addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(p0: Int) {
                    }

                    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                    }

                    override fun onPageSelected(p0: Int) {
                        val selectedPage = fragments[p0]
                        if (selectedPage is EffectItemFragment) {
                            try {
                                selectedPage.selectItem()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                })
                ThemeStore.setSelectedTabIndicatorColor(tab_text)
            }
            ll_top_left.setOnClickListener {
                viewModel.deleteEffect()
                fragments.forEach {
                    if (it is EffectItemFragment) {
                        try {
                            it.updateWorkPos()
                            it.selectItem()
                        } catch (e: Exception) {

                        }
                    }
                }
            }
        }
    }


    override fun provideEditorViewModel(): VideoEffectViewModel {
        return viewModelProvider(this).get(VideoEffectViewModel::class.java)
    }

    override fun onUpdateUI() {
        DLog.d("BaseUndoRedoFragment::UndoRedoListener::VideoEffectFragment")
    }
}