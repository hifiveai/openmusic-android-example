package com.ss.ugc.android.editor.base.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.text.TextUtils
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

/**
 * time : 2020/5/15
 * author : tanxiao
 * description :
 * 文件工具类
 */
object FileUtil {
    // "/sdcard/effect_tianmu/FilterResource/Filter/Filter_01_38"
    val ROOT_DIR =
        Environment.getExternalStorageDirectory().path + "/VESDK_Demo/"
    val VEIMAGE_DIR =
        ROOT_DIR + "veimage/"
    val RESOURCE_DIR =
        ROOT_DIR + "resource/"
    val FILTER_DIR =
        RESOURCE_DIR + "filter/"
    val STICKER_RESOURCE_DIR =
        RESOURCE_DIR + "stickers/"

    // gif file magic numbers.
    private val GIF87A = byteArrayOf(0x47, 0x49, 0x46, 0x38, 0x37, 0x61)
    private val GIF89A = byteArrayOf(0x47, 0x49, 0x46, 0x38, 0x39, 0x61)

    // jpeg file magic numbers
    private val JPEG =
        byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

    // png file magic numbers
    private val PNG =
        byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    var FilterList: MutableList<String> =
        ArrayList()
    val PIN_DIR =
        ROOT_DIR + "object_tracking/"
    var PinModelPath = "bingo_objectTracking_v1.0.dat"
    val IMAGE_LIST: MutableList<String> =
        ArrayList()

    @Throws(IOException::class)
    fun initResource(context: Context): Boolean {
        makeDir(ROOT_DIR)
        makeDir(RESOURCE_DIR)
        makeDir(STICKER_RESOURCE_DIR)
        makeDir(VEIMAGE_DIR)
        val fileNames =
            context.resources.assets.list("veimage")
        for (s in fileNames!!) {
            IMAGE_LIST.add("veimage/$s")
        }
        for (s in IMAGE_LIST) {
            try {
                UnZipAssetFolder(
                    context,
                    s,
                    VEIMAGE_DIR
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    @Throws(Exception::class)
    fun UnZipAssetFolder(
        context: Context,
        assetFileName: String,
        outDirName: String
    ) {
        var outDirName = outDirName
        var `in`: InputStream? = null
        var fileExist = true
        try {
            `in` = context.assets.open(assetFileName)
            val dirFile = File(outDirName)
            if (dirFile.exists()) {
                if (dirFile.isFile) {
                    dirFile.delete()
                    dirFile.mkdirs()
                }
            } else {
                dirFile.mkdirs()
            }
            if (true) {
                val folder = File(
                    outDirName + File.separator + GetFileName(
                        assetFileName
                    )
                )
                if (folder.exists()) {
                    deleteDir(folder)
                }
                folder.mkdirs()
                outDirName += File.separator + GetFileName(
                    assetFileName
                )
            }
        } catch (e: FileNotFoundException) {
            fileExist = false
        } finally {
            `in`?.close()
        }
        if (!fileExist) {
            return
        }
        `in` = context.assets.open(assetFileName)
        UnZipFolder(`in`, outDirName)
    }

    @Throws(Exception::class)
    private fun UnZipFolder(
        inputStream: InputStream,
        outDirName: String
    ) {
        var out: FileOutputStream? = null
        var inZip: ZipInputStream? = null
        try {
            var zipEntry: ZipEntry
            var szName = ""
            inZip = ZipInputStream(inputStream)
            while (inZip.nextEntry.also { zipEntry = it } != null) {
                szName = zipEntry.name
                if (zipEntry.isDirectory) {
                    szName = szName.substring(0, szName.length - 1)
                    val folder =
                        File(outDirName + File.separator + szName)
                    if (folder.exists()) {
                        continue
                    }
                    folder.mkdirs()
                } else {
                    val file =
                        File(outDirName + File.separator + szName)
                    if (file.exists()) {
                        break
                    }
                    file.createNewFile()
                    // get the output stream of the file
                    out = FileOutputStream(file)
                    var len: Int
                    val buffer = ByteArray(1024)
                    // read (len) bytes into buffer
                    while (inZip.read(buffer).also { len = it } != -1) {
                        // write (len) byte from buffer at the position 0
                        out.write(buffer, 0, len)
                        out.flush()
                    }
                    out.close()
                }
            } //end of while
        } finally {
            inZip?.close()
            out?.close()
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list()
            //递归删除目录中的子目录下
            for (i in children.indices) {
                val success =
                    deleteDir(
                        File(
                            dir,
                            children[i]
                        )
                    )
                if (!success) {
                    return false
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete()
    }

    fun GetFileName(pathandname: String): String? {
        val start = pathandname.lastIndexOf("/")
        val end = pathandname.lastIndexOf(".")
        return if (end != -1) {
            pathandname.substring(start + 1, end)
        } else {
            null
        }
    }

    fun makeDir(dirPath: String): Boolean {
        if (dirPath.isEmpty()) {
            return false
        }
        val dir = File(dirPath)
        return !dir.exists() && dir.mkdirs()
    }

    /**
     * 校验文件是否有效
     *
     * @param path
     * @return
     */
    fun check(path: String?): Boolean {
        val file = File(path)
        return file.exists() && file.isFile && file.length() > 0
    }

    /**
     * 解压文件
     *
     * @param zipFilePath zip包路径
     * @param destDir     压缩目标路径
     */
    fun unzip(zipFilePath: String?, destDir: String) {
        val dir = File(destDir)
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs()
        val fis: FileInputStream
        //buffer for read and write data to file
        val buffer = ByteArray(1024)
        try {
            fis = FileInputStream(zipFilePath)
            val zis = ZipInputStream(fis)
            var ze = zis.nextEntry
            while (ze != null) {
                val fileName = ze.name
                val newFile =
                    File(destDir + File.separator + fileName)
                println("Unzipping to " + newFile.absolutePath)
                //create directories for sub directories in zip
                File(newFile.parent).mkdirs()
                val fos = FileOutputStream(newFile)
                var len: Int
                while (zis.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close()
                //close this ZipEntry
                zis.closeEntry()
                ze = zis.nextEntry
            }
            //close last ZipEntry
            zis.closeEntry()
            zis.close()
            fis.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(RuntimeException::class)
    fun unZip(srcFile: File, destDirPath: String) {
        val start = System.currentTimeMillis()
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw RuntimeException(srcFile.path + "所指文件不存在")
        }
        // 开始解压
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(srcFile)
            val entries: Enumeration<*> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                if (entry.name.contains("__MACOSX")) {
                    continue
                }
                println("解压" + entry.name)
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory) {
                    val dirPath = destDirPath + "/" + entry.name
                    val dir = File(dirPath)
                    dir.mkdirs()
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    val targetFile = File(destDirPath + "/" + entry.name)
                    // 保证这个文件的父文件夹必须要存在
                    if (!targetFile.parentFile.exists()) {
                        targetFile.parentFile.mkdirs()
                    }
                    targetFile.createNewFile()
                    // 将压缩文件内容写入到这个文件中
                    val `is` = zipFile.getInputStream(entry)
                    val fos = FileOutputStream(targetFile)
                    var len: Int
                    val buf = ByteArray(1024)
                    while (`is`.read(buf).also { len = it } != -1) {
                        fos.write(buf, 0, len)
                    }
                    // 关流顺序，先打开的后关闭
                    fos.close()
                    `is`.close()
                }
            }
            val end = System.currentTimeMillis()
            println("解压完成，耗时：" + (end - start) + " ms")
        } catch (e: Exception) {
            throw RuntimeException("unzip error from ZipUtils", e)
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 解压文件
     *
     * @param inputStream zip包inputStream
     * @param destDir     压缩目标路径
     */
    fun unzip(inputStream: InputStream, destDir: String) {
        val dir = File(destDir)
        deleteDir(dir)
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs()
        //buffer for read and write data to file
        val buffer = ByteArray(1024)
        try {
            val zis = ZipInputStream(inputStream)
            var ze = zis.nextEntry
            while (ze != null) {
                val fileName = ze.name
                //mac 拷贝资源产生的坑
                if (fileName.contains("__MACOSX") || ze.isDirectory) {
                    ze = zis.nextEntry
                    continue
                }
                val newFile =
                    File(destDir + File.separator + fileName)
                println("Unzipping to " + newFile.absolutePath)
                //create directories for sub directories in zip
                File(newFile.parent).mkdirs()
                val fos = FileOutputStream(newFile)
                var len: Int
                while (zis.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close()
                //close this ZipEntry
                zis.closeEntry()
                ze = zis.nextEntry
            }
            //close last ZipEntry
            zis.closeEntry()
            zis.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 复制文件
     *
     * @param context    上下文对象
     * @param zipPath    源文件
     * @param targetPath 目标文件
     * @throws Exception
     */
    @Throws(Exception::class)
    fun copy(
        context: Context,
        zipPath: String?,
        targetPath: String?
    ) {
        if (TextUtils.isEmpty(zipPath) || TextUtils.isEmpty(targetPath)) {
            return
        }
        var exception: Exception? = null
        val dest = File(targetPath)
        dest.parentFile.mkdirs()
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            `in` = BufferedInputStream(context.assets.open(zipPath!!))
            out = BufferedOutputStream(FileOutputStream(dest))
            val buffer = ByteArray(1024)
            var length = 0
            while (`in`.read(buffer).also { length = it } != -1) {
                out.write(buffer, 0, length)
            }
        } catch (e: FileNotFoundException) {
            exception = Exception(e)
        } catch (e: IOException) {
            exception = Exception(e)
        } finally {
            try {
                out!!.close()
                `in`!!.close()
            } catch (e: IOException) {
                exception = Exception(e)
            }
        }
        if (exception != null) {
            throw exception
        }
    }

    /**
     * Copy assets file to folder
     * @param context
     * @param src source path in asset
     * @param dst dst file full path
     * @return
     */
    @JvmStatic
    fun copyAssetFile(
        context: Context,
        src: String?,
        dst: String?
    ): Boolean {
        return try {
            val `in` = context.assets.open(src!!)
            val outFile = File(dst)
            val parentFile = outFile.parentFile
            if (!parentFile.exists()) parentFile.mkdirs()
            val out: OutputStream = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            out.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    //转换成字符串的时间
    private val mFormatBuilder = StringBuilder()
    private val mFormatter = Formatter(
        mFormatBuilder,
        Locale.getDefault()
    )

    /**
     * 把毫秒转换成：1：20：30这样的形式
     * @param timeMs
     * @return
     */
    fun stringForTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format(
                "%d:%02d:%02d",
                hours,
                minutes,
                seconds
            ).toString()
        } else {
            mFormatter.format(
                "%02d:%02d",
                minutes,
                seconds
            ).toString()
        }
    }

    //读取json文件
    @JvmStatic
    fun readJsonFile(fileName: String?): String? {
        var jsonStr = ""
        return try {
            val jsonFile = File(fileName)
            val fileReader = FileReader(jsonFile)
            val reader: Reader =
                InputStreamReader(FileInputStream(jsonFile), "utf-8")
            var ch = 0
            val sb = StringBuffer()
            while (reader.read().also { ch = it } != -1) {
                sb.append(ch.toChar())
            }
            fileReader.close()
            reader.close()
            jsonStr = sb.toString()
            jsonStr
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // 检测文件是否存在
    fun isFileExist(filePath: String?): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return false
        }
        val file = File(filePath)
        return file.exists() && file.length() > 0
    }

    fun getImageType(path: String?): ImageType {
        return if (!TextUtils.isEmpty(path)) {
            getImageType(File(path))
        } else ImageType.UNKNOWN
    }

    fun getImageType(file: File?): ImageType {
        if (null == file || !file.exists()) {
            return ImageType.UNKNOWN
        }
        val type =
            ImageType.UNKNOWN
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            val byteNumber = 8
            val buffer = ByteArray(byteNumber)
            fis.read(buffer)
            if (checkSignature(
                    buffer,
                    GIF89A
                ) || checkSignature(
                    buffer,
                    GIF87A
                )
            ) {
                return ImageType.GIF
            } else if (checkSignature(
                    buffer,
                    JPEG
                )
            ) {
                return ImageType.JPG
            } else if (checkSignature(
                    buffer,
                    PNG
                )
            ) {
                return ImageType.PNG
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fis?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return type
    }

    private fun checkSignature(
        input: ByteArray?,
        signature: ByteArray?
    ): Boolean {
        if (null == input || null == signature) {
            return false
        }
        var success = true
        for (ix in signature.indices) {
            if (input[ix] != signature[ix]) {
                success = false
                break
            }
        }
        return success
    }



    fun writeToFile(data: ByteArray, file: File): Boolean {
        var fos: FileOutputStream? = null
        return try {
            fos = FileOutputStream(file)
            fos.write(data)
            fos.flush()
            true
        } catch (e: IOException) {
            false
        } finally {
            safeClose(fos)
        }
    }


    private fun safeClose(closeable: Closeable?): Boolean {
        if (null != closeable) {
            try {
                closeable.close()
            } catch (e: IOException) {
                return false
            }
        }
        return true
    }
    private const val BITMAP_COMPRESS_QUALITY = 100



    fun saveBmpToFile(bmp: Bitmap?, file: File?, format: Bitmap.CompressFormat): Boolean {
        if (null == bmp || null == file) {
            return false
        }
        val baos = ByteArrayOutputStream()
        bmp.compress(format, BITMAP_COMPRESS_QUALITY, baos)
        return writeToFile(baos.toByteArray(), file)
    }

    fun copy(from: String, to: String, bufferSize: Int = 1024) {
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            input = FileInputStream(File(from))
            output = FileOutputStream(File(to))

            val buffer = ByteArray(bufferSize)
            var len = 0
            while (len >= 0) {
                len = input.read(buffer)
                if (len <= 0) {
                    break
                }
                output.write(buffer, 0, len)
            }
        } catch (ignored: Exception) {
        } finally {
            safeClose(input)
            safeClose(output)
        }
    }

    fun findImageFilePath(dir: String): String? {
        val patternSuffix = "([^\\s]+(?=.(png|PNG|jpg|jpeg)).\\2)"
        val fileDir = File(dir)
        if (!fileDir.isDirectory) {
            return dir
        } else {
            val listFiles = fileDir.listFiles { file ->
                file.length() > 0 && file.name.matches(Regex(patternSuffix))
            }
            if (listFiles.isEmpty()) {
                return null
            }
            return listFiles[0].absolutePath
        }
    }

    @JvmStatic
    fun findDirFirstFilePath(dir: String): String? {
        val fileDir = File(dir)
        if (!fileDir.isDirectory) {
            return null
        } else {
            val listFiles = fileDir.listFiles { file ->
                file.length() > 0
            }
            if (listFiles.isEmpty()) {
                return null
            }
            return listFiles[0].absolutePath;
        }
    }

    enum class ImageType {
        UNKNOWN, JPG, PNG, GIF
    }

    init {
        for (i in 1..19) {
            if (i < 10) {
                FilterList.add("Filter_0$i")
            } else {
                FilterList.add("Filter_$i")
            }
        }
    }
}