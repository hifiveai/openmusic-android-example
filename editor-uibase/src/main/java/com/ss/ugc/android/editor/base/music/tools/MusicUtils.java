package com.ss.ugc.android.editor.base.music.tools;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ss.ugc.android.editor.base.music.data.CoverUrl;
import com.ss.ugc.android.editor.base.music.data.MusicCollection;
import com.ss.ugc.android.editor.base.music.data.MusicCollectionsResponse;
import com.ss.ugc.android.editor.base.music.data.MusicItem;
import com.ss.ugc.android.editor.base.music.data.Song;
import com.ss.ugc.android.editor.base.resource.ResourceHelper;
import com.ss.ugc.android.editor.base.resource.ResourceItem;
import com.ss.ugc.android.editor.core.utils.DLog;
import com.ss.ugc.android.editor.core.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicUtils {

    private static Gson gson = new Gson();

    /**
     * 扫描系统里面的音频文件，返回一个list集合
     */
    public static List<Song> getMusicData1(Context context) {
        List<Song> list = new ArrayList<Song>();

        // 媒体库查询语句（写一个工具类MusicUtils）
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));//歌曲名称
                song.singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));//歌手
                song.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));//专辑名
                song.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//歌曲路径
                song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));//歌曲时长
                song.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));//歌曲大小

                if (song.size > 1000 * 800) {
                    DLog.d("-----", song.toString());
                    // 注释部分是切割标题，分离出歌曲名和歌手 （本地媒体库读取的歌曲信息不规范）
                    try {
                        if (song.song.contains("-")) {
                            String[] str = song.song.split("-");
                            song.singer = str[0];
                            song.song = str[1];
                        }
                        list.add(song);
                    }catch (Exception e){
                        DLog.d("-----exception:", e.getMessage());
                    }

                }
            }
            // 释放资源
            cursor.close();
        }
        return list;
    }

    @TargetApi(Build.VERSION_CODES.Q)
    public static List<MusicItem> getMusicData(Context context) {

        List<MusicItem> list = new ArrayList<>();

        // 媒体库查询语句（写一个工具类MusicUtils）
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        if (cursor != null) {
            int i = 0;
            while (cursor.moveToNext()) {
                MusicItem song = new MusicItem();
                song.id = i++;
                int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID));
                song.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));//歌曲名称
                song.author = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));//歌手
                song.uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//歌曲路径

                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));//歌曲大小
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));//专辑名
                song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)) / 1000;//歌曲时长
                //再通过专辑Id组合出音乐封面的Uri地址
                Uri musicPic = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);

                CoverUrl coverUrl = new CoverUrl();
                coverUrl.setCover_medium(musicPic.toString());
                song.coverUrl = coverUrl;

                if (size > 1000 * 800) {
                    DLog.d("getMusicData1-----", song.toString());
                    // 注释部分是切割标题，分离出歌曲名和歌手 （本地媒体库读取的歌曲信息不规范）
                    if (song.title.contains("-")) {
                        String[] str = song.title.split("-");
                        song.author = str.length > 0 ? str[0] : "";
                        song.title = str.length > 1 ? str[1] : "";
                    }
                    list.add(song);
                }
            }
            // 释放资源
            cursor.close();
        }
        return list;
    }

    public static List<MusicCollection> getSoundEffectsCollection() {
        List<MusicCollection> collectionList = null;
        try {
            final String json = FileUtil.readJsonFile(getSoundsEffectPath() + "sound_collections.json");
            if (!TextUtils.isEmpty(json)) {
                final MusicCollectionsResponse collectionsResponse = gson.fromJson(json, MusicCollectionsResponse.class);
                if (collectionsResponse != null && collectionsResponse.getData() != null) {
                    collectionList = collectionsResponse.getData().getCollections();
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return collectionList;
    }

    private static String getSoundsEffectPath() {
        return new File(ResourceHelper.getInstance().getLocalResourcePath(), "sounds_effect.bundle").getAbsolutePath() + File.separator;
    }

    public static List<MusicItem> getSoundEffectsList() {
        List<ResourceItem> soundList = ResourceHelper.getInstance().getSoundList();
        List<MusicItem> musicItemList = new ArrayList<>();
        for (ResourceItem item : soundList) {
            MusicItem musicItem = new MusicItem();
            musicItem.id = item.getOrder();
            musicItem.duration = (int) (item.getDuration() / 1000);
            musicItem.title = item.getName();
            musicItem.uri = item.getPath();
            musicItem.author = item.getSinger();
            musicItemList.add(musicItem);
        }
        return musicItemList;
    }
}
