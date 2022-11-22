package com.ss.ugc.android.editor.picker.album.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.picker.PickComponentConfig
import com.ss.ugc.android.editor.picker.album.config.MaterialListViewConfig
import com.ss.ugc.android.editor.picker.album.view.adapter.AlbumPagerAdapter
import com.ss.ugc.android.editor.picker.album.view.adapter.ConcatPagerAdapter
import com.ss.ugc.android.editor.picker.album.view.adapter.MaterialListViewAdapter
import com.ss.ugc.android.editor.picker.album.viewmodel.AlbumModel
import com.ss.ugc.android.editor.picker.album.viewmodel.MaterialDataViewModel
import com.ss.ugc.android.editor.picker.data.model.LocalMediaCategory
import com.ss.ugc.android.editor.picker.selector.viewmodel.MaterialSelectModel

/**
 * 素材展示页面，负责加载素材数据，展示素材数据。
 * 可以通过传入MaterialSelectModel，实现选择功能。
 */
class AlbumView(
    private val root: ViewGroup,
    private val lifecycleOwner: LifecycleOwner,
    private val albumModel: AlbumModel,
    private val materialSelectModel: MaterialSelectModel? = null,
    private val materialListViewConfig: MaterialListViewConfig,
    private val pickComponentConfig: PickComponentConfig,
    private val extraAdapterList: List<PagerAdapter>? = null,
    private val closeAlbumListener: (() -> Unit)? = null
) {
    private var isInit = false
    var minVideoTimeThreshold:Long = -1
    private lateinit var contentView: ViewGroup
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    private val allListView: MaterialListView by lazy {
        initMaterialListView(contentView, LocalMediaCategory.Type.ALL, minVideoTimeThreshold)
    }

    private val videoListView: MaterialListView by lazy {
        initMaterialListView(contentView, LocalMediaCategory.Type.VIDEO, minVideoTimeThreshold)
    }

    private val imageListView: MaterialListView by lazy {
        initMaterialListView(contentView, LocalMediaCategory.Type.IMAGE)
    }

    fun init() {
        initContentView(root)
        initObserver()
        isInit = true
    }

    fun showView() {
        if (!isInit) {
            init()
        }
        albumModel.requestData()
    }

    private fun initObserver() {
        albumModel.category.observe(lifecycleOwner, {
            it?.filterIsInstance<LocalMediaCategory>()?.let { localMaterials ->
                val concatPagerAdapter = ConcatPagerAdapter()
                val lists = ArrayList<PagerAdapter>().apply {
                    add(AlbumPagerAdapter(localMaterials, pageViewProvider))
                    extraAdapterList?.let { adapterList ->
                        addAll(adapterList)
                    }
                }
                concatPagerAdapter.updateAdapters(lists)
                viewPager.adapter = concatPagerAdapter
            }
        })
    }

    private fun initContentView(root: ViewGroup) {
        contentView = LayoutInflater.from(root.context)
            .inflate(R.layout.layout_album, root, true) as ViewGroup
        viewPager = contentView.findViewById<ViewPager>(R.id.materialViewPager).apply {
            offscreenPageLimit = 3
        }
        tabLayout = contentView.findViewById<TabLayout>(R.id.pagerTab).apply {
            setupWithViewPager(viewPager)
        }
        contentView.findViewById<ImageView>(R.id.backIv).setOnClickListener {
            closeAlbumListener?.invoke()
        }
    }

    private val pageViewProvider: (LocalMediaCategory.Type) -> MaterialListView = {
        when (it) {
            LocalMediaCategory.Type.ALL -> allListView
            LocalMediaCategory.Type.VIDEO -> videoListView
            LocalMediaCategory.Type.IMAGE -> imageListView
        }
    }

    private fun initMaterialListView(
        content: ViewGroup,
        category: LocalMediaCategory.Type,
        minVideoTimeThreshold: Long = -1
    ): MaterialListView {
        val materialDataViewModel =
            MaterialDataViewModel(lifecycleOwner, category, albumModel.materialDataRepository)

        return MaterialListView(
            content.context,
            lifecycleOwner,
            materialListViewConfig,
            MaterialListViewAdapter(
                pickComponentConfig,
                materialListViewConfig,
                materialSelectModel,
                minVideoTimeThreshold
            ),
            materialDataViewModel,
            materialSelectModel
        ).apply {
            init()
            loadData()
        }
    }
}