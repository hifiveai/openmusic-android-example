package com.ss.ugc.android.editor.base.draft

import android.app.Application
import android.os.Build
import android.text.TextUtils
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.utils.JsonType
import com.ss.ugc.android.editor.core.utils.GsonUtil
import com.ss.ugc.android.editor.core.utils.Toaster
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardOpenOption

public interface IDraftHelper {

    /**
     * 初始化草稿的存放路径
     */
    public fun onInitDraft()

    /**
     * 删除草稿
     */
    public fun onDeleteDraft(draftID: String)

    /**
     * 存储草稿， 可能是新增也可能是已经存在的草稿
     */
    public fun onSaveDraft(draft: DraftItem)

    /**
     * 通过草稿获取草稿的信息
     */
    public fun onGetDraftById(draftID: String?): DraftItem?

    /**
     * 从文件中读取草稿
     */
    public fun onLoadDrafts(): HashMap<String, DraftItem>

    /**
     * 存储草稿
     */
    public fun onFlush()

    /**
     * 按照某个排序来获取抄稿
     */
    public fun onGetDrafts(): ArrayList<DraftItem>
}

public class DefaultDraftHelper(val application: Application) : IDraftHelper {
    private var draftsFile: File? = null

    private val cacheDrafts: HashMap<String, DraftItem> by lazy {
        onLoadDrafts()
    }

    override fun onInitDraft() {
        // before application.codeCacheDir 覆盖安装后codeCacheDir会重新创建导致草稿箱清空
        val draftDir = EditorSDK.instance.getDraftDir()
        draftsFile = File(draftDir, "VeSdk-drafts.txt").let {
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

    override fun onSaveDraft(draft: DraftItem) {
        cacheDrafts[draft.uuid] = draft
        onFlush()
    }

    override fun onGetDraftById(draftID: String?): DraftItem? {
        return cacheDrafts[draftID]
    }

    override fun onLoadDrafts(): HashMap<String, DraftItem> {
        val readText = draftsFile?.readText()
        return if (TextUtils.isEmpty(readText)) {
            HashMap()
        } else {
            GsonUtil.fromJson(readText, JsonType.genericType<HashMap<String, DraftItem>>())
        }
    }

    override fun onFlush() {
        if (draftsFile == null) {
            throw  NullPointerException("draftsFile not init")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.write(draftsFile?.toPath(), GsonUtil.toJson(cacheDrafts).toByteArray(), StandardOpenOption.TRUNCATE_EXISTING)
        } else {
            writeFileData()
        }
    }

    override fun onGetDrafts(): ArrayList<DraftItem> {
        return ArrayList(cacheDrafts.values.sortedByDescending { it.updateTime })
    }

    @Synchronized
    private fun writeFileData() {
        if (draftsFile == null) {
            throw  NullPointerException("draftsFile not init")
        }
        if (!draftsFile?.exists()!!) {
            draftsFile?.createNewFile()
        }
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(draftsFile)
            fileOutputStream.write(GsonUtil.toJson(cacheDrafts).toByteArray())
        } catch (e: FileNotFoundException) {
            Toaster.show(application.getString(R.string.ck_tips_draft_not_exist))
        } catch (e: IOException) {
            Toaster.show(application.getString(R.string.ck_tips_draft_write_failed))
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                Toaster.show(application.getString(R.string.ck_tips_draft_close_failed))
            }
        }
    }
}