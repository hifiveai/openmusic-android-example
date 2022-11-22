package com.ss.ugc.android.editor.main.draft

import android.app.Application
import androidx.annotation.Keep
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.draft.DefaultDraftHelper
import com.ss.ugc.android.editor.base.draft.DraftItem
import com.ss.ugc.android.editor.base.draft.IDraftHelper
import com.ss.ugc.android.editor.core.LifecycleViewModel

/**
 * time : 2021/2/19
 * author : tanxiao
 * description :
 * 草稿箱 viewModel
 */
@Keep
class DraftViewModel(application: Application) : LifecycleViewModel(application) {

    var draftHelper: IDraftHelper? = null
    init {
        draftHelper = if (EditorSDK.instance.config.draftHelper == null) {
            DefaultDraftHelper(application)
        } else {
            EditorSDK.instance.config.draftHelper
        }
        draftHelper?.onInitDraft()

    }

    val drafts: ArrayList<DraftItem>
        get() = draftHelper?.onGetDrafts() ?: ArrayList()


    @Synchronized
    fun saveDraft(draft: DraftItem) {
        draftHelper?.onSaveDraft(draft)
    }

    @Synchronized
    fun deleteDraft(draftItem: DraftItem) {
        draftHelper?.onDeleteDraft(draftItem.uuid)

    }


    fun getDraft(uuid: String?): DraftItem? {
        return draftHelper?.onGetDraftById(uuid)
    }

    @Synchronized
    fun loadDrafts(): HashMap<String, DraftItem> {
        return draftHelper?.onLoadDrafts() ?: HashMap()
    }

    @Synchronized
    fun flush() {
        draftHelper?.onFlush()
    }


}