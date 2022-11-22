package com.ss.ugc.android.editor.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bytedance.ies.nle.editor_jni.NLETrackSlot;
import com.ss.ugc.android.editor.base.EditorConfig;
import com.ss.ugc.android.editor.base.EditorSDK;
import com.ss.ugc.android.editor.base.fragment.FragmentHelper;
import com.ss.ugc.android.editor.base.monitior.ReportConstants;
import com.ss.ugc.android.editor.base.monitior.ReportUtils;
import com.ss.ugc.android.editor.base.theme.ThemeStore;
import com.ss.ugc.android.editor.base.utils.CommonUtils;
import com.ss.ugc.android.editor.base.utils.SizeUtil;
import com.ss.ugc.android.editor.base.utils.StatusBarUtil;
import com.ss.ugc.android.editor.base.view.CustomPopWindow;
import com.ss.ugc.android.editor.base.view.EditorDialog;
import com.ss.ugc.android.editor.base.view.export.WaitingDialog;
import com.ss.ugc.android.editor.bottom.DefaultBottomPanel;
import com.ss.ugc.android.editor.bottom.IBottomPanel;
import com.ss.ugc.android.editor.bottom.theme.BottomPanelConfig;
import com.ss.ugc.android.editor.core.NLEEditorContext;
import com.ss.ugc.android.editor.core.api.video.IExportStateListener;
import com.ss.ugc.android.editor.core.manager.IVideoPlayer;
import com.ss.ugc.android.editor.core.manager.MediaManager;
import com.ss.ugc.android.editor.core.utils.DLog;
import com.ss.ugc.android.editor.core.utils.Toaster;
import com.ss.ugc.android.editor.preview.PreviewPanel;
import com.ss.ugc.android.editor.preview.PreviewPanelConfig;
import com.ss.ugc.android.editor.track.TrackPanel;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * time : 2020/5/18
 * <p>
 * description :
 * 编辑基础流程 基础操作 API 演示
 */
public class EditorActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String URI_PREVIEW = "%s.FileProvider";

    private ImageView iv_play;
    //    public String bussinessId = null;
    private Button bt_complete, btn_save_cover, save_template_btn;
    private TextView mTv_play_time, resolution, reset_cover;
    private LinearLayout resolution_layout;
    private ConstraintLayout controlBar;
    private ImageView closeFullScreenView, enable_resolution, big_mask;
    private ImageView undoView;
    private ImageView redoView;
    private ImageView fullScreenView;
    private ImageView close_template_iv;
    private TextView template_setting_tv;

    private ImageView ic_close;
    private ImageView iv_add_keyframe;

    private Context mContext = null;
    private final static String TAG = " EditorActivity----";
    private static final String COMPILE_TAG = "HandlingCompile";

    private WaitingDialog waitingDialog;
    private CustomPopWindow resolutionPopWindow;
    private CustomPopWindow fpsPopWindow;
    private CustomPopWindow resolutionFpsPopWindow;

    private final String FRAGMENT_TAG = "exportFragment";

    NLEEditorContext nleEditorContext;
    private IEditorActivityDelegate editorActivityDelegate;

    public IEditorActivityDelegate getEditorActivityDelegate() {
        return editorActivityDelegate;
    }


    private boolean isCoverMode = false;
    private PreviewPanel previewPanel = null;
    private TrackPanel trackPanel = null;

    private Boolean isFullScreen = false;
    private ConstraintLayout fullScreenControl = null;
    private TextView mTv_cur_play_time = null;
    private TextView mTv_total_play_time = null;
    private TextView mTv_eable_template_setting = null;
    private FloatSliderView fullScreenSeekBar = null;
    private IBottomPanel bottomPanel = null;
    private NLETrackSlot currentKeyframe = null;
    FragmentHelper fragmentHelper = new FragmentHelper(R.id.template_fragment_container);
    private MediaManager mediaManager = null;
    private IVideoPlayer videoPlayer = null;
    public Boolean isSetTemplateReplaceableMsg = false;

    private IEditorViewStateListener viewStateListener = new IEditorViewStateListener() {
        @Override
        public void onKeyframeIconSelected(@org.jetbrains.annotations.Nullable NLETrackSlot keyframe) {
            if (keyframe != null) {
                if (currentKeyframe == null) {
                    iv_add_keyframe.setImageResource(R.drawable.ic_keyframe_delete);
                }
                currentKeyframe = keyframe;
            } else {
                iv_add_keyframe.setImageResource(R.drawable.ic_keyframe_add);
                currentKeyframe = null;
            }
        }

        @Override
        public void setKeyframeVisible(boolean visible) {
            if (visible) {
                iv_add_keyframe.setVisibility(View.VISIBLE);
            } else {
                iv_add_keyframe.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onPlayTimeChanged(@NotNull String curPlayerTime, @NotNull String totalPlayerTime) {
            mTv_play_time.setText(String.format("%s/%s", curPlayerTime, totalPlayerTime));
            mTv_cur_play_time.setText(String.format("%s", curPlayerTime));
            mTv_total_play_time.setText(String.format("%s", totalPlayerTime));
            mTv_cur_play_time.setText(String.format("%s", curPlayerTime));
            mTv_total_play_time.setText(String.format("%s", totalPlayerTime));
            DLog.d("update seek progress/" + curPlayerTime + "//" + totalPlayerTime);
            if (isFullScreen && nleEditorContext != null && !nleEditorContext.getVideoPlayer().isPlaying()
                    && nleEditorContext.getVideoPlayer().totalDuration() != 0) {
                //视频播放时动态更新全屏状态下的进度条
                float position = 100 * nleEditorContext.getVideoPlayer().curPosition() / nleEditorContext.getVideoPlayer().totalDuration();
                fullScreenSeekBar.setCurrPosition(position);
            }
        }

        @Override
        public void onPlayViewActivate(boolean activate) {
            iv_play.setActivated(activate);
        }

        @Override
        public void onUndoViewActivate(boolean activate) {
            setViewState(undoView, activate);
        }

        @Override
        public void onRedoViewActivate(boolean activate) {
            setViewState(redoView, activate);
        }

        @Override
        public void onCoverModeActivate(boolean activate) {
            setCoverModeState(activate);
        }
    };

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EditorSDK.getInstance().isInitialized()) {
            DLog.e("EditorSDK has not initialized");
            finish();
            return;
        }
//        bussinessId = this.getIntent().getStringExtra("bussiness_id");
        setContentView(R.layout.activity_genius_edit_tob);

        Window window = getWindow();
        if (window != null) {
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.black_home));
        }
        fullScreenSeekBar = findViewById(R.id.full_screen_seekbar);
        StatusBarUtil.INSTANCE.setStatusBarColor(this, Color.BLACK);
        initView();
        //2.6.4合并代码导致,默认隐藏导出分辨率等按钮,草稿箱进入时因有封面不会回调到此方法导致,导致不显示导出等按钮
        setCoverModeState(false);
        //init PreviewPanel
        this.previewPanel = new PreviewPanel();
        previewPanel.init(new PreviewPanelConfig.Builder().build());
        previewPanel.show(this, R.id.fr_preview);
        //init TrackPanel
        trackPanel = findViewById(R.id.trackPanel);
        //init BottomPanel
        bottomPanel = new DefaultBottomPanel(this, R.id.function_bar_container, R.id.function_panel_container);
        bottomPanel.init(
                new BottomPanelConfig.Builder().build()   //对BottmPanel初始化
        );
        initDraftModule();
        editorActivityDelegate = new EditorActivityDelegate(this, previewPanel, trackPanel, bottomPanel, R.id.function_panel_container);
        editorActivityDelegate.setViewStateListener(viewStateListener);
        editorActivityDelegate.onCreate(savedInstanceState);
        nleEditorContext = editorActivityDelegate.getNleEditorContext();
        mediaManager = new MediaManager(nleEditorContext);
        nleEditorContext.setLoopPlay(EditorSDK.getInstance().config.isLoopPlay());
        editorActivityDelegate.initFullScreen(fullScreenSeekBar);
        videoPlayer = nleEditorContext.getVideoPlayer();
        ReportUtils.INSTANCE.setResolutionRate(nleEditorContext.getChangeResolutionEvent().getValue());
    }

    private void initView() {
        mContext = getApplicationContext();
        fullScreenView = findViewById(R.id.iv_full_screen);   //full screen button
        fullScreenView.setOnClickListener(this);
        controlBar = findViewById(R.id.control);
        waitingDialog = new WaitingDialog(this);
        iv_play = findViewById(R.id.iv_play);
        if (ThemeStore.INSTANCE.getPlayIconRes() != null && ThemeStore.INSTANCE.getPlayIconRes() > 0) {
            iv_play.setImageResource(ThemeStore.INSTANCE.getPlayIconRes());
        }
        close_template_iv = findViewById(R.id.close_template_setting_iv);
        template_setting_tv = findViewById(R.id.tempate_setting_title_tv);
        resolution = findViewById(R.id.resolution);
        resolution_layout = findViewById(R.id.resolution_layout);
        enable_resolution = findViewById(R.id.enable_resolution);
        big_mask = findViewById(R.id.big_mask);
        mTv_play_time = findViewById(R.id.tv_play_time); //显示播放时间
        mTv_cur_play_time = findViewById(R.id.full_screen_current_tv_play_time);
        mTv_total_play_time = findViewById(R.id.full_screen_total_tv_play_time);
        mTv_eable_template_setting = findViewById(R.id.enable_template_setting_tv);
        mTv_eable_template_setting.setOnClickListener(this);
        bt_complete = findViewById(R.id.bt_complete);
        save_template_btn = findViewById(R.id.save_template_btn);
        reset_cover = findViewById(R.id.reset_cover);
        btn_save_cover = findViewById(R.id.btn_save_cover);
        close_template_iv.setOnClickListener(this);
        iv_play.setOnClickListener(this);
        bt_complete.setOnClickListener(this);
        save_template_btn.setOnClickListener(this);
        btn_save_cover.setOnClickListener(this);
        ThemeStore.INSTANCE.setCommonBackgroundRes(bt_complete);
        resolution_layout.setOnClickListener(this);
        findViewById(R.id.close).setOnClickListener(this);
        reset_cover.setOnClickListener(this);
        fullScreenControl = findViewById(R.id.full_screen_control);
        //fullScreenIVPlay = findViewById(R.id.full_screen_iv_play);
        ic_close = findViewById(R.id.close);
        ic_close.setOnClickListener(this);
        closeFullScreenView = findViewById(R.id.iv_close_full_screen);
        closeFullScreenView.setOnClickListener(this);
        if (ThemeStore.INSTANCE.getGlobalUIConfig().getCloseIconRes() != 0) {
            ic_close.setImageResource(ThemeStore.INSTANCE.getGlobalUIConfig().getCloseIconRes());
        }
        iv_add_keyframe = findViewById(R.id.iv_add_keyframe);
        iv_add_keyframe.setOnClickListener(this);
    }

    private void setViewState(View targetView, boolean enable) {
        if (targetView == null) {
            return;
        }
        if (!enable) {
            targetView.setClickable(false);
            targetView.setAlpha(0.5f);
            targetView.setEnabled(false);
        } else {
            targetView.setClickable(true);
            targetView.setAlpha(1.0f);
            targetView.setEnabled(true);
        }
    }

    private void setCoverModeState(boolean activate) {
        if (activate) {
//            resolution.setVisibility(View.INVISIBLE);
            resolution_layout.setVisibility(View.INVISIBLE);
            bt_complete.setVisibility(View.INVISIBLE);
            reset_cover.setVisibility(View.VISIBLE);
            btn_save_cover.setVisibility(View.VISIBLE);
            isCoverMode = true;
        } else {
//            fps.setVisibility(View.VISIBLE);
//            resolution.setVisibility(View.VISIBLE);
            reset_cover.setVisibility(View.INVISIBLE);
            btn_save_cover.setVisibility(View.INVISIBLE);
            resolution_layout.setVisibility(View.VISIBLE);
            bt_complete.setVisibility(View.VISIBLE);
            if (EditorSDK.getInstance().config.getEnableTemplateFunction()) { // 模版功能部分
                bt_complete.setVisibility(View.VISIBLE);
                mTv_eable_template_setting.setVisibility(View.VISIBLE);
            }
            isCoverMode = false;
        }
    }

    private void initDraftModule() {
        undoView = findViewById(R.id.iv_editor_pre);
        if (ThemeStore.INSTANCE.getUndoIconRes() != null && ThemeStore.INSTANCE.getUndoIconRes() > 0) {
            undoView.setImageResource(ThemeStore.INSTANCE.getUndoIconRes());
        }
        undoView.setOnClickListener(view -> {
            if (CommonUtils.isFastClick()) {
                Toaster.show(getString(R.string.ck_tips_submitted_too_often));
            } else {
                if (editorActivityDelegate.canUndo()) {
                    String commitDesc = editorActivityDelegate.getHeadDescription();
                    if (editorActivityDelegate.undo() && !TextUtils.isEmpty(commitDesc)) {
                        if (commitDesc.equals(getString(R.string.ck_delete_keyframe))) {
                            Toaster.show(getString(R.string.ck_redo_keyframe_success));
                        } else {
                            Toaster.show(getString(R.string.ck_undo_toast_prompt, commitDesc));
                        }
                    }
                } else {
                    Toaster.show(getString(R.string.ck_tips_nothing_to_undo));
                }
                updateUndoRedoViewState();
                editorActivityDelegate.updateUndoRedoViewState();
            }
        });
        redoView = findViewById(R.id.iv_editor_behind);
        if (ThemeStore.INSTANCE.getRedoIconRes() != null && ThemeStore.INSTANCE.getRedoIconRes() > 0) {
            redoView.setImageResource(ThemeStore.INSTANCE.getRedoIconRes());
        }
        redoView.setOnClickListener(view -> {
            if (CommonUtils.isFastClick()) {
                Toaster.show(getString(R.string.ck_tips_submitted_too_often));
            } else {
                if (editorActivityDelegate.canRedo()) {
                    if (editorActivityDelegate.redo()) {
                        String commitDesc = editorActivityDelegate.getHeadDescription();
                        if (!TextUtils.isEmpty(commitDesc)) {
                            if (commitDesc.equals(getString(R.string.ck_delete_keyframe))) {
                                Toaster.show(getString(R.string.ck_undo_keyframe_success));
                            } else {
                                Toaster.show(getString(R.string.ck_redo_toast_prompt, commitDesc));
                            }
                        }
                    }
                } else {
                    Toaster.show(getString(R.string.ck_tips_nothing_to_recover));
                }
                updateUndoRedoViewState();
                editorActivityDelegate.updateUndoRedoViewState();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (getCompilerConfig() != null) {
            getCompilerConfig().onEditResume(this);
        }
        if (editorActivityDelegate != null) {
            editorActivityDelegate.onResume();
            updateUndoRedoViewState();
        }
        editorActivityDelegate.onEditorSwitch(nleEditorContext.getVideoPlayer().isPlayWhileEditorSwitch(),
                isFullScreen);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (editorActivityDelegate != null) {
            editorActivityDelegate.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (editorActivityDelegate != null) {
            editorActivityDelegate.onDestroy();
        }
    }

    private void updateUndoRedoViewState() {
        setViewState(undoView, nleEditorContext.canUndo());
        setViewState(redoView, nleEditorContext.canRedo());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (editorActivityDelegate != null) {
            editorActivityDelegate.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastClick()) {
            return;
        }
        if (v.getId() == R.id.iv_play) {
            if (v.isActivated()) {
                //isPlay = false;
                nleEditorContext.getVideoPlayer().setPlayingInFullScreen(false);
                nleEditorContext.getVideoPlayer().pause();
                //iv_play.setImageResource(R.drawable.ic_player_start);
            } else {
                //isPlay = true;
                nleEditorContext.getVideoPlayer().setPlayingInFullScreen(true);
                nleEditorContext.getVideoPlayer().play();
                //iv_play.setImageResource(R.drawable.ic_pause);
            }
        } else if (v.getId() == R.id.close) {
            if (isCoverMode) {
                editorActivityDelegate.cancelCoverAction();
            } else {
//                EditorConfig.IVideoCompilerConfig config = getCompilerConfig();
//                if (config != null && !config.onCustomCloseMethodIntercept(EditorActivity.this)) {
//                    closeEdit();  //如果不是在全屏状态下，点击x，则退出编辑页
//                }
                if (!isFullScreen) { //如果不是在全屏状态下，点击x，则退出编辑页
                    EditorConfig.IVideoCompilerConfig config = getCompilerConfig();
                    if (config != null && !config.onCustomCloseMethodIntercept(EditorActivity.this)) {
                        closeEdit();
                    } else {
                        closeEdit();
                    }
                } else {
                    isFullScreen = false;
                    editorActivityDelegate.closeFullScreen(resolution, bt_complete, fullScreenControl);
                }
            }
        } else if (v.getId() == R.id.resolution_layout) {
//            showResolutionPopBottom();
            showPopBottom();
            big_mask.setVisibility(View.VISIBLE);
            enable_resolution.setImageResource(R.drawable.disable_resolution);

        } else if (v.getId() == R.id.save_template_btn) { // 「保存」 按钮
        } else if (v.getId() == R.id.bt_complete) {
            if (EditorSDK.getInstance().config.getEnableTemplateFunction()) {
            } else {
                compileVideo();
            }


        } else if (v.getId() == R.id.btn_save_cover) {
            editorActivityDelegate.saveCover();
        } else if (v.getId() == R.id.reset_cover) {
            editorActivityDelegate.resetCover();
        } else if (v.getId() == R.id.iv_full_screen) {
            isFullScreen = true;
            editorActivityDelegate.startFullScreen(resolution, bt_complete, fullScreenControl);
            nleEditorContext.getVideoPlayer().refreshCurrentFrame();
        } else if (v.getId() == R.id.iv_close_full_screen) {
            isFullScreen = false;
            editorActivityDelegate.closeFullScreen(resolution, bt_complete, fullScreenControl);
        } else if (v.getId() == R.id.iv_add_keyframe) {
            if (currentKeyframe != null) {
                nleEditorContext.getKeyframeEditor().deleteKeyframe(currentKeyframe);
                nleEditorContext.done(getString(R.string.ck_delete_keyframe));
            } else {
                nleEditorContext.getKeyframeEditor().addOrUpdateKeyframe(true);
                nleEditorContext.done(getString(R.string.ck_keyframe));
            }
        }
    }


    private void handleCompileClick() {
        DLog.d(TAG, "Handling Compile CLick");
        EditorConfig.IVideoCompilerConfig compileActionConfig = EditorSDK.getInstance().config.getCompileActionConfig();
        if (compileActionConfig != null) {
            if (!compileActionConfig.onVideoCompileIntercept(nleEditorContext.getVideoEditor().getVideoDuration(), nleEditorContext.getVideoEditor().calculatorVideoSize(), this)) {
                if (ThemeStore.INSTANCE.getGlobalUIConfig().getApplyLVStyleCompile()) {
                    editorActivityDelegate.pause();
                    triggerExportFragment();
                } else {
                    compileVideo();
                }
            }
        } else {
            if (ThemeStore.INSTANCE.getGlobalUIConfig().getApplyLVStyleCompile()) {
                editorActivityDelegate.pause();
                triggerExportFragment();
            } else {
                compileVideo();
            }
        }
    }



    /**
     * 使用剪映导出样式进行导出
     */
    private void triggerExportFragment() {
        Fragment exportThumbnailFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (exportThumbnailFragment == null) {
            exportThumbnailFragment = new ExportThumbnailFragment();
        }
        DLog.d(TAG, "Adding Fragment");
        this.getSupportFragmentManager().beginTransaction()
                .add(R.id.export_fragment_container,
                        exportThumbnailFragment, FRAGMENT_TAG)
                .commitNowAllowingStateLoss();
//        this.getSupportFragmentManager().beginTransaction()
//                .show(exportThumbnailFragment)
//                .commitNowAllowingStateLoss();
    }

    /**
     * 使用浮标样式进行导出
     */
    public void compileVideo() {
        waitingDialog.setCancelable(false);
        waitingDialog.show();
        editorActivityDelegate.pause();
        editorActivityDelegate.generateCoverImageForDraft();
        EditorExportUtils.INSTANCE.reportExportClickEvent();
        nleEditorContext.getVideoEditor().exportVideo(null, EditorSDK.getInstance().config.isDefaultSaveInAlbum(), mContext,
                EditorSDK.getInstance().config.getWaterMarkPath(), new IExportStateListener() {
                    @Override
                    public void onExportError(int error, int ext, float f, String msg) {
                        EditorExportUtils.INSTANCE.reportExportVideoFinishedEvent(false, error, msg);
                        DLog.d("onCompileError:" + error);
                        waitingDialog.dismiss();
                        Toaster.show("Error:" + error);
                        nleEditorContext.getVideoPlayer().prepare(); //合成后需要重新prepare一下 此时位于0的位置 todo
                    }

                    @Override
                    public void onExportDone() {
                        EditorExportUtils.INSTANCE.reportExportVideoFinishedEvent(true, 0, "");
                        DLog.d("onCompileDone");

                        waitingDialog.setProgress1(getString(R.string.ck_video_synthesis), 0F);
                        waitingDialog.dismiss();

                        nleEditorContext.getVideoPlayer().prepare(); //合成后需要重新prepare一下 此时位于0的位置 todo
                        if (getCompilerConfig() != null) {
                            getCompilerConfig().onVideoCompileDone(nleEditorContext.getVideoEditor().getExportFilePath(), EditorActivity.this);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(new File(nleEditorContext.getVideoEditor().getExportFilePath())));
                            sendBroadcast(intent);
//                            nleEditorContext.getVideoPlayer().seekToPosition(0, SeekMode.EDITOR_SEEK_FLAG_LastSeek, true);
//                            //调用系统播放器播放视频
//                            previewExportVideo(nleEditorContext.getVideoEditor().getExportFilePath());
                            // 点击完成后保存到本地，同时存入草稿箱，提示toast文案“已保存到本地和草稿箱”
                            String draftId = editorActivityDelegate.saveTemplateDraft();
                            if (!TextUtils.isEmpty(draftId)) {
                                Toaster.show(getString(R.string.ck_has_saved_local_and_draft));
                            }
                        }

                    }

                    @Override
                    public void onExportProgress(float progress) {
                        DLog.d("onCompileProgress:" + progress);
                        waitingDialog.setProgress1(getString(R.string.ck_video_synthesis), progress);
                    }
                }, EditorSDK.getInstance().config.isFixedRatio());
    }


    @Override
    public void onBackPressed() {
        if (editorActivityDelegate != null && isFullScreen) {
            editorActivityDelegate.closeFullScreen(resolution, bt_complete, fullScreenControl);
            boolean handled = editorActivityDelegate.onBackPressed();
            if (!handled) {
                editorActivityDelegate.closeFullScreen(resolution, bt_complete, fullScreenControl);
                isFullScreen = false;
            }
            return;
        }
        if (editorActivityDelegate != null) {
            List<Fragment> list = getSupportFragmentManager().getFragments();
            if (!editorActivityDelegate.onBackPressed()) {
                EditorConfig.IVideoCompilerConfig config = getCompilerConfig();
                if (config == null || !config.onCustomCloseMethodIntercept(EditorActivity.this)) {
                    closeEdit();  //如果不是在全屏状态下，点击x，则退出编辑页
                } else {
                    closeEdit();
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//  修复字体功能切换tab会闪一次样式tab选中状态
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            View currentView = getCurrentFocus();
//            if (isHideKeyboard(currentView, ev)) {
//                KeyboardUtils.INSTANCE.hide((EditText) currentView);
//            }
//        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isHideKeyboard(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] location = {0, 0};
            v.getLocationInWindow(location);
            int left = location[0],
                    top = location[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return event.getX() > left && event.getX() < right
                    && event.getY() > bottom;
        }
        return false;
    }

    /**
     * 关闭编辑页面
     */
    private void closeEdit() {
        EditorConfig.IVideoCompilerConfig compilerConfig = EditorSDK.getInstance().config.getCompileActionConfig();
        if (EditorSDK.getInstance().config.getEnableDraftBox()) {
            new EditorDialog.Builder(this)
                    .setTitle(getString(R.string.ck_back_to_home))
                    .setContent(getString(R.string.ck_save_content_hint))
                    .setCancelText(getString(R.string.ck_back))
                    .setCancelListener(() -> {
                        reportEditBackClickEvent(false);
                        afterCloseEdit();
                    })
                    .setConfirmText(getString(R.string.ck_save))
                    .setConfirmListener(() -> {
                        editorActivityDelegate.generateCoverImageForDraft();
                        reportEditBackClickEvent(true);


                        String draftId = editorActivityDelegate.saveTemplateDraft();
                        if (TextUtils.isEmpty(draftId)) {
                            Toaster.show(getString(R.string.ck_tips_draft_save_failed));
                        } else {
                            Toaster.show(getString(R.string.ck_tips_draft_save_success));
                        }
                        afterCloseEdit();
                    })
                    .build()
                    .show();
        } else {
            afterCloseEdit();
        }
    }

    public void reportEditBackClickEvent(boolean isOK) {
        Map<String, String> params = new HashMap<>();
        if (isOK) {
            params.put("action", "confirm");
        } else {
            params.put("action", "cancel");
        }
        ReportUtils.INSTANCE.doReport(ReportConstants.VIDEO_EDIT_BACK_CLICK_EVENT, params);
    }

    private void afterCloseEdit() {
        EditorConfig.IVideoCompilerConfig compilerConfig = getCompilerConfig();
        if (compilerConfig != null && compilerConfig.onCloseEdit(EditorActivity.this)) {
            return;
        }
        finish();
    }

    private EditorConfig.IVideoCompilerConfig getCompilerConfig() {
        return EditorSDK.getInstance().config.getCompileActionConfig();
    }

    private void previewExportVideo(String exportFilePath) {
        File file = new File(exportFilePath);
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String uri_preview = String.format(URI_PREVIEW, getAppId());
            uri = FileProvider.getUriForFile(EditorActivity.this.getApplicationContext(), uri_preview, file);
        } else {
            uri = Uri.fromFile(file);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "video/*");
        startActivity(intent);
    }

    private void showResolutionPopBottom() {
        View pop_resolution = LayoutInflater.from(this).inflate(R.layout.pop_resolution, null);
        handleResolution(pop_resolution);
        resolutionPopWindow = new CustomPopWindow.PopupWindowBuilder(this)
                .setView(pop_resolution)
                .setFocusable(true)
                .setOutsideTouchable(true)
                .create();
        resolutionPopWindow.showAsDropDown(resolution);
    }


    private void showPopBottom() { // use
        View pop_resolution_fps = LayoutInflater.from(this).inflate(R.layout.pop_resolution_fps, null);
        handleResolutionFps(pop_resolution_fps);
        resolutionFpsPopWindow = new CustomPopWindow.PopupWindowBuilder(this)
                .setView(pop_resolution_fps)
                .setFocusable(true)
                .size(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .setOutsideTouchable(true)
                .setOnDissmissListener(mDismissListener)
                .create();
//        resolutionFpsPopWindow.showAtLocation(this.ic_close, Gravity.TOP, 0, 240);
        resolutionFpsPopWindow.showAsDropDown(bt_complete);
    }


    private PopupWindow.OnDismissListener mDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            enable_resolution.setImageResource(R.drawable.enable_resolution);
            big_mask.setVisibility(View.GONE);
        }
    };


    private void initDefaultFpsResolution(View contentView) {

        int resolutionId;
        switch (nleEditorContext.getChangeResolutionEvent().getValue()) {
            case 540:
                resolutionId = R.id.resolution_540p;
                break;
            case 720:
                resolutionId = R.id.resolution_720p;
                break;
            case 2160:
                resolutionId = R.id.resolution_4k;
                break;
            default:
            case 1080:
                resolutionId = R.id.resolution_1080p;
                break;
        }
        contentView.findViewById(resolutionId).setSelected(true);

        int fpsId;
        switch (nleEditorContext.getChangeFpsEvent().getValue()) {
            case 25:
                fpsId = R.id.fps_25;
                break;
            case 30:
                fpsId = R.id.fps_30;
                break;
            case 50:
                fpsId = R.id.fps_50;
                break;
            default:
            case 60:
                fpsId = R.id.fps_60;
                break;
        }
        contentView.findViewById(fpsId).setSelected(true);
        TextView tv_file_size = contentView.findViewById(R.id.file_size_tv);
        {
            long fileSize = mediaManager.calculatorVideoSize();
            tv_file_size.setText(getString(R.string.ck_export_file_size, (float) fileSize));
        }

    }


    private void handleResolutionFps(View contentView) {
        initDefaultFpsResolution(contentView);
        View.OnClickListener listener = v -> {
            int id = v.getId();
            ConstraintLayout parent_layout = (ConstraintLayout) v.getParent();
            if (parent_layout.getId() == R.id.resolution_list) { // 若点击的是分辨率的布局

                int tvCount = parent_layout.getChildCount();
                for (int i = 0; i < tvCount; i++) {
                    View curView = parent_layout.getChildAt(i);
                    curView.setSelected(false);
                }
                if (id == R.id.resolution_540p) {
                    nleEditorContext.getChangeResolutionEvent().setValue(540);
                    ReportUtils.INSTANCE.setResolutionRate(540);
                    resolution.setText("540P");
                    v.setSelected(true);
                } else if (id == R.id.resolution_720p) {
                    nleEditorContext.getChangeResolutionEvent().setValue(720);
                    ReportUtils.INSTANCE.setResolutionRate(720);
                    resolution.setText("720P");
                    v.setSelected(true);
                } else if (id == R.id.resolution_1080p) {
                    nleEditorContext.getChangeResolutionEvent().setValue(1080);
                    ReportUtils.INSTANCE.setResolutionRate(1080);
                    resolution.setText("1080P");
                    v.setSelected(true);
                } else if (id == R.id.resolution_4k) {
                    nleEditorContext.getChangeResolutionEvent().setValue(2160); // 3840X2160
                    ReportUtils.INSTANCE.setResolutionRate(2160);
                    resolution.setText("4K");
                    v.setSelected(true);
                }
            } else if (parent_layout.getId() == R.id.fps_list) {// 若点击的是帧率的布局
                int tvCount = parent_layout.getChildCount();
                for (int i = 0; i < tvCount; i++) {
                    View curView = parent_layout.getChildAt(i);
                    curView.setSelected(false);
                }
                if (id == R.id.fps_25) {
                    nleEditorContext.getChangeFpsEvent().setValue(25);
                    ReportUtils.INSTANCE.setFrameRate(25);
                    v.setSelected(true);
                } else if (id == R.id.fps_30) {
                    nleEditorContext.getChangeFpsEvent().setValue(30);
                    ReportUtils.INSTANCE.setFrameRate(30);
                    v.setSelected(true);
                } else if (id == R.id.fps_50) {
                    nleEditorContext.getChangeFpsEvent().setValue(50);
                    ReportUtils.INSTANCE.setFrameRate(50);
                    v.setSelected(true);
                } else if (id == R.id.fps_60) {
                    nleEditorContext.getChangeFpsEvent().setValue(60);
                    ReportUtils.INSTANCE.setFrameRate(60);
                    v.setSelected(true);
                }
            }


            TextView fileSize_tv = contentView.findViewById(R.id.file_size_tv);
            long fileSize = mediaManager.calculatorVideoSize();
            fileSize_tv.setText(getString(R.string.ck_export_file_size, (float) fileSize));

        };

        contentView.findViewById(R.id.resolution_540p).setOnClickListener(listener);
        contentView.findViewById(R.id.resolution_720p).setOnClickListener(listener);
        contentView.findViewById(R.id.resolution_1080p).setOnClickListener(listener);
        contentView.findViewById(R.id.resolution_4k).setOnClickListener(listener);
        contentView.findViewById(R.id.fps_25).setOnClickListener(listener);
        contentView.findViewById(R.id.fps_30).setOnClickListener(listener);
        contentView.findViewById(R.id.fps_50).setOnClickListener(listener);
        contentView.findViewById(R.id.fps_60).setOnClickListener(listener);
    }

    /**
     * 处理弹出显示内容、点击事件等逻辑
     *
     * @param contentView
     */
    private void handleFps(View contentView) {
        View.OnClickListener listener = v -> {
            if (fpsPopWindow != null) {
                fpsPopWindow.dissmiss();
            }
            int id = v.getId();
            if (id == R.id.tv_fps_25) {
                nleEditorContext.getChangeFpsEvent().setValue(25);
                ReportUtils.INSTANCE.setFrameRate(25);
            } else if (id == R.id.tv_fps_30) {
                nleEditorContext.getChangeFpsEvent().setValue(30);
                ReportUtils.INSTANCE.setFrameRate(30);
            } else if (id == R.id.tv_fps_50) {
                nleEditorContext.getChangeFpsEvent().setValue(50);
                ReportUtils.INSTANCE.setFrameRate(50);
            } else if (id == R.id.tv_fps_60) {
                nleEditorContext.getChangeFpsEvent().setValue(60);
                ReportUtils.INSTANCE.setFrameRate(60);
            }
        };
        ((TextView) contentView.findViewById(R.id.tv_fps_25)).setTextColor(Color.WHITE);
        ((TextView) contentView.findViewById(R.id.tv_fps_30)).setTextColor(Color.WHITE);
        ((TextView) contentView.findViewById(R.id.tv_fps_50)).setTextColor(Color.WHITE);
        ((TextView) contentView.findViewById(R.id.tv_fps_60)).setTextColor(Color.WHITE);
        if (nleEditorContext.getChangeFpsEvent().getValue() == 25) {
            ThemeStore.INSTANCE.setCommonTextColor(contentView.findViewById(R.id.tv_fps_25));
        } else if (nleEditorContext.getChangeFpsEvent().getValue() == 30) {
            ThemeStore.INSTANCE.setCommonTextColor(contentView.findViewById(R.id.tv_fps_30));
        } else if (nleEditorContext.getChangeFpsEvent().getValue() == 50) {
            ThemeStore.INSTANCE.setCommonTextColor(contentView.findViewById(R.id.tv_fps_50));
        } else if (nleEditorContext.getChangeFpsEvent().getValue() == 60) {
            ThemeStore.INSTANCE.setCommonTextColor(contentView.findViewById(R.id.tv_fps_60));
        }

        contentView.findViewById(R.id.tv_fps_25).setOnClickListener(listener);
        contentView.findViewById(R.id.tv_fps_30).setOnClickListener(listener);
        contentView.findViewById(R.id.tv_fps_50).setOnClickListener(listener);
        contentView.findViewById(R.id.tv_fps_60).setOnClickListener(listener);
    }

    private void handleResolution(View contentView) {
        View.OnClickListener listener = v -> {
            if (resolutionPopWindow != null) {
                resolutionPopWindow.dissmiss();
            }
            int id = v.getId();
            if (id == R.id.tv_resolution_540) {
                nleEditorContext.getChangeResolutionEvent().setValue(540);
                ReportUtils.INSTANCE.setResolutionRate(540);
                resolution.setText("540P");
            } else if (id == R.id.tv_resolution_720) {
                nleEditorContext.getChangeResolutionEvent().setValue(720);
                ReportUtils.INSTANCE.setResolutionRate(720);
                resolution.setText("720P");
            } else if (id == R.id.tv_resolution_1080) {
                nleEditorContext.getChangeResolutionEvent().setValue(1080);
                ReportUtils.INSTANCE.setResolutionRate(1080);
                resolution.setText("1080P");
            } else if (id == R.id.tv_resolution_4k) {
                nleEditorContext.getChangeResolutionEvent().setValue(2160); // 3840X2160
                ReportUtils.INSTANCE.setResolutionRate(2160);
                resolution.setText("4K");
            }
        };
        ((TextView) contentView.findViewById(R.id.tv_resolution_540)).setTextColor(Color.WHITE);
        ((TextView) contentView.findViewById(R.id.tv_resolution_720)).setTextColor(Color.WHITE);
        ((TextView) contentView.findViewById(R.id.tv_resolution_1080)).setTextColor(Color.WHITE);
        ((TextView) contentView.findViewById(R.id.tv_resolution_4k)).setTextColor(Color.WHITE);

        if (nleEditorContext.getChangeResolutionEvent().getValue() == 540) {
            ThemeStore.INSTANCE.setCommonTextColor(contentView.findViewById(R.id.tv_resolution_540));
        } else if (nleEditorContext.getChangeResolutionEvent().getValue() == 720) {
            ThemeStore.INSTANCE.setCommonTextColor(contentView.findViewById(R.id.tv_resolution_720));

        } else if (nleEditorContext.getChangeResolutionEvent().getValue() == 1080) {
            ThemeStore.INSTANCE.setCommonTextColor(contentView.findViewById(R.id.tv_resolution_1080));

        } else if (nleEditorContext.getChangeResolutionEvent().getValue() == 2160) {
            ThemeStore.INSTANCE.setCommonTextColor(contentView.findViewById(R.id.tv_resolution_4k));
        }
        contentView.findViewById(R.id.tv_resolution_540).setOnClickListener(listener);
        contentView.findViewById(R.id.tv_resolution_720).setOnClickListener(listener);
        contentView.findViewById(R.id.tv_resolution_1080).setOnClickListener(listener);
        contentView.findViewById(R.id.tv_resolution_4k).setOnClickListener(listener);
    }

    private String getAppId() {
        return getApplicationInfo().packageName;
    }


/*    public void saveDraft() {
        editorActivityDelegate.saveTemplateDraft();
    }*/
}