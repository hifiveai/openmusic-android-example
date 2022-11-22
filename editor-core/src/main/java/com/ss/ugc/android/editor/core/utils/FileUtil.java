package com.ss.ugc.android.editor.core.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * time : 2020/5/15
 * author : tanxiao
 * description :
 * 文件工具类
 */
public class FileUtil {

    // "/sdcard/effect_tianmu/FilterResource/Filter/Filter_01_38"

    public final static String ROOT_DIR = Environment.getExternalStorageDirectory().getPath() + "/VESDK_Demo/";
    public final static String VEIMAGE_DIR = ROOT_DIR + "veimage/";
    public final static String RESOURCE_DIR = ROOT_DIR + "resource/";
    public final static String FILTER_DIR = RESOURCE_DIR + "filter/";
    public final static String STICKER_RESOURCE_DIR = RESOURCE_DIR + "stickers/";

    public static List<String> FilterList = new ArrayList<>();
    public final static String PIN_DIR = ROOT_DIR + "object_tracking/";
    public static String PinModelPath = "bingo_objectTracking_v1.0.dat";

    static {
        for (int i = 1; i <= 19; i++) {
            if (i < 10) {
                FilterList.add("Filter_0" + i);
            } else {
                FilterList.add("Filter_" + i);
            }
        }
    }

    public final static List<String> IMAGE_LIST = new ArrayList<>();

    public static boolean initResource(Context context) throws IOException {
        makeDir(ROOT_DIR);
        makeDir(RESOURCE_DIR);
        makeDir(STICKER_RESOURCE_DIR);
        makeDir(VEIMAGE_DIR);

        String[] fileNames = context.getResources().getAssets().list("veimage");
        for (String s: fileNames){
            IMAGE_LIST.add("veimage/" +s);
        }

        for (String s : IMAGE_LIST) {
            try {
                FileUtil.UnZipAssetFolder(context, s, VEIMAGE_DIR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }


    public static void UnZipAssetFolder(Context context, String assetFileName, String outDirName) throws Exception {
        InputStream in = null;
        boolean fileExist = true;
        try{
            in = context.getAssets().open(assetFileName);
            File dirFile = new File(outDirName);
            if (dirFile.exists()) {
                if (dirFile.isFile()) {
                    dirFile.delete();
                    dirFile.mkdirs();
                }
            } else {
                dirFile.mkdirs();
            }
            if (true) {
                File folder = new File(outDirName + File.separator + GetFileName(assetFileName));
                if (folder.exists()) {
                    deleteDir(folder);
                }

                folder.mkdirs();
                outDirName += File.separator + GetFileName(assetFileName);
            }
        } catch (FileNotFoundException e) {
            fileExist = false;
        } finally {
            if (in != null) {
                in.close();
            }
        }
        if (!fileExist) {
            return;
        }
        in = context.getAssets().open(assetFileName);

        UnZipFolder(in, outDirName);
    }

    private static void UnZipFolder(InputStream inputStream, String outDirName) throws Exception {
        FileOutputStream out = null;
        ZipInputStream inZip = null;
        try{
            ZipEntry zipEntry;
            String szName = "";
            inZip = new ZipInputStream(inputStream);
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outDirName + File.separator + szName);
                    if (folder.exists()) {
                        continue;
                    }
                    folder.mkdirs();
                } else {
                    File file = new File(outDirName + File.separator + szName);
                    if (file.exists()) {
                        break;
                    }
                    file.createNewFile();
                    // get the output stream of the file
                    out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    // read (len) bytes into buffer
                    while ((len = inZip.read(buffer)) != -1) {
                        // write (len) byte from buffer at the position 0
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }//end of while
        }finally {
            if (inZip != null) {
                inZip.close();
            }
            if (out != null) {
                out.close();
            }
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
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public static String GetFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");

        if (end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }

    }

    public static boolean makeDir(String dirPath) {
        if (dirPath.isEmpty()) {
            return false;
        }
        File dir = new File(dirPath);
        return !dir.exists() && dir.mkdirs();
    }
    /**
     * 校验文件是否有效
     *
     * @param path
     * @return
     */
    public static boolean check(String path) {
        File file = new File(path);
        return file.exists() && file.isFile() && file.length() > 0;
    }

    /**
     * 解压文件
     *
     * @param zipFilePath zip包路径
     * @param destDir     压缩目标路径
     */
    public static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to " + newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unZip(File srcFile, String destDirPath) throws RuntimeException {
        long start = System.currentTimeMillis();
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "所指文件不存在");
        }
        // 开始解压
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(srcFile);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.getName().contains("__MACOSX")){
                    continue;
                }
                System.out.println("解压" + entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = destDirPath + "/" + entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(destDirPath + "/" + entry.getName());
                    // 保证这个文件的父文件夹必须要存在
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("解压完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
    public static void unzip(InputStream inputStream, String destDir) {
        File dir = new File(destDir);
        deleteDir(dir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                //mac 拷贝资源产生的坑
                if (fileName.contains("__MACOSX")||ze.isDirectory()){
                    ze = zis.getNextEntry();
                    continue;
                }
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to " + newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
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
    public static void copy(Context context, String zipPath, String targetPath) throws Exception {
        if (TextUtils.isEmpty(zipPath) || TextUtils.isEmpty(targetPath)) {
            return;
        }
        Exception exception = null;
        File dest = new File(targetPath);
        dest.getParentFile().mkdirs();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(context.getAssets().open(zipPath));
            out = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            exception = new Exception(e);
        } catch (IOException e) {
            exception = new Exception(e);
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                exception = new Exception(e);
            }
        }
        if (exception != null) {
            throw exception;
        }
    }


    /**
     * Copy assets file to folder
     * @param context
     * @param src source path in asset
     * @param dst dst file full path
     * @return
     */
    public static boolean copyAssetFile(Context context, String src, String dst) {
        try {
            InputStream in = context.getAssets().open(src);
            File outFile = new File(dst);
            File parentFile = outFile.getParentFile();
            if (!parentFile.exists()) parentFile.mkdirs();

            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    //转换成字符串的时间
    private static StringBuilder mFormatBuilder = new StringBuilder() ;
    private static Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());


    /**
     * 把毫秒转换成：1：20：30这样的形式
     * @param timeMs
     * @return
     */
    public static String stringForTime(int timeMs){
        int totalSeconds = timeMs/1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds/60)%60;
        int hours = totalSeconds/3600;
        mFormatBuilder.setLength(0);
        if(hours>0){
            return mFormatter.format("%d:%02d:%02d",hours,minutes,seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d",minutes,seconds).toString();
        }
    }

    //读取json文件
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    // 检测文件是否存在
    public static boolean isFileExist( String filePath)  {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }

}
