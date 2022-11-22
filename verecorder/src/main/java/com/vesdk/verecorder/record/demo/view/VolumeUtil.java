package com.vesdk.verecorder.record.demo.view;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * 声音播放
 */

public class VolumeUtil {


    private MediaPlayer mediaPlayer;

    public void playMusic(Context context,int music) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        mediaPlayer = MediaPlayer.create(context, music);
        mediaPlayer.start();
        mediaPlayer.setVolume(1f, 1f);
    }

    public void releaseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
