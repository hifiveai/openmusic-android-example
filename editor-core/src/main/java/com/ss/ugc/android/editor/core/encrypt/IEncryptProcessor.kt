package com.ss.ugc.android.editor.core.encrypt

/**
 *  模板加密解密processor接口
 */
interface IEncryptProcessor {
    /**
     * 加密字符串
     */
    fun encrypt(str: String): String

    /**
     * 解密字符串
     */
    fun decrypt(str: String): String
}