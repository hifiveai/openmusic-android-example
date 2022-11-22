/*
package com.ss.ugc.android.editor.main;

import com.bytedance.ies.nle.editor_jni.NLESegmentInfoSticker;
import com.bytedance.ies.nle.editor_jni.NLESegmentTextSticker;
import com.bytedance.ies.nle.editor_jni.NLETrack;
import com.bytedance.ies.nle.editor_jni.NLETrackSlot;
import com.bytedance.ies.nle.editor_jni.NLETrackType;
import com.bytedance.ies.nlemedia.SeekMode;
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode;
import com.ss.ugc.android.editor.base.logger.ILog;
import com.ss.ugc.android.editor.base.utils.FileUtil;
import com.ss.ugc.android.editor.base.utils.LogUtils;
import com.ss.ugc.android.editor.base.utils.ToastUtils;
import com.ss.ugc.android.editor.base.viewmodel.PreviewStickerViewModel;
import com.ss.ugc.android.editor.bottom.IBottomPanel;
import com.ss.ugc.android.editor.base.functions.FunctionType;
import com.ss.ugc.android.editor.core.NLEEditorContext;
import com.ss.ugc.android.editor.track.ITrackPanel;
import com.ss.ugc.android.editor.track.PlayPositionState;
import com.ss.ugc.android.editor.track.SeekInfo;
import com.ss.ugc.android.editor.track.TrackPanel;
import com.ss.ugc.android.editor.track.TrackPanelActionListener;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class TrackPanelListener  implements TrackPanelActionListener {

    public static int LEFT = 0;
    public static int RIGHT = 1;
    private NLEEditorContext nleEditorContext;

    private TrackPanel trackPanel;
    private IBottomPanel mBottomPanel;

    private final static String TAG = "TrackPanelListener";

    private PreviewStickerViewModel previewStickerViewModel;

    private EditorActivity activity;



    @Override
    public void onStartAndDuration(@NotNull NLETrackSlot slot, int start, int duration, int side) {
//                editorModel.onMainTrackClip(slot, start, duration, side);
        nleEditorContext.getTrackEditor().onMainTrackClip(slot, start, duration, side);
        // 跳转到前面或者后面
        if (side == LEFT) {
            // 跳转到最开始
            trackPanel.updatePlayState(new PlayPositionState(slot.getStartTime(), false, false), true);
        } else {
            trackPanel.updatePlayState(new PlayPositionState(slot.getMeasuredEndTime(), false, false), true);
        }
    }

    @Override
    public void onMainTrackMoveSlot(@NotNull NLETrackSlot nleTrackSlot, int fromIndex, int toIndex) {
        ILog.INSTANCE.d(TAG, "onMainTrackMoveSlot  fromIndex:" + fromIndex + " toIndex:" + toIndex + "\n" + nleTrackSlot);
//                editorModel.onMainTrackMoveSlot(nleTrackSlot, fromIndex, toIndex);
        nleEditorContext.getTrackEditor().onMainTrackMoveSlot(nleTrackSlot, fromIndex, toIndex);
    }

    @Override
    public void onMove(int fromTrackIndex, int toTrackIndex, @NotNull NLETrackSlot slot, long newStart, long curPosition) {
        ILog.INSTANCE.d(TAG, "--onMove fromTrackIndex：" + fromTrackIndex + " toTrackIndex：" + toTrackIndex + " newStart：" + newStart + " curPosition：" + curPosition + "\n" + slot);
//                editorModel.onMove(fromTrackIndex, toTrackIndex, slot, newStart, curPosition);
        nleEditorContext.getTrackEditor().onMove(fromTrackIndex, toTrackIndex, slot, newStart, curPosition);
        if (NLESegmentTextSticker.dynamicCast(slot.getMainSegment()) != null
                || NLESegmentInfoSticker.dynamicCast(slot.getMainSegment()) != null) {
            LogKit.d(Constant.TAG,"onMove 选中贴纸或Text轨道");
            trackPanel.selectSlot(slot);
        }

    }

    @Override
    public void onClip(@NotNull NLETrackSlot slot, long startDiff, long duration) {
        ILog.INSTANCE.d(TAG, "--onClip " + slot + " \nstartDiff: " + startDiff + "\nduration: " + duration);
//                editorModel.onClip(slot, startDiff, duration);
        nleEditorContext.getTrackEditor().onClip(slot, startDiff, duration);
    }

    @Override
    public void onScale(float scaleRatio) {
        ILog.INSTANCE.d(TAG, "onScale " + scaleRatio);
    }

    @Override
    public void onScaleEnd() {
        ILog.INSTANCE.d(TAG, "onScaleEnd ");
    }

    @Override
    public void onScaleBegin() {
        ILog.INSTANCE.d(TAG, "onScaleBegin ");
//                editorModel.pause();
        nleEditorContext.getVideoPlayer().pause();
    }

    @Override
    public void onAddResourceClick() {
        activity.selectVideo(ActivityForResultCode.ADD_VIDEO_REQUEST_CODE, 5);
    }

    @Override
    public void onSegmentSelect(@org.jetbrains.annotations.Nullable NLETrack nleTrack, @org.jetbrains.annotations.Nullable NLETrackSlot nleTrackSlot) {
        ILog.INSTANCE.d(TAG, "onSegmentSelect " + nleTrackSlot + "  track: " + nleTrack);
//                editorModel.updateSelectedTrackSlot(nleTrack, nleTrackSlot);
        nleEditorContext.getTrackEditor().updateSelectedTrackSlot(nleTrack, nleTrackSlot);
        if (nleTrackSlot != null) { // 打开剪辑面板
//                    editorModel.pause(); // 暂停播放等操作
            nleEditorContext.getVideoPlayer().pause();
            if (nleTrack != null && nleTrack.getExtraTrackType() == NLETrackType.VIDEO) {
                videoTrackSelected = true;
                LogKit.d(Constant.TAG,"onSegmentSelect 选中视频轨");
                mBottomPanel.getFunctionNavigator().expandCutFuncItem(nleTrack.getMainTrack());

            } else if (nleTrack != null && nleTrack.getExtraTrackType() == NLETrackType.AUDIO) {
                audioTrackSelected = true;
                LogKit.d(Constant.TAG,"onSegmentSelect 选中音频轨");
                mBottomPanel.getFunctionNavigator().expandAudioFuncItem();
            } else if (nleTrack != null && nleTrack.getExtraTrackType() == NLETrackType.STICKER) {
                stickerTrackSelected = true;
                mBottomPanel.getFunctionNavigator().expandStickerFuncItem();
                if (NLESegmentTextSticker.dynamicCast(nleTrackSlot.getMainSegment()) != null) {
                    LogKit.d(Constant.TAG,"onSegmentSelect 选中Text轨道");
                } else if (NLESegmentInfoSticker.dynamicCast(nleTrackSlot.getMainSegment()) != null) {
                    LogKit.d(Constant.TAG,"onSegmentSelect 选中贴纸轨道");
                }
            } else if (nleTrack != null && nleTrack.getExtraTrackType() == NLETrackType.EFFECT) {
//                        audioTrackSelected = true;
                LogKit.d(Constant.TAG,"onSegmentSelect 选中特效轨");
                ToastUtils.show("选中特效轨道");
//                        mBottomPanel.getFunctionNavigator().expandRootFuncItemByType(FunctionType.VIDEO_EFFECT_SELECT);
                mBottomPanel.getFunctionNavigator().expandFuncItemByType(FunctionType.VIDEO_EFFECT_SELECT);
            }

        } else { // 关闭剪辑面板
            videoTrackSelected = false;
            audioTrackSelected = false;
            stickerTrackSelected = false;
            LogKit.d(Constant.TAG,"取消选中轨道....");
            mBottomPanel.getFunctionNavigator().backToRoot();
            return;
        }

        LogKit.d(Constant.TAG,"start:" + TimeUnit.MICROSECONDS.toMillis(nleTrackSlot.getStartTime()));
        LogKit.d(Constant.TAG,"end  :" + TimeUnit.MICROSECONDS.toMillis(nleTrackSlot.getEndTime()));
        //片段切换后，预览画面切到当前position
//                long playTime = TimeUnit.MILLISECONDS.toMicros(trackPanel.getCurrentSlotInfo().getPlayTime());
        long playTime = trackPanel.getCurrentSlotInfo().getPlayTime();
        if (nleTrackSlot.getStartTime() > playTime) {
            nleEditorContext.getVideoPlayer().seekToPosition((int) TimeUnit.MICROSECONDS.toMillis(nleTrackSlot.getStartTime()) + 1, SeekMode.EDITOR_SEEK_FLAG_LastSeek, true);
        } else if (playTime > nleTrackSlot.getMeasuredEndTime()) {
            nleEditorContext.getVideoPlayer().seekToPosition((int) TimeUnit.MICROSECONDS.toMillis(nleTrackSlot.getMeasuredEndTime()) - 1, SeekMode.EDITOR_SEEK_FLAG_LastSeek, true);
        }
    }

    */
/**
     * SeekInfo(position=19634, autoPlay=false, seekFlag=0, seekPxSpeed=0.0625, seekDurationSpeed=0.41666666)
     *//*

    @Override
    public void onVideoPositionChanged(@NotNull SeekInfo seek) {
        // 用户滑动轨道，位置变化
//                TrackLog.INSTANCE.d(TAG, "onVideoPositionChanged " + seek);
        if (seek.isFromUser()) {
            nleEditorContext.getVideoPlayer().pause();
            nleEditorContext.getVideoPlayer().seekToPosition((int) TimeUnit.MICROSECONDS.toMillis(seek.getPosition()), SeekMode.values()[seek.getSeekFlag()], false);
            activity.mTv_play_time.setText(FileUtil.stringForTime(nleEditorContext.getVideoPlayer().curPosition()) + "/" + FileUtil.stringForTime(nleEditorContext.getVideoPlayer().totalDuration()));

        } else {
            // TODO: 1/10/21 不是用户主动触发
        }
//                editorModel.onVideoPositionChange(seek.getPosition());
        nleEditorContext.getTrackEditor().onVideoPositionChange(seek.getPosition());
        previewStickerViewModel.onVideoPositionChange(seek.getPosition());
    }


    @Override
    public void onTransitionClick(@NotNull NLETrackSlot segment, @NotNull NLETrackSlot nextSegment) {
        ILog.INSTANCE.d(TAG, "get current slot info  " + trackPanel.getCurrentSlotInfo());
        LogKit.d(Constant.TAG,"onTransitionClick segment: " + segment + "  nextSegment: " + nextSegment);
//                editorModel.updateTransitionTrackSlot(segment, nextSegment);
        nleEditorContext.getTrackEditor().updateTransitionTrackSlot(segment, nextSegment);
        mBottomPanel.getFunctionNavigator().showTransactionPanel();
    }
}
*/
