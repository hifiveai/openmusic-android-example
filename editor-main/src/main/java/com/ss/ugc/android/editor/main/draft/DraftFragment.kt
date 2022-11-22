package com.ss.ugc.android.editor.main.draft

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.android.winnow.WinnowAdapter
import com.bytedance.ies.nle.editor_jni.NLEEditor
import com.ss.ugc.android.editor.base.draft.DraftItem
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import com.ss.ugc.android.editor.base.utils.CommonUtils
import com.ss.ugc.android.editor.base.view.EditorDialog
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.main.EditorHelper
import com.ss.ugc.android.editor.main.R
import kotlinx.android.synthetic.main.fragment_draft.*
import kotlinx.android.synthetic.main.item_draft.view.*
import java.io.File

/**
 * time : 2021/2/8
 * description :
 * 草稿页
 */
class DraftFragment : BaseFragment() {

    override fun getContentView(): Int = R.layout.fragment_draft

    private val draftModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application)).get<DraftViewModel>(
            DraftViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rcv_draft.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            val adapter = WinnowAdapter.create(DraftHolder::class.java).addHolderListener(object : WinnowAdapter.HolderListener<DraftHolder>() {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onHolderCreated(holder: DraftHolder) {
                    super.onHolderCreated(holder)
                    holder.itemView.setOnClickListener {

                        if (CommonUtils.isFastClick()) {
                            return@setOnClickListener
                        }

                        //跳转编辑
                        if (activity != null) {
                            if (checkAllResourcesExist(holder.data)) {
                                //todo jeff EditorHelper
                                EditorHelper.onEditorWithDraftID(activity!!, holder.data.uuid)
                                pop()
                            } else {
                                Toaster.show("草稿素材不存在，视频已被删除", Toast.LENGTH_LONG)
                                draftModel?.deleteDraft(holder.data)
                                (adapter as WinnowAdapter).removeItem(holder.data)
                                (adapter as WinnowAdapter).notifyDataSetChanged()
                            }
                        }
                    }
                    holder.itemView.iv_delete.setOnClickListener {
                        EditorDialog.Builder(requireContext())
                            .setTitle(getString(R.string.ck_delete_draft_title))
                            .setContent(getString(R.string.ck_delete_draft_text))
                            .setCancelText(getString(R.string.ck_cancel))
                            .setConfirmText(getString(R.string.ck_confirm))
                            .setConfirmListener(object : EditorDialog.OnConfirmListener  {
                                override fun onClick() {
                                    draftModel?.deleteDraft(holder.data)
                                    (adapter as WinnowAdapter).removeItem(holder.data)
                                    (adapter as WinnowAdapter).notifyDataSetChanged()
                                    updateTitle()
                                }
                            })
                            .build()
                            .show()
                    }
                }
            })
            this.adapter = adapter
            val a = draftModel
            adapter.addItems(draftModel?.drafts)
        }
        updateTitle()
        close.setOnClickListener {
            pop()
        }
        view.setOnClickListener { pop() }
    }
    private fun updateTitle() {
        title.text = getString(R.string.ck_home_drafts) + " ${draftModel.drafts.size}"
    }

    private fun checkAllResourcesExist(draft: DraftItem): Boolean {
        val tempEditor = NLEEditor()
        tempEditor.restore(draft.draftData)

        for (node in tempEditor.allResources) {
            val filePath: String = node.resourceFile
            val file = File(filePath)
            if (!TextUtils.isEmpty(filePath) && !file.exists()) {
                return false
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        draftModel.flush()
    }
}