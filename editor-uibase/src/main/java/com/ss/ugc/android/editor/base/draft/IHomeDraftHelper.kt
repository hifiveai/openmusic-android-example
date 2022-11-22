package com.ss.ugc.android.editor.base.draft

import android.app.Application
import android.text.TextUtils
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.utils.JsonType
import com.ss.ugc.android.editor.core.utils.GsonUtil
import java.io.File

interface IHomeDraftHelper {

    val draftsNum: Int
    val firstDraft: TemplateDraftItem?

    fun obtainDraftsInfo()
}

class DefaultHomeDraftHelper(val app: Application) : IHomeDraftHelper {

    companion object {
        const val DRAFT_FILE_PATHNAME = "VeSdk-template-drafts.txt"
    }

    private var draftsFile: File? = null
    private var draftsList: List<TemplateDraftItem>? = null

    override val draftsNum: Int get() = draftsList?.size ?: 0
    override val firstDraft: TemplateDraftItem? get() = draftsList?.getOrNull(0)

    override fun obtainDraftsInfo() {
        val draftDir = EditorSDK.instance.getDraftDir()
        draftsFile = File(draftDir, DefaultTemplateDraftHelper.DRAFT_FILE_PATHNAME).let {
            if (!it.exists()) {
                it.createNewFile()
            }
            it
        }
        val dataFile: File? = this.draftsFile
        val readText = dataFile?.readText()
        val draftsMap = if (TextUtils.isEmpty(readText)) HashMap<String, TemplateDraftItem>() else GsonUtil.fromJson(
            readText,
            JsonType.genericType<HashMap<String, TemplateDraftItem>>()
        )
        draftsList = draftsMap.values.sortedByDescending { it.updateTime }
    }
}