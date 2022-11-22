package com.ss.ugc.android.editor.base.resourceview

import android.content.Context
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.imageloder.ImageLoader
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import com.ss.ugc.android.editor.base.resource.DownloadState
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.ResourceModel
import com.ss.ugc.android.editor.base.resourceview.ResourceListAdapter.ResourceItemHolder
import com.ss.ugc.android.editor.base.theme.resource.TextPosition
import com.ss.ugc.android.editor.base.utils.TextUtil
import com.ss.ugc.android.editor.base.utils.UIUtils
import kotlinx.android.synthetic.main.btm_common_resource_item_view.view.*
import java.util.Locale


class ResourceListAdapter(
    private val resourceViewConfig: ResourceViewConfig,
    private val isBuildInResource: Boolean? = false
) : RecyclerView.Adapter<ResourceItemHolder>() {

    private var itemClickListener: ResourceModelClickListener? = null
    val resourceModelList = arrayListOf<ResourceModel>()

    val resourceList
        get() = resourceModelList.map { it.resourceItem }

    fun setData(list: List<ResourceModel>) {
        resourceModelList.clear()
        if (resourceViewConfig.nullItemInFirstConfig.addNullItemInFirst) {
            val emptyResource = ResourceItem().apply {
                val language = Locale.getDefault().language
                name = if (!TextUtils.equals(language, "zh")) {
                    "None"
                } else {
                    "无"
                }
                icon = ""
                path = ""
            }
            val emptyModel = ResourceModel(emptyResource, isSelect = false)
            resourceModelList.add(emptyModel)
        }
        resourceModelList.addAll(list)
        if (resourceViewConfig.customItemConfig.addCustomItemInFirst) {
            val emptyResource = ResourceItem().apply {
                val language = Locale.getDefault().language
                name = if (!TextUtils.equals(language, "zh")) {
                    "Custom"
                } else {
                    "自定义"
                }
                icon = ""
                path = ""
            }
            val emptyModel = ResourceModel(emptyResource, isSelect = false)
            if (resourceViewConfig.nullItemInFirstConfig.addNullItemInFirst) {
                resourceModelList.add(1, emptyModel)
            } else {
                resourceModelList.add(0, emptyModel)
            }
        }
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: ResourceModelClickListener?) {
        this.itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ResourceItemHolder {
        val view = (LayoutInflater.from(parent.context)
            .inflate(R.layout.btm_common_resource_item_view, parent, false)) as ConstraintLayout

        measureView(view, parent)
        ConstraintSet().apply {
            clone(view)
            val maxWidth = maxOf(
                resourceViewConfig.resourceImageConfig.imageWidth,
                resourceViewConfig.selectorConfig.selectorWidth
            )
            val maxHeight = maxOf(
                resourceViewConfig.resourceImageConfig.imageHeight,
                resourceViewConfig.selectorConfig.selectorHeight
            )
            setViewWidthAndHeight(this, view, R.id.resourceItemGroup, maxWidth, maxHeight)
            applyTo(view)
        }

        if (resourceViewConfig.resourceTextConfig.enableText) {
            view.resourceItemText.visibility = View.VISIBLE
            view.resourceItemText.textSize =
                resourceViewConfig.resourceTextConfig.textSize.toFloat()
            if (resourceViewConfig.resourceTextConfig.textPosition == TextPosition.DOWN) {
                ConstraintSet().apply {
                    clone(view)
                    connect(
                        R.id.resourceItemGroup,
                        ConstraintSet.TOP,
                        R.layout.btm_common_resource_item_view,
                        ConstraintSet.TOP,
                        0
                    )
                    connect(
                        R.id.resourceItemText,
                        ConstraintSet.TOP,
                        R.id.resourceItemGroup,
                        ConstraintSet.BOTTOM,
                        UIUtils.dp2px(parent.context, 3.0f)
                    )
                    applyTo(view)
                }
            }
        } else {
            ConstraintSet().apply {
                clone(view)
                connect(
                    R.id.resourceItemGroup,
                    ConstraintSet.TOP,
                    R.layout.btm_common_resource_item_view,
                    ConstraintSet.TOP,
                    0
                )
                applyTo(view)
            }
            view.resourceItemText.visibility = View.GONE
        }
        if (resourceViewConfig.downloadIconConfig.enableDownloadIcon) {
            view.resourceItemDownload.visibility = View.VISIBLE
            view.resourceItemDownload.setImageResource(resourceViewConfig.downloadIconConfig.iconResource)
        } else {
            view.resourceItemDownload.visibility = View.INVISIBLE
        }

        return ResourceItemHolder(view)
    }

    private fun measureView(view: ConstraintLayout, parent: ViewGroup) {
        ConstraintSet().apply {
            clone(view.resourceItemGroup)
            val resourceWidth = resourceViewConfig.resourceImageConfig.imageWidth
            val resourceHeight = resourceViewConfig.resourceImageConfig.imageHeight
            val downloadIconWidth = resourceViewConfig.downloadIconConfig.iconWidth
            val downloadIconHeight = resourceViewConfig.downloadIconConfig.iconHeight

            setViewWidthAndHeight(
                this, parent, R.id.resourceItemImage,
                resourceWidth, resourceHeight
            )
            setViewWidthAndHeight(
                this, parent, R.id.resourceItemMask,
                resourceViewConfig.selectorConfig.selectorWidth,
                resourceViewConfig.selectorConfig.selectorHeight
            )
            setViewWidthAndHeight(
                this, parent, R.id.resourceItemMaskText,
                resourceViewConfig.selectorConfig.selectorWidth,
                resourceViewConfig.selectorConfig.selectorHeight
            )
            setViewWidthAndHeight(
                this, parent, R.id.resourceItemDownload,
                downloadIconWidth, downloadIconHeight
            )

            if (resourceViewConfig.customItemConfig.addCustomItemInFirst) {
                setViewWidthAndHeight(
                    this, parent, R.id.resourceItemClear,
                    16, 16
                )
            }

            val downloadStartMargin = resourceWidth - downloadIconWidth + 3
            val downloadBottomMargin = resourceHeight - downloadIconHeight + 3
            val clearStartMargin = resourceWidth - 16 + 8
            val clearBottomMargin = resourceHeight - 16 + 8
            connect(
                R.id.resourceItemDownload,
                ConstraintSet.START,
                R.id.resourceItemImage,
                ConstraintSet.START,
                UIUtils.dp2px(parent.context, downloadStartMargin.toFloat())
            )
            connect(
                R.id.resourceItemDownload,
                ConstraintSet.BOTTOM,
                R.id.resourceItemImage,
                ConstraintSet.BOTTOM,
                UIUtils.dp2px(parent.context, downloadBottomMargin.toFloat())
            )
            if (resourceViewConfig.customItemConfig.addCustomItemInFirst) {
                connect(
                    R.id.resourceItemClear,
                    ConstraintSet.START,
                    R.id.resourceItemImage,
                    ConstraintSet.START,
                    UIUtils.dp2px(parent.context, clearStartMargin.toFloat())
                )
                connect(
                    R.id.resourceItemClear,
                    ConstraintSet.BOTTOM,
                    R.id.resourceItemImage,
                    ConstraintSet.BOTTOM,
                    UIUtils.dp2px(parent.context, clearBottomMargin.toFloat())
                )
            }
            applyTo(view.resourceItemGroup)
        }
    }

    private fun setViewWidthAndHeight(
        it: ConstraintSet, parent: ViewGroup,
        viewId: Int, width: Int, height: Int
    ) {
        it.apply {
            constrainWidth(viewId, UIUtils.dp2px(parent.context, width.toFloat()))
            constrainHeight(viewId, UIUtils.dp2px(parent.context, height.toFloat()))
        }
    }

    override fun getItemCount(): Int {
        return resourceModelList.size
    }

    override fun onBindViewHolder(viewHolder: ResourceItemHolder, position: Int) {
        val resourceModel = resourceModelList[position]
        val itemView = viewHolder.itemView
        val context = itemView.context
        val addNullItemInFirst = resourceViewConfig.nullItemInFirstConfig.addNullItemInFirst
        val addCustomItemInFirst = resourceViewConfig.customItemConfig.addCustomItemInFirst

        viewHolder.clearIcon.visibility = View.INVISIBLE
        if (resourceViewConfig.resourceTextConfig.enableText) {
            val textColor =
                ContextCompat.getColor(context, resourceViewConfig.resourceTextConfig.textColor)
            viewHolder.textView.setTextColor(textColor)
            viewHolder.textView.text = TextUtil.handleText(
                resourceModel.resourceItem.name,
                resourceViewConfig.resourceTextConfig.textMaxLen
            )
        }
        when (resourceModel.downloadState) {
            DownloadState.INIT -> onDownloadInit(context, viewHolder, resourceModel, position)

            DownloadState.LOADING -> onDownloading(viewHolder)

            DownloadState.SUCCESS -> onDownloadSuccess(context, viewHolder, resourceModel, position)
        }
        if (addNullItemInFirst && position == 0) {
            viewHolder.imageView.scaleType = ImageView.ScaleType.CENTER
            viewHolder.imageView.setImageResource(resourceViewConfig.nullItemInFirstConfig.nullItemIcon)
            viewHolder.imageView.setBackgroundResource(resourceViewConfig.nullItemInFirstConfig.nullItemResource)
            viewHolder.downloadIcon.visibility = View.INVISIBLE
        } else if ((!addNullItemInFirst && position == 0 && addCustomItemInFirst) || (addNullItemInFirst && position == 1 && addCustomItemInFirst)) {
            if (resourceModel.resourceItem.icon.isNotBlank()) {
                ImageLoader.loadBitmap(
                    viewHolder.itemView.context,
                    resourceModel.resourceItem.icon,
                    viewHolder.imageView,
                    ImageOption.Builder()
                        .scaleType(ImageView.ScaleType.CENTER_CROP)
                        .roundCorner(
                            UIUtils.dp2px(
                                itemView.context,
                                resourceViewConfig.resourceImageConfig.roundRadius.toFloat()
                            )
                        )
                        .placeHolder(resourceViewConfig.resourceImageConfig.resourcePlaceHolder)
                        .build()
                )
                viewHolder.clearIcon.visibility = View.VISIBLE
            } else {
                viewHolder.imageView.scaleType = ImageView.ScaleType.CENTER
                viewHolder.imageView.setImageResource(resourceViewConfig.customItemConfig.customItemIcon)
                viewHolder.imageView.setBackgroundResource(resourceViewConfig.customItemConfig.customItemResource)
            }
            viewHolder.downloadIcon.visibility = View.INVISIBLE
        } else {
            viewHolder.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            viewHolder.imageView.setBackgroundResource(resourceViewConfig.resourceImageConfig.backgroundResource)
            val padding = UIUtils.dp2px(
                itemView.context,
                resourceViewConfig.resourceImageConfig.padding.toFloat()
            )
            viewHolder.imageView.setPadding(padding, padding, padding, padding)
            isBuildInResource?.let {
                if (resourceViewConfig.iconStyle == IconStyle.IMAGE) {
                    val iconPath =
                        if (resourceViewConfig.resourceImageConfig.enableSelectedIcon && resourceModel.isSelect && !isBuildInResource) {
                            resourceModel.resourceItem.selectedIcon
                        } else {
                            resourceModel.resourceItem.icon
                        }
                    ImageLoader.loadBitmap(
                        viewHolder.itemView.context,
                        iconPath,
                        viewHolder.imageView,
                        ImageOption.Builder()
                            .scaleType(ImageView.ScaleType.CENTER_CROP)
                            .roundCorner(
                                UIUtils.dp2px(
                                    itemView.context,
                                    resourceViewConfig.resourceImageConfig.roundRadius.toFloat()
                                )
                            )
                            .placeHolder(resourceViewConfig.resourceImageConfig.resourcePlaceHolder)
                            .build()
                    )
                } else {
                    resourceModel.resourceItem.videoFrame?.let {
                        ImageLoader.loadImageBitmap(
                            viewHolder.itemView.context,
                            resourceModel.resourceItem.videoFrame,
                            viewHolder.imageView,
                            ImageOption.Builder()
                                .scaleType(ImageView.ScaleType.CENTER_CROP)
                                .roundCorner(
                                    UIUtils.dp2px(
                                        itemView.context,
                                        resourceViewConfig.resourceImageConfig.roundRadius.toFloat()
                                    )
                                )
                                .placeHolder(resourceViewConfig.resourceImageConfig.resourcePlaceHolder)
                                .blurRadius((resourceModel.resourceItem.blurRadius * 14f).toInt())
                                .build()
                        )
                    }
                }
            }
        }
        itemView.setOnClickListener {
            itemClickListener?.onItemClick(resourceModel, position)
        }

        viewHolder.clearIcon.setOnClickListener {
            resourceViewConfig.customItemConfig.onClearButtonClick?.invoke()
        }
    }

    private fun onDownloadSuccess(
        context: Context,
        viewHolder: ResourceItemHolder,
        resourceModel: ResourceModel,
        position: Int
    ) {
        viewHolder.loadingIcon.visibility = View.INVISIBLE
        viewHolder.downloadIcon.visibility = View.INVISIBLE
        if (resourceModel.isSelect) {
            val selectorBgColor =
                resourceViewConfig.selectorConfig.selectorBorderRes
            viewHolder.mask.setBackgroundResource(selectorBgColor)
            if (resourceViewConfig.nullItemInFirstConfig.addNullItemInFirst && position == 0 && !resourceViewConfig.nullItemInFirstConfig.isIdentical) {
                viewHolder.mask.setImageResource(resourceViewConfig.nullItemInFirstConfig.selectorResource)
            } else {
                viewHolder.mask.setImageResource(resourceViewConfig.selectorConfig.selectorBorderRes)
            }
            if (resourceViewConfig.selectorConfig.enableSelector) {
                viewHolder.mask.visibility = View.VISIBLE
                if (resourceViewConfig.resourceTextConfig.enableText) {
                    val selectedTextColor = ContextCompat.getColor(
                        context,
                        resourceViewConfig.resourceTextConfig.textSelectedColor
                    )
                    viewHolder.textView.setTextColor(selectedTextColor)
                }
            }
            if (!TextUtils.isEmpty(resourceViewConfig.selectorConfig.selectText)) {
                viewHolder.maskText.visibility = View.VISIBLE
                viewHolder.maskText.text = resourceViewConfig.selectorConfig.selectText
            }
        } else {
            viewHolder.mask.visibility = View.INVISIBLE
            viewHolder.maskText.visibility = View.INVISIBLE
            if (resourceViewConfig.resourceTextConfig.enableText) {
                val textColor =
                    ContextCompat.getColor(context, resourceViewConfig.resourceTextConfig.textColor)
                viewHolder.textView.setTextColor(textColor)
            }
        }
    }

    private fun onDownloading(viewHolder: ResourceListAdapter.ResourceItemHolder) {
        viewHolder.loadingIcon.visibility = View.VISIBLE
        viewHolder.downloadIcon.visibility = View.INVISIBLE
        viewHolder.mask.visibility = View.INVISIBLE
        viewHolder.maskText.visibility = View.INVISIBLE
    }

    private fun onDownloadInit(
        context: Context, viewHolder: ResourceItemHolder,
        resourceModel: ResourceModel, position: Int
    ) {
        val addNullItemInFirst = resourceViewConfig.nullItemInFirstConfig.addNullItemInFirst
        val addCustomItemInFirst = resourceViewConfig.customItemConfig.addCustomItemInFirst
        viewHolder.loadingIcon.visibility = View.INVISIBLE
        if (EditorSDK.instance.config.resourceProvider?.isResourceReady(resourceModel.resourceItem.resourceId) == true || resourceModel.resourceItem.resourceId.isEmpty()) {
            viewHolder.downloadIcon.visibility = View.INVISIBLE
            if (resourceModel.isSelect) {
                val unEnableNullItemSelector =
                    resourceViewConfig.nullItemInFirstConfig.addNullItemInFirst && position == 0 && !resourceViewConfig.nullItemInFirstConfig.enableSelector
                val unEnableCustomItemSelector =
                    ((!addNullItemInFirst && position == 0 && addCustomItemInFirst) || (addNullItemInFirst && position == 1 && addCustomItemInFirst)) && !resourceViewConfig.customItemConfig.enableCustomSelector
                if (unEnableNullItemSelector || unEnableCustomItemSelector) {
                    viewHolder.mask.visibility = View.INVISIBLE
                    viewHolder.maskIcon.visibility = View.INVISIBLE
                    viewHolder.maskText.visibility = View.INVISIBLE
                    if (resourceViewConfig.resourceTextConfig.enableText) {
                        val textColor = ContextCompat.getColor(
                            context,
                            resourceViewConfig.resourceTextConfig.textColor
                        )
                        viewHolder.textView.setTextColor(textColor)
                    }
                } else {
                    val selectorBgColor = resourceViewConfig.selectorConfig.selectorBorderRes
                    viewHolder.mask.setBackgroundResource(selectorBgColor)
                    if (resourceViewConfig.nullItemInFirstConfig.addNullItemInFirst && position == 0 && !resourceViewConfig.nullItemInFirstConfig.isIdentical) {
                        viewHolder.mask.setImageResource(resourceViewConfig.nullItemInFirstConfig.selectorResource)
                        viewHolder.maskIcon.visibility = View.INVISIBLE
                    } else {
                        viewHolder.mask.setImageResource(resourceViewConfig.selectorConfig.selectorBorderRes)
                        if (resourceViewConfig.selectorConfig.selectorIcon != 0) {
                            viewHolder.maskIcon.visibility = View.VISIBLE
                            viewHolder.maskIcon.setImageResource(resourceViewConfig.selectorConfig.selectorIcon)
                        } else {
                            viewHolder.maskIcon.visibility = View.INVISIBLE
                        }
                    }
                    if (resourceViewConfig.selectorConfig.enableSelector) {
                        viewHolder.mask.visibility = View.VISIBLE
                        if (resourceViewConfig.resourceTextConfig.enableText) {
                            val selectedTextColor = ContextCompat.getColor(
                                context,
                                resourceViewConfig.resourceTextConfig.textSelectedColor
                            )
                            viewHolder.textView.setTextColor(selectedTextColor)
                        }
                    }
                    if (!TextUtils.isEmpty(resourceViewConfig.selectorConfig.selectText)) {
                        viewHolder.maskText.visibility = View.VISIBLE
                        viewHolder.maskText.text = resourceViewConfig.selectorConfig.selectText
                    }
                }
            } else {
                viewHolder.mask.visibility = View.INVISIBLE
                viewHolder.maskIcon.visibility = View.INVISIBLE
                viewHolder.maskText.visibility = View.INVISIBLE
                if (resourceViewConfig.resourceTextConfig.enableText) {
                    val textColor = ContextCompat.getColor(
                        context,
                        resourceViewConfig.resourceTextConfig.textColor
                    )
                    viewHolder.textView.setTextColor(textColor)
                }
            }
        } else {
            viewHolder.mask.visibility = View.INVISIBLE
            viewHolder.maskIcon.visibility = View.INVISIBLE
            viewHolder.maskText.visibility = View.INVISIBLE
            if (resourceViewConfig.resourceTextConfig.enableText) {
                val textColor =
                    ContextCompat.getColor(context, resourceViewConfig.resourceTextConfig.textColor)
                viewHolder.textView.setTextColor(textColor)
            }
            if (resourceViewConfig.downloadIconConfig.enableDownloadIcon)
                viewHolder.downloadIcon.visibility = View.VISIBLE
        }
    }

    class ResourceItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.resourceItemImage)
        val textView: TextView = itemView.findViewById(R.id.resourceItemText)
        val mask: ImageView = itemView.findViewById(R.id.resourceItemMask)
        val maskIcon: ImageView = itemView.findViewById(R.id.resourceItemMask_icon)
        val loadingIcon: FrameLayout = itemView.findViewById(R.id.resourceItemLoading)
        val downloadIcon: ImageView = itemView.findViewById(R.id.resourceItemDownload)
        val maskText: TextView = itemView.findViewById(R.id.resourceItemMaskText)
        val clearIcon: ImageView = itemView.findViewById(R.id.resourceItemClear)
    }

}

interface ResourceItemClickListener {
    fun onItemClick(item: ResourceItem?, position: Int, select: Boolean)
}

interface ResourceModelClickListener {
    fun onItemClick(item: ResourceModel?, position: Int)
}

interface ResourceListInitListener {
    fun onResourceListInitFinish()
}

