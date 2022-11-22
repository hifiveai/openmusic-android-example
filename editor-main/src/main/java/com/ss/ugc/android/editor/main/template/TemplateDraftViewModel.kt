package com.ss.ugc.android.editor.main.template

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ss.ugc.android.editor.base.draft.*
import com.ss.ugc.android.editor.core.LifecycleViewModel

enum class ManageState {
    MANAGE_SELECT_EMPTY,
    MANAGE_SELECTED,
    NONE
}

class TemplateDraftViewModel(application: Application) : LifecycleViewModel(application) {

    private var draftHelper: ITemplateDraftHelper? = null
    val manageDraftEvent = MutableLiveData<ManageState>()

    val draftsLiveData = MutableLiveData<ArrayList<TemplateDraftItem>>()

    init {
        draftHelper = DefaultTemplateDraftHelper(application)
        draftHelper?.onInitDraft()
        draftsLiveData.value = draftHelper?.onGetDrafts()
    }

    fun getDraft(uuid: String?): TemplateDraftItem? = draftHelper?.onGetDraftById(uuid)

    fun getTemplateInfo(uuid: String?): TemplateDraftItem? = draftHelper?.onGetTemplateInfoById(uuid)

    fun onSaveDraft(draftItem: TemplateDraftItem) {
        draftHelper?.onSaveDraft(draftItem)
        draftsLiveData.value = draftHelper?.onGetDrafts()
    }

    fun onSaveTemplateInfo(templateInfo: TemplateDraftItem) = draftHelper?.onSaveTemplateInfo(templateInfo)

    fun onRenameDraft(draftId: String, name: String?) {
        if (!name.isNullOrEmpty()) {
            draftHelper?.onRenameDraft(draftId, name)
        }
    }

    fun onDuplicateDraft(item: TemplateDraftItem): TemplateDraftItem? = draftHelper?.onDuplicateDraft(item.uuid)

    fun onDeleteDraft(item: TemplateDraftItem) = draftHelper?.onDeleteDraft(item.uuid)
}