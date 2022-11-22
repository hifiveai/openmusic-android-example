package com.longyuan.hifive.ui.fragment

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.longyuan.hifive.HiFiveConfig
import com.longyuan.hifive.HiFiveRequestManager
import com.longyuan.hifive.R
import com.longyuan.hifive.adapter.HiFiveMusicAdapter
import com.longyuan.hifive.ui.HiFiveActivity
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import kotlinx.android.synthetic.main.hifive_search_fragemnt.*


/**
 * #duxiaxing
 * #date: 2022/7/18
 */
class HiFiveSearchFragment : BaseFragment(){
    private val pageSize = 20
    private var page = 1
    private var keyWord = ""
    //musicName,albumName,artistName,tagName,不传时默认搜索条件歌名、专辑名、艺人名、标签名
    //private var searchFiled = ""

    override fun getContentView(): Int = R.layout.hifive_search_fragemnt

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        hiFiveSearchIv.setOnClickListener { hiFiveSearchRefresh.autoRefresh() }
        hiFiveSearchInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //执行对应的操作
                keyWord = hiFiveSearchInput.text.toString().trim()
                if (HiFiveConfig.isNetSystemUsable(v.context)){
                    updateData()
                }else HiFiveConfig.showToast(v.context,HiFiveConfig.networkErrorMsg)
                hideShowKeyboard(hiFiveSearchInput)
                return@setOnEditorActionListener true
            }
            false
        }
        hiFiveSearchInput.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hiFiveSearchInputClear.visibility = if (s == null || s.isBlank()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        hiFiveSearchInputClear.setOnClickListener {
            hiFiveSearchInput.setText("")
            hiFiveSearchInputClear.visibility = View.GONE
        }
        hiFiveSearchCancelTv.setOnClickListener { (activity as HiFiveActivity).back() }

        hiFiveSearchRv.layoutManager = LinearLayoutManager(context)
        activity?.let { hiFiveSearchRv.adapter = HiFiveMusicAdapter(it) }
        hiFiveSearchRefresh.setEnableRefresh(false)
        hiFiveSearchRefresh.setOnRefreshListener {
            if (HiFiveConfig.isNetSystemUsable(it.layout.context)) {
                page = 1
                it.setNoMoreData(false)
                updateData()
            }else {
                HiFiveConfig.showToast(it.layout.context,HiFiveConfig.networkErrorMsg)
                it.layout.postDelayed({ it.finishRefresh() },100)
            }
        }
        hiFiveSearchRefresh.setOnLoadMoreListener {
            if (HiFiveConfig.isNetSystemUsable(it.layout.context)) {
                page++
                updateData()
            }else {
                HiFiveConfig.showToast(it.layout.context,HiFiveConfig.networkErrorMsg)
                it.layout.postDelayed({ it.finishLoadMore() },100)
            }
        }
    }

    // 隐藏软键盘
    private fun hideShowKeyboard(editText: EditText) {
        val manager = context?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    private fun updateData(){
        HiFiveRequestManager.updateSearch(keyWord,page,pageSize, failed = { errorInfo ->
            HiFiveConfig.showToast(activity,errorInfo)
            finishRefresh()
        }){ data ->
            (hiFiveSearchRv.adapter as HiFiveMusicAdapter).let { adapter ->
                HiFiveConfig.formatMusicData(data.record).let { musics ->
                    if (page == 1)adapter.setNewInstance(musics) else adapter.addData(musics)
                }
                finishRefresh()
                if (adapter.getData().size >= data.meta.totalCount || data.record.size < pageSize)hiFiveSearchRefresh.setNoMoreData(true)
                hiFiveSearchDefaultLayout.visibility = if (adapter.getData().isNotEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun finishRefresh(){
        hiFiveSearchRefresh.finishRefresh()
        hiFiveSearchRefresh.finishLoadMore()
    }
}