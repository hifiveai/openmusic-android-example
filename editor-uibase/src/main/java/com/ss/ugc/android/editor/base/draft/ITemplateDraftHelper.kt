package com.ss.ugc.android.editor.base.draft

import android.app.Application
import android.os.Build
import android.text.TextUtils
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.utils.JsonType
import com.ss.ugc.android.editor.core.utils.GsonUtil
import com.ss.ugc.android.editor.core.utils.Toaster
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.UUID

public interface ITemplateDraftHelper {

    /**
     * 初始化草稿的存放路径
     */
    fun onInitDraft()

    /**
     * 删除草稿
     */
    fun onDeleteDraft(draftID: String)

    /**
     * 存储草稿， 可能是新增也可能是已经存在的草稿
     */
    fun onSaveDraft(draft: TemplateDraftItem)

    /**
     *  存储 TemplateInfo
     */
    fun onSaveTemplateInfo(templateInfo: TemplateDraftItem)

    /**
     * 通过草稿获取草稿的信息
     */
    fun onGetDraftById(draftID: String?): TemplateDraftItem?

    fun onGetTemplateInfoById(draftID: String?): TemplateDraftItem?

    /**
     * 从文件中读取草稿
     */
    fun onLoadDrafts(opt: String = DefaultTemplateDraftHelper.DRAFT_FILE_PATHNAME): HashMap<String, TemplateDraftItem>

    /**
     * 存储草稿
     */
    fun onFlush(opt: String = DefaultTemplateDraftHelper.DRAFT_FILE_PATHNAME)

    /**
     * 按照某个排序来获取抄稿
     */
    fun onGetDrafts(): ArrayList<TemplateDraftItem>

    /**
     * 重命名草稿
     */
    fun onRenameDraft(draftID: String, newName: String)

    /**
     * 复制草稿
     */
    fun onDuplicateDraft(draftID: String): TemplateDraftItem?
}

class DefaultTemplateDraftHelper(val application: Application) : ITemplateDraftHelper {

    companion object {
        const val DRAFT_FILE_PATHNAME = "VeSdk-template-drafts.txt"
        const val DRAFT_FILE_NO_TEMPLATEINFO_PATHNAME = "VeSdk-template-drafts-no-templateinfo.txtd"
        const val DRAFT_FILE_TEMPLATEINFO_PATHNAME = "VeSdk-templateinfo-drafts.txt"
    }

    private var draftsFile: File? = null
    private var templateInfosFile: File? = null
    private var draftsNoTemplateInfoFile: File? = null

    private val cacheDrafts: HashMap<String, TemplateDraftItem> by lazy {
        onLoadDrafts()
    }

    private val cacheDraftsNoTemplateInfo: HashMap<String, TemplateDraftItem> by lazy {
        onLoadDrafts(DRAFT_FILE_NO_TEMPLATEINFO_PATHNAME)
    }

    private val cacheTemplateInfo: HashMap<String, TemplateDraftItem> by lazy {
        onLoadDrafts(DRAFT_FILE_TEMPLATEINFO_PATHNAME)
    }

    override fun onRenameDraft(draftID: String, newName: String) {
        cacheDrafts[draftID]?.name = newName
        onFlush()
    }

    override fun onDuplicateDraft(draftID: String): TemplateDraftItem? {
        val curr = cacheDrafts[draftID]
        var duplicate: TemplateDraftItem? = null
        val duplicateTime: Long = System.currentTimeMillis()
        curr?.apply {
            val newName = "${curr.name}(复制)"
            val newUUID = UUID.randomUUID().toString()
            val templateInfoJSON = onGetTemplateInfoById(curr.uuid)?.draftData
            templateInfoJSON?.also {
                onSaveTemplateInfo(
                    TemplateDraftItem(it, 0, "", "", duplicateTime, 0, uuid = newUUID, 0, 0f)
                )
            }

            duplicate = TemplateDraftItem(
                curr.draftData, curr.duration, newName, curr.cover, duplicateTime, curr.slots, newUUID, curr.draftSize, curr.draftRatio
            )
            onSaveDraft(duplicate!!)
        }
        return duplicate
    }

    override fun onInitDraft() {
        val draftDir = EditorSDK.instance.getDraftDir()
        draftsFile = File(draftDir, DRAFT_FILE_PATHNAME).let {
            if (!it.exists()) {
                it.createNewFile()
            }
            it
        }
        templateInfosFile = File(draftDir, DRAFT_FILE_TEMPLATEINFO_PATHNAME).let {
            if (!it.exists()) {
                it.createNewFile()
            }
            it
        }
        draftsNoTemplateInfoFile = File(draftDir, DRAFT_FILE_NO_TEMPLATEINFO_PATHNAME).let {
            if (!it.exists()) {
                it.createNewFile()
            }
            it
        }
    }

    override fun onDeleteDraft(draftID: String) {
        cacheDrafts.remove(draftID)
        onFlush()
    }

    /**
     * 保存完整草稿
     */
    override fun onSaveDraft(draft: TemplateDraftItem) {
        cacheDrafts[draft.uuid] = draft
        onFlush()
    }

    override fun onSaveTemplateInfo(templateInfo: TemplateDraftItem) {
        cacheTemplateInfo[templateInfo.uuid] = templateInfo
        onFlush(DRAFT_FILE_TEMPLATEINFO_PATHNAME)
    }

    override fun onGetDraftById(draftID: String?): TemplateDraftItem? {
        return cacheDrafts[draftID]
    }

    override fun onGetTemplateInfoById(draftID: String?): TemplateDraftItem? {
        return cacheTemplateInfo[draftID]
    }

    override fun onLoadDrafts(opt: String): HashMap<String, TemplateDraftItem> {
        var dataFile: File? = null
        when (opt) {
            DRAFT_FILE_PATHNAME -> {
                dataFile = this.draftsFile
            }
            DRAFT_FILE_NO_TEMPLATEINFO_PATHNAME -> {
                dataFile = this.draftsNoTemplateInfoFile
            }
            DRAFT_FILE_TEMPLATEINFO_PATHNAME -> {
                dataFile = this.templateInfosFile
            }
        }
        val readText = dataFile?.readText()
        return if (TextUtils.isEmpty(readText)) {
            HashMap()
        } else {
            GsonUtil.fromJson(readText, JsonType.genericType<HashMap<String, TemplateDraftItem>>())
        }
    }

    override fun onFlush(opt: String) {
        var dataFile: File? = null
        var dataCache: HashMap<String, TemplateDraftItem>? = null
        when (opt) {
            DRAFT_FILE_PATHNAME -> {
                dataFile = this.draftsFile
                dataCache = this.cacheDrafts
            }
            DRAFT_FILE_NO_TEMPLATEINFO_PATHNAME -> {
                dataFile = this.draftsNoTemplateInfoFile
                dataCache = this.cacheDraftsNoTemplateInfo
            }
            DRAFT_FILE_TEMPLATEINFO_PATHNAME -> {
                dataFile = this.templateInfosFile
                dataCache = this.cacheTemplateInfo
            }
        }
        if (dataFile == null) {
            throw  NullPointerException("draftsFile not init")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.write(dataFile.toPath(), GsonUtil.toJson(dataCache).toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)
        } else {
            writeFileData(dataFile, dataCache!!)
        }
    }

    override fun onGetDrafts(): ArrayList<TemplateDraftItem> {
        return ArrayList(cacheDrafts.values.sortedByDescending { it.updateTime })
    }

    @Synchronized
    private fun writeFileData(dataFile: File, dataCache: HashMap<String, TemplateDraftItem>) {
        if (!dataFile.exists()) {
            dataFile.createNewFile()
        }
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(dataFile)
            fileOutputStream.write(GsonUtil.toJson(dataCache).toByteArray())
        } catch (e: FileNotFoundException) {
            Toaster.show("草稿文件不存在")
        } catch (e: IOException) {
            Toaster.show("草稿写入出错")
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                Toaster.show("草稿文件关闭出错")
            }
        }
    }
}