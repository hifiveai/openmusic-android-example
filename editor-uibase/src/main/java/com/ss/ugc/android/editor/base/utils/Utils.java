package com.ss.ugc.android.editor.base.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    //累计听歌数量
    public static int count;

    //播放模式
    public static final int TYPE_ORDER = 4212;  //顺序播放
    public static final int TYPE_SINGLE = 4313; //单曲循环
    public static final int TYPE_RANDOM = 4414; //随机播放

    // 获取本地音乐封面图片
    public static Bitmap getLocalMusicBmp(ContentResolver res, String musicPic) {
        InputStream in;
        Bitmap bmp = null;
        try {
            Uri uri = Uri.parse(musicPic);
            in = res.openInputStream(uri);
            BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
            bmp = BitmapFactory.decodeStream(in, null, sBitmapOptions);
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    //格式化歌曲时间
    public static String formatTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        Date data = new Date(time);
        return dateFormat.format(data);
    }

    @SuppressLint("DefaultLocale")
    public static String getTimeStr(long duration) {
        long seconds = duration / 1000;
        String format;
        if (seconds / 60 < 10) {
            format = String.format("%02d:%02d", seconds / 60, seconds % 60);
        } else {
            format = String.format("%d:%02d", seconds / 60, seconds % 60);
        }
        return format;
    }
}