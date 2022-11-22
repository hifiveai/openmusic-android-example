package com.ss.ugc.android.editor.base.constants

import com.ss.ugc.android.editor.base.EditorSDK

object PathConstants {
    val APP_DIR = EditorSDK.instance.getApplication()?.filesDir?.absolutePath ?: ""

    val DOWNLOAD_MUSIC_SAVE_PATH = "$APP_DIR/downloadMusic"

}