package com.ss.ugc.android.editor.picker.mediapicker

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.imageloder.ImageLoader
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.core.api.video.EditMedia
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.picker.PickComponentConfig
import com.ss.ugc.android.editor.picker.album.config.MaterialListViewConfig
import com.ss.ugc.android.editor.picker.album.config.MaterialViewHolderConfig
import com.ss.ugc.android.editor.picker.album.view.AlbumView
import com.ss.ugc.android.editor.picker.album.viewmodel.AlbumModel
import com.ss.ugc.android.editor.picker.data.model.*
import com.ss.ugc.android.editor.picker.data.repository.CategoryDataRepository
import com.ss.ugc.android.editor.picker.data.repository.MaterialDataRepository
import com.ss.ugc.android.editor.picker.mediapicker.PickType.SELECT
import com.ss.ugc.android.editor.picker.preview.view.MaterialPreView
import com.ss.ugc.android.editor.picker.preview.viewmodel.MaterialPreViewModel
import com.ss.ugc.android.editor.picker.selector.config.SelectorViewConfig
import com.ss.ugc.android.editor.picker.selector.validator.*
import com.ss.ugc.android.editor.picker.selector.view.MaterialSelectorView
import com.ss.ugc.android.editor.picker.selector.view.SelectViewType
import com.ss.ugc.android.editor.picker.selector.viewmodel.MaterialSelectModel
import com.ss.ugc.android.editor.picker.utils.FileUtils
import kotlinx.android.synthetic.main.pick_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class PickerActivity : AppCompatActivity() {
    private lateinit var materialSelectModel: MaterialSelectModel
    private lateinit var materialPreViewModel: MaterialPreViewModel
    private lateinit var albumModel: AlbumModel

    private lateinit var materialSelectorView: MaterialSelectorView
    private lateinit var materialPreView: MaterialPreView
    private lateinit var albumView: AlbumView

    private lateinit var pickComponentConfig: PickComponentConfig

    //最大选择数量
    private var maxSelect = PickerConfig.DEFAULT_SELECTED_MAX_COUNT

    //最大选择大小
    private var maxSize = PickerConfig.DEFAULT_SELECTED_MAX_SIZE

    //是否有限制视频最少的播放时间，如果返回-1则表示没有限制
    private var timeLimit = PickerConfig.NO_TIME_LIMIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pick_main)
        initView()
    }

    @AfterPermissionGranted(119)
    private fun initView() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            intent?.let {
                maxSelect = intent.getIntExtra(
                    PickerConfig.MAX_SELECT_COUNT,
                    PickerConfig.DEFAULT_SELECTED_MAX_COUNT
                )
                maxSize = intent.getLongExtra(
                    PickerConfig.MAX_SELECT_SIZE,
                    PickerConfig.DEFAULT_SELECTED_MAX_SIZE
                )
                timeLimit = intent.getLongExtra(
                    PickerConfig.MIN_VIDEO_TIME_THRESHOLD,
                    PickerConfig.NO_TIME_LIMIT
                )
                pickComponentConfig = PickComponentConfig(imageLoader = ImageLoader)
                initViewModel()
                initAlbum()
                initPreView()
                initSelectView(it)
            }
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.ck_read_external_storage),
                119,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    /**
     * 是否 因为「视频播放时长少于替换素材的时长」而不执行监听器的相关功能
     */
    private fun isInvalidMedia(
        list: List<MediaItem>,
        position: Int,
        minVideoTimeThreshold: Long
    ): Boolean {
        val mediaItem = list[position]
        if (mediaItem.isVideo()) {
            if (list[position].duration < minVideoTimeThreshold) {
                return true
            }
        }
        return false
    }

    /**
     * 素材选择回调
     */
    private val selectorClickListener: (list: List<MediaItem>, position: Int) -> Unit =
        { list, position ->
            if (!isInvalidMedia(list, position, timeLimit)) {
                materialSelectModel.changeSelectState(list[position])
            }
        }

    /**
     * 素材点击回调
     */
    private val itemClickListener: (list: List<MediaItem>, position: Int) -> Unit =
        { list, position ->
            if (!isInvalidMedia(list, position, timeLimit))
                if (maxSelect <= 1) {
                    done(listOf(list[position]))
                } else {
                    materialPreView.show(list, position)
                }
        }

    /**
     * 选择完成回调
     */
    private val confirmClickListener: () -> Unit = {
        done(materialSelectModel.selectedList)
    }

    fun done(selects: List<MediaItem>) {
        val list = selects.map { mediaItem ->
            EditMedia(
                mediaItem.path,
                mediaItem.isVideo(),
                mediaItem.width,
                mediaItem.height,
                mediaItem.size,
                mediaItem.duration,
                mediaItem.rotation
            )
        }
        setResult(PickerConfig.RESULT_CODE, Intent().apply {
            putParcelableArrayListExtra(PickerConfig.EXTRA_RESULT, list as ArrayList<EditMedia>)
        })
        finish()
    }

    override fun onBackPressed() {
        done(emptyList())
        super.onBackPressed()
    }

    private fun initViewModel() {
        materialSelectModel =
            ViewModelProviders.of(this).get(MaterialSelectModel::class.java).apply {
                addConfirmValidator(MinCountConfirmValidator(1))
                addPostSelectValidator(MaxCountPostValidator(maxSelect))
                addPreSelectValidator(MaxCountPreValidator(maxSelect))
                addPreSelectValidator(MaxSizePreValidator(maxSize, onInValidate = {
                    //当选择的素材大小不符合条件时，弹出Toast提醒
                    Toaster.toast(
                        this@PickerActivity.getString(R.string.ck_msg_size_limit) + (FileUtils.fileSize(
                            maxSize
                        ))
                    )
                }))
                addPreSelectValidator(VeUtilsPreValidator {
                    Toaster.toast(getString(R.string.ck_tips_the_material_not_supported))
                })
            }
        val type = intent.getIntExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE_VIDEO)
        var imageQueryParams: QueryParam? = null
        var categoryDataRepository: CategoryDataRepository? = null

        when (type) {
            PickerConfig.PICKER_IMAGE_INCLUDE_GIF -> {
                imageQueryParams = DEFAULT_IMAGE_QUERY_PARAM
                categoryDataRepository = object : CategoryDataRepository(this@PickerActivity) {
                    override val categories: List<MediaCategory>
                        get() = listOf(
                            LocalMediaCategory.ofImage(this@PickerActivity)
                        )
                }
            }

            PickerConfig.PICKER_IMAGE_EXCLUDE_GIF -> {
                imageQueryParams = DEFAULT_IMAGE_QUERY_PARAM_NON_GIF
                categoryDataRepository = object : CategoryDataRepository(this@PickerActivity) {
                    override val categories: List<MediaCategory>
                        get() = listOf(
                            LocalMediaCategory.ofImage(this@PickerActivity)
                        )
                }
            }

            PickerConfig.PICKER_VIDEO -> {
                categoryDataRepository = object : CategoryDataRepository(this@PickerActivity) {
                    override val categories: List<MediaCategory>
                        get() = listOf(
                            LocalMediaCategory.ofVideo(this@PickerActivity)
                        )
                }
            }
        }

        val materialDataRepository = MaterialDataRepository(
            imageQueryParams = imageQueryParams ?: DEFAULT_IMAGE_QUERY_PARAM,
            videoQueryParams = DEFAULT_VIDEO_QUERY_PARAM
        )

        albumModel = ViewModelProvider(
            this@PickerActivity,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AlbumModel(
                        categoryDataRepository ?: CategoryDataRepository(this@PickerActivity),
                        materialDataRepository
                    ) as T
                }
            }
        ).get(AlbumModel::class.java)
        materialPreViewModel = ViewModelProviders.of(this).get(MaterialPreViewModel::class.java)
    }

    private fun initAlbum() {
        val materialListViewConfig = MaterialListViewConfig(
            itemClickListener = itemClickListener,
            selectorClickListener = selectorClickListener,
            materialViewHolderConfig = MaterialViewHolderConfig(
                //如果最大选择数量大于1，则显示选择icon
                showSelectIcon = maxSelect > 1,
                showNonSelectableMask = true,
                noSelectIcon = R.drawable.ic_no_select,
                selectedIcon = R.drawable.ic_selected
            )
        )

        albumView = AlbumView(
            root = album_root,
            lifecycleOwner = this,
            albumModel = albumModel,
            materialSelectModel = materialSelectModel,
            materialListViewConfig = materialListViewConfig,
            pickComponentConfig = pickComponentConfig,
            closeAlbumListener = {
                done(emptyList())
            }
        ).apply {
            init()
            showView()
        }
        albumView.minVideoTimeThreshold = timeLimit
    }

    private fun initPreView() {
        materialPreView = MaterialPreView(
            root = preview_root,
            lifecycleOwner = this,
            materialPreViewModel = materialPreViewModel,
            pickComponentConfig = pickComponentConfig,
            viewStateChangeListener = { visible ->
                if (visible) {
                    //在preview场景下，选择view为添加功能类型
                    materialSelectorView.changeViewType(SelectViewType.ADD)
                } else {
                    materialSelectorView.changeViewType(SelectViewType.CONFIRM)
                }
            }
        )
    }

    private fun initSelectView(intent: Intent) {
        val type = intent.getStringExtra(PickerConfig.PICK_TYPE)
        val text = type?.let {
            when (it) {
                SELECT.type -> this@PickerActivity.resources.getString(R.string.ck_start)
                else -> this@PickerActivity.resources.getString(R.string.ck_add)
            }
        }

        val config = text?.let {
            SelectorViewConfig(it)
        }

        materialSelectorView = MaterialSelectorView(
            root = selector_root,
            lifecycleOwner = this,
            materialSelectModel = materialSelectModel,
            selectorViewConfig = config,
            selectorMeasureListener = { height ->
                album_root.layoutParams.apply {
                    this.height = SizeUtil.getScreenHeight(this@PickerActivity) - height
                }
            },
            confirmClickListener = confirmClickListener,
            addClickListener = {
                materialPreViewModel.selectedPage.value?.let { addItem ->
                    val isSelected = materialSelectModel.getSelectState(addItem).isSelected()
                    if (!isSelected) {
                        materialSelectModel.changeSelectState(addItem)
                    }
                    done(materialSelectModel.selectedList)
                }
            }
        )
        //当最大选择数大于1，才需要在素材展示列表页面显示选择页面。
        if (maxSelect > 1) {
            materialSelectorView.init()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}