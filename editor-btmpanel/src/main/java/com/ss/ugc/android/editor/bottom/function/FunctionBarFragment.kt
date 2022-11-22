package com.ss.ugc.android.editor.bottom.function

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.theme.AlignInParent.CENTER
import com.ss.ugc.android.editor.base.theme.AlignInParent.LEFT
import com.ss.ugc.android.editor.base.theme.AlignInParent.RIGHT
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.CommonUtils
import com.ss.ugc.android.editor.base.utils.UIUtils
import com.ss.ugc.android.editor.base.view.FuncItemDecoration
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.R.id
import com.ss.ugc.android.editor.bottom.R.layout
import com.ss.ugc.android.editor.bottom.event.*
import com.ss.ugc.android.editor.bottom.function.FunctionListAdapter.ItemViewHolder
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import kotlinx.android.synthetic.main.btm_function_bar_layout.*

/**
 * @date: 2021/3/26
 */
class FunctionBarFragment : Fragment() {

    private var isRoot: Boolean = false
    private var parentItem: FunctionItem? = null
    private var funcItemList: ArrayList<FunctionItem>? = FunctionDataHelper.getFunctionItemList()
    private var recyclerView: RecyclerView? = null
    private var contentView: View? = null
    private var functionListAdapter: FunctionListAdapter? = null
    private var backIconLayout: View? = null
    private var backIcon: ImageView? = null
    private var currentFuncType: String? = null
    private var functionBarThemeConfig = ThemeStore.getFunctionBarViewConfig()

    var functionItemTreeHelper: FunctionItemTreeHelper? = null

    fun getCurrentFuncType(): String? {
        return currentFuncType
    }

    fun getParentItem(): FunctionItem? {
        return parentItem
    }

    companion object {
        fun newInstance(): FunctionBarFragment {
            return FunctionBarFragment()
        }
    }

    fun updateFunctionList(functionItemList: ArrayList<FunctionItem>) {
        this.funcItemList = functionItemList
        functionListAdapter?.setList(functionItemList)
    }

    fun notifyItemChange(item: FunctionItem) {
        functionListAdapter?.notifyItemChange(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRoot = (parentItem == null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.btm_function_bar_layout, container, false)
    }

    @SuppressLint("RtlHardcoded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        contentView = view
        val barBgRes = functionBarThemeConfig.funcBarBackgroundDrawableRes
        if (barBgRes != 0) {
            contentView!!.background = context?.getDrawable(barBgRes)
        }
        contentView?.apply {
            val barHeight = functionBarThemeConfig.funcBarHeight
            layoutParams.height = UIUtils.dp2px(view.context, barHeight.toFloat())
        }

        recyclerView = view.findViewById(R.id.rcy_function_list)
        backIconLayout = view.findViewById<FrameLayout>(R.id.iv_back_container)
        backIcon = view.findViewById(R.id.iv_bottom_func_back)
        backIconLayout?.apply {
            visibility = if (isRoot) {
                View.GONE
            } else {
                View.VISIBLE
            }
            setOnClickListener {
                if (parentItem?.type == FunctionType.TYPE_FUNCTION_CUT) {
                    viewModelProvider(this@FunctionBarFragment).get(EditModeEvent::class.java)
                        .setChangeEditMode(false)
                }
                if (parentItem?.type == FunctionType.TYPE_FUNCTION_CANVAS) {
                    viewModelProvider(this@FunctionBarFragment).get(CanvasModeEvent::class.java)
                        .setChangeCanvasMode(false)
                }
                viewModelProvider(this@FunctionBarFragment).get(BackClickEvent::class.java)
                    .setChanged(parentItem)
            }
        }

        if (!isRoot) {
            val layoutParams = recyclerView?.layoutParams as LayoutParams
            layoutParams.gravity = when (functionBarThemeConfig!!.childrenAlignInParent) {
                LEFT -> Gravity.LEFT
                CENTER -> Gravity.CENTER
                RIGHT -> Gravity.RIGHT
            }
        }
        val itemSpacing = functionBarThemeConfig.itemSpacing
        if (itemSpacing != 0) {
            recyclerView?.addItemDecoration(FuncItemDecoration(itemSpacing))
        }
        if (functionBarThemeConfig.backIconDrawableRes != 0) {
            view.findViewById<ImageView>(R.id.iv_bottom_func_back)?.apply {
                setImageDrawable(context.getDrawable(functionBarThemeConfig.backIconDrawableRes))
            }
        }
        if (functionBarThemeConfig.backIconMarginStart != 0) {
            view.findViewById<ImageView>(R.id.iv_bottom_func_back)?.apply {
                val currentLayoutParams = layoutParams
                if (currentLayoutParams is  MarginLayoutParams) {
                    currentLayoutParams.leftMargin = UIUtils.dp2px(view.context, functionBarThemeConfig.backIconMarginStart.toFloat())
                }
                layoutParams = currentLayoutParams
            }
            if (functionBarThemeConfig.backIconMarginStart != 0) {
                view.findViewById<ImageView>(R.id.iv_bottom_func_back)?.apply {
                    val currentLayoutParams = layoutParams
                    if (currentLayoutParams is  MarginLayoutParams) {
                        currentLayoutParams.leftMargin = UIUtils.dp2px(view.context, functionBarThemeConfig!!.backIconMarginStart.toFloat())
                    }
                    layoutParams = currentLayoutParams
                }
            }
            if (functionBarThemeConfig.backIconContainerBackgroundColor != 0) {
                val backgroundColor = ContextCompat.getColor(requireContext(), functionBarThemeConfig!!.backIconContainerBackgroundColor)
                backIconLayout?.setBackgroundColor(backgroundColor)
            }
        }
        recyclerView?.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        functionListAdapter = FunctionListAdapter(object : IFunctionItemClick {
            override fun onItemClick(funcItem: FunctionItem, position: Int) {
                if (position==5 && CommonUtils.isFastClick(1000)) {
                    Toaster.show(getString(R.string.ck_click_too_quick))
                    return
                }
                viewModelProvider(this@FunctionBarFragment).get(FuncItemClickEvent::class.java).setClickedItem(funcItem)
                if (funcItem.hasChildren()) {
                    expandChildren(funcItem)
                } else {
                    showOperationPanel(funcItem)
                }
            }
        })
        functionListAdapter?.setList(funcItemList!!)
        recyclerView?.adapter = functionListAdapter
    }

    fun showRootList(funcItemList: ArrayList<FunctionItem>) {
        if (currentFuncType != null) {
            this.currentFuncType = null
            this.parentItem = null
            functionListAdapter?.setList(funcItemList)
            backIconLayout?.visibility = View.GONE
            viewModelProvider(this).get(CheckRootStateEvent::class.java).setCheckRootEvent(true)
        }
    }

    fun showChildList(parentItem: FunctionItem?, isSecondaryMenu: Boolean = false) {
        this.currentFuncType = parentItem?.type
        this.parentItem = parentItem
        parentItem?.apply {
            if (isSecondaryMenu || currentFuncType == FunctionType.TYPE_CUT_SPEED ||
                    currentFuncType == FunctionType.TYPE_CUT_ANIMATION) {
                iv_bottom_func_back.setImageResource(R.drawable.ic_function_back_double)
            } else {
                iv_bottom_func_back.setImageResource(R.drawable.ic_function_back_single)
            }
            functionListAdapter?.setList(getChildList())
            val layoutParams = recyclerView?.layoutParams as LayoutParams
            layoutParams.gravity = when (functionBarThemeConfig!!.childrenAlignInParent) {
                LEFT -> Gravity.LEFT
                CENTER -> Gravity.CENTER
                RIGHT -> Gravity.RIGHT
            }
        }
        backIconLayout?.visibility = View.VISIBLE
    }

    private fun showOperationPanel(funcItem: FunctionItem) {
        viewModelProvider(this).get(FuncItemClickEvent::class.java).setClickedLeafItem(funcItem)
    }

    private fun expandChildren(item: FunctionItem) {
        showChildList(item)
        if (item.type == FunctionType.TYPE_FUNCTION_CUT) {
            viewModelProvider(this).get(EditModeEvent::class.java).setChangeEditMode(true)
        }
        if (item.type == FunctionType.TYPE_FUNCTION_CANVAS) {
            viewModelProvider(this).get(CanvasModeEvent::class.java)
                .setChangeCanvasMode(true)
            showChildList(item)
        }
    }
}

class FunctionListAdapter(private val functionItemClick: IFunctionItemClick) : RecyclerView.Adapter<ItemViewHolder>() {

    var functionList: ArrayList<FunctionItem> = arrayListOf()
        private set

    fun setList(funcItemList: ArrayList<FunctionItem>) {
        functionList.clear()
        functionList.addAll(funcItemList)
        notifyDataSetChanged()   //更新RecyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(layout.btm_holder_function_item, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return functionList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        functionList.apply {
            val functionItem = this[position]
            holder.bindData(functionItem)
            holder.itemView.setOnClickListener {
                if (functionItem.enable) {
                    functionItemClick.onItemClick(functionItem, position)
                }
            }
        }
    }

    fun notifyItemChange(item: FunctionItem) {
        val indexOfFirst = functionList.indexOfFirst { functionItem ->
            item.type == functionItem.type
        }
        if (indexOfFirst >= 0) {
            notifyItemChanged(indexOfFirst)
        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val funcBarThemeConfig = ThemeStore.getFunctionBarViewConfig()
        private val textView: TextView = view.findViewById(id.tv_function_item)
        private val imageView: ImageView = view.findViewById(id.iv_function_item)

        @SuppressLint("ResourceType")
        fun bindData(functionItem: FunctionItem) {
            //set title
            val title = functionItem.titleResId?.let {
                itemView.context.getString(it)
            } ?: functionItem.title

            if (title != null) {
                textView.text = title
                funcBarThemeConfig?.apply {
                    if (itemTextViewSize != 0) {
                        textView.textSize = funcBarThemeConfig.itemTextViewSize.toFloat()
                    }
                    if (itemTextViewColor > 0) {
                        val textColor = ContextCompat.getColor(itemView.context, funcBarThemeConfig.itemTextViewColor)
                        textView.setTextColor(textColor)
                    }
                    if (itemTextTopMargin > 0 && (textView.layoutParams is MarginLayoutParams)) {
                        (textView.layoutParams as MarginLayoutParams).topMargin = UIUtils.dp2px(itemView.context, itemTextTopMargin.toFloat())
                    }
                }
                textView.visibility = View.VISIBLE
            } else {
                textView.visibility = View.GONE
            }
            //set icon
            if (functionItem.icon != null) {
                imageView.setImageResource(functionItem.icon!!)
                imageView.visibility = View.VISIBLE
                funcBarThemeConfig?.also {
                    var itemImageViewWidth = 20 //default width
                    if (funcBarThemeConfig.itemImageViewWidth != 0) {
                        itemImageViewWidth = funcBarThemeConfig.itemImageViewWidth
                    }
                    var itemImageViewHeight = 20 //default height
                    if (funcBarThemeConfig.itemImageViewHeight != 0) {
                        itemImageViewHeight = funcBarThemeConfig.itemImageViewHeight
                    }
                    imageView.layoutParams.apply {
                        width = UIUtils.dp2px(itemView.context, itemImageViewWidth.toFloat())
                        height = UIUtils.dp2px(itemView.context, itemImageViewHeight.toFloat())
                    }
                }
            } else {
                imageView.visibility = View.GONE
            }

            if (!functionItem.enable) {
                textView.alpha = 0.4f
                textView.isEnabled = false
                imageView.alpha = 0.4f
                imageView.isEnabled = false
            } else {
                textView.alpha = 1.0f
                textView.isEnabled = true
                imageView.alpha = 1.0f
                imageView.isEnabled = true
            }
        }
    }
}

interface IFunctionItemClick {
    fun onItemClick(funcItem: FunctionItem, position: Int)
}

