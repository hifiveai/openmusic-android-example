package com.vesdk.verecorder.record.demo;

import static com.ss.android.vesdk.VECameraSettings.CAMERA_FLASH_MODE.CAMERA_FLASH_OFF;
import static com.ss.android.vesdk.VECameraSettings.CAMERA_FLASH_MODE.CAMERA_FLASH_ON;
import static com.vesdk.vebase.demo.present.contract.StickerContract.TYPE_STICKER;
import static com.vesdk.verecorder.record.demo.fragment.PreviewBottomFragment.TAG_EFFECT;
import static com.vesdk.verecorder.record.demo.fragment.PreviewBottomFragment.TAG_FILTER;
import static com.vesdk.verecorder.record.demo.fragment.PreviewBottomFragment.TAG_STICKER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import  androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import  androidx.annotation.NonNull;
import  androidx.core.app.ActivityCompat;
import  androidx.fragment.app.Fragment;
import  androidx.fragment.app.FragmentManager;
import  androidx.fragment.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bytedance.android.winnow.WinnowHolder;
import com.vesdk.vebase.log.LogKit;
import com.vesdk.vebase.Constant;
import com.vesdk.vebase.LiveDataBus;
import com.vesdk.vebase.ToastUtils;
import com.vesdk.vebase.demo.base.BaseActivity;
import com.vesdk.vebase.demo.fragment.BaseFeatureFragment;
import com.vesdk.vebase.demo.model.ComposerNode;
import com.vesdk.vebase.demo.present.contract.PreviewContract;
import com.vesdk.vebase.old.util.PermissionUtil;
import com.vesdk.verecorder.R;
import com.vesdk.verecorder.record.demo.fragment.EffectFragment;
import com.vesdk.verecorder.record.demo.fragment.PreviewBottomFragment;
import com.vesdk.verecorder.record.demo.fragment.StickerFragment;
import com.vesdk.verecorder.record.demo.fragment.TabStickerFragment;
import com.vesdk.verecorder.record.demo.view.RecordTabView;
import com.vesdk.verecorder.record.preview.function.FeatureView;
import com.vesdk.verecorder.record.preview.model.CountDown;
import com.vesdk.verecorder.record.preview.model.ZoomConfig;
import com.vesdk.verecorder.record.preview.viewmodel.PreviewModel;

import java.io.File;
import java.io.Serializable;


/**
 * 录制界面
 */
public class PreviewActivity extends BaseActivity<PreviewContract.Presenter>
        implements PreviewContract.View, PreviewBottomFragment.OnItemClickListener {

    public static void startPreviewActivity(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        activity.startActivity(intent);
    }

    private SurfaceView surfaceView;
    private ImageView ivCapture;
    private static final String TAG = "Preview";
    private PreviewModel previewModel;
    private View rootView;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    public static final int ANIMATOR_DURATION = 400;

    private StickerFragment mStickerFragment; //道具 (贴纸)
    protected EffectFragment mEffectFragment; //美颜 (特效)
    protected EffectFragment mFilterFragment; //美颜 (滤镜)

    public static final int FEATURE_PIC = 0; //拍照
    public static final int FEATURE_VIDEO = 1; //视频
    public static final int FEATURE_DUET = 2; //合拍
    private int CURRENT_FEATURE = FEATURE_PIC;

    private TextView tvZoom, tv_record_time;
    private RecordTabView recordTab;
    private View tabIndexLine;
    private View topFunction;

    boolean isDuet; //是否为合拍模式,默认false
    String duetVideoPath; //合拍的视频路径
    String duetAudioPath; //合拍的音频路径

    public boolean getIsDuet() {
        return isDuet;
    }

    public String getDuetVideoPath() {
        return duetVideoPath;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_preview);
        isDuet = getIntent().getBooleanExtra(Constant.isDuet, false);
        duetVideoPath = getIntent().getStringExtra(Constant.duetVideoPath);
        duetAudioPath = getIntent().getStringExtra(Constant.duetAudioPath);

        previewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(PreviewModel.class);
        setPresenter(new PreviewPresenter(isDuet, duetVideoPath, duetAudioPath));
        initView();
        mPresenter.initRecorder(surfaceView);
        previewModel.inject(mPresenter.getCapture(), mPresenter.getRecorder());
        initConfig();
        initBottom();
        getLifecycle().addObserver(previewModel);

        LiveDataBus.getInstance().with("fragment", Bitmap.class).observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                LogKit.INSTANCE.d(Constant.TAG,"onChanged----:" + bitmap.toString());
                ivCapture.setVisibility(View.VISIBLE);
                ivCapture.setImageBitmap(bitmap);

                hideFeature();
            }
        });

        //启用横屏拍摄
        previewModel.registerOrientation(getContext());

        generateFragment(TAG_EFFECT);
        mEffectFragment.init(); //初始化美颜效果

        //曝光
        ((SeekBar) findViewById(R.id.ec)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                previewModel.setExposureCompensation(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public PreviewModel getPreviewModel() {
        return previewModel;
    }


    private void initConfig() {
        findViewById(R.id.resolution).performClick();
    }

    private PreviewBottomFragment bottomFragment;
    private boolean flashOn = false;

    public boolean getFlashOn() {
        return flashOn;
    }

    private void initBottom() {

        bottomFragment = PreviewBottomFragment.newInstance();
        bottomFragment.setOnClickListener(this);
        bottomFragment.inject(mPresenter.getCapture(), mPresenter.getRecorder(), previewModel);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, bottomFragment)
                .commit();

    }

    /**
     * 如果是合拍页面 需要隐藏一些不需要显示的按钮
     */
    private void operateDuetUi() {
        if (isDuet) {
            CURRENT_FEATURE = FEATURE_VIDEO;
            recordTab.setVisibility(View.INVISIBLE);
            tabIndexLine.setVisibility(View.INVISIBLE);
            img_duet_change.setVisibility(hasRecord ? View.GONE : View.VISIBLE);
        } else {
            img_duet_change.setVisibility(View.GONE);
        }
    }

    private boolean isFrontCamera = false;
    private ImageView img_duet_change;

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {

        FeatureView featureView = findViewById(R.id.feature);
        featureView.setOnZoomListener(new FeatureView.OnZoomListener() {
            @Override
            public void zoom(float zoom) {
                int temp = (int) zoom / 10;
                tvZoom.setText((temp == 0 ? 1 : temp) + ".0x");
            }
        });

        featureView.setOnFocusEnable(new FeatureView.OnFocusEnable() {
            @Override
            public boolean focusEnable() {
                Fragment showingFragment = showingFragment();
                if (showingFragment instanceof EffectFragment || showingFragment instanceof StickerFragment) {
                    closeFeature(true);
                    return false;
                } else {
                    return true;
                }
            }
        });

        img_duet_change = findViewById(R.id.img_duet_change);
        img_duet_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onSwitchDuet();
            }
        });
        tv_record_time = findViewById(R.id.tv_record_time);
        ivCapture = findViewById(R.id.iv_capture);
        surfaceView = findViewById(R.id.preview);
        recordTab = findViewById(R.id.record_tab);
        topFunction = findViewById(R.id.top_function);
        tabIndexLine = findViewById(R.id.tab_index_line);
        recordTab.setDefaultSelectIndex(0);
        recordTab.setListener(new RecordTabView.OnSelectedListener() {
            @Override
            public void onSelected(WinnowHolder holder) {
                int index = holder.getAdapterPosition();
                switch (index) {
                    case 0: { //拍照
                        CURRENT_FEATURE = FEATURE_PIC;
                        bottomFragment.refreshFeature(CURRENT_FEATURE);
                        tv_record_time.setVisibility(View.GONE);
                    }
                    break;
                    case 1: { //摄像
                        CURRENT_FEATURE = FEATURE_VIDEO;
                        bottomFragment.refreshFeature(CURRENT_FEATURE);
                        tv_record_time.setVisibility(View.VISIBLE);
                    }
                    break;
                }
            }
        });

        //返回
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewActivity.this.finish();
            }
        });

        //变焦
        tvZoom = findViewById(R.id.zoom);
        tvZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZoomConfig config = previewModel.zoomConfigs.get(previewModel.zoomAuto.get());
                ((TextView) v).setText(config.zoom + "X");
                previewModel.zoom(config.zoom == 1.0f ? 0 : config.zoom * 10);
            }
        });
        //定时拍摄
        findViewById(R.id.delay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                CountDown countDown = previewModel.setCountDown();
//                ((ImageView) v).setImageDrawable(getDrawable(countDown.res)); // // getDrawable需要android21 5.0以上才有 5.0以下的手机上会崩溃
                ((ImageView) v).setImageResource(countDown.res);
            }
        });


        //闪光灯
        findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //具体模式参考 VECameraSettings.CAMERA_FLASH_MODE
                v.setSelected(!v.isSelected());
                if (v.isSelected()) {
                    flashOn = true;
                    mPresenter.getCapture().switchFlashMode(CAMERA_FLASH_OFF); // CAMERA_FLASH_TORCH
                } else {
                    flashOn = false;
                    mPresenter.getCapture().switchFlashMode(CAMERA_FLASH_OFF);
                }
            }
        });
        findViewById(R.id.flash).setVisibility(isFrontCamera ? View.GONE : View.VISIBLE); //前置时隐藏闪光灯按钮

        //多画幅
        findViewById(R.id.ratio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasRecord) return;
                int i = previewModel.picAuto.get();
                ((TextView) v).setText(previewModel.pics[i]);
                previewModel.changePic(i);
            }
        });
        findViewById(R.id.ratio).setVisibility(isDuet ? View.GONE : View.VISIBLE); //合拍模式下 不切换画幅


        //输出分辨率
        findViewById(R.id.resolution).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasRecord) return;
                //切换输出分辨率
                int i = previewModel.resolutionAuto.get();
                ((TextView) v).setText(previewModel.RESOLUTIONS_NAME[i]);
                previewModel.changeRatio(i);
            }
        });

        //切换前后摄像头
        findViewById(R.id.switch_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切前后摄像头
                tvZoom.setText("1.0X");
                mPresenter.getCapture().switchCamera();
                //解决前置开启了闪光灯后 到后置拍照不打开闪光灯
                mPresenter.getCapture().switchFlashMode(flashOn ? CAMERA_FLASH_ON : CAMERA_FLASH_OFF);

                isFrontCamera = !isFrontCamera;
                findViewById(R.id.flash).setVisibility(isFrontCamera ? View.GONE : View.VISIBLE);
            }
        });

        rootView = findViewById(R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LogKit.INSTANCE.d(Constant.TAG,"onTouch----");
                closeFeature(true);
                return false;
            }
        });

        // 处理手势按下 抬起的操作(关闭 恢复美颜)
        LiveDataBus.getInstance().with("onPress", String.class).observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s.equals("ACTION_DOWN")) {
                    mPresenter.onNormalDown();
                } else if (s.equals("ACTION_UP")) {
                    mPresenter.onNormalUp();
                }
            }
        });

        operateDuetUi();

    }

    /**
     * 关闭所有的 feature 面板
     * close all feature panel
     * showBoard:代表是否从面板中点击录制后 显示一些控件  防止从道具等面板中点击录制后，会显示顶部工具栏等
     *
     * @return whether close panel successfully 是否成功关闭某个面板，即是否有面板正在开启中
     */
    public boolean closeFeature(boolean showBoard) {
        Fragment showingFragment = showingFragment();
        // 如果有正在展示的面板 并且不是bottom
        if (showingFragment != null && !(showingFragment instanceof PreviewBottomFragment)) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.board_enter, R.anim.board_exit);
            ft.hide(showingFragment).commitNow();

            if (bottomFragment != null) {
                ft.show(bottomFragment).commitNow();
            } else {
                ToastUtils.show("bottomFragment为空了");
            }

            showOrHideBoard(showBoard);

        }

        return showingFragment != null;
    }

    @Override
    public void onStartTask() {

    }

    @Override
    public void changeDuetImage(final String imagePath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(getContext()).load(imagePath).into(img_duet_change);
            }
        });

    }


    @Override
    public Context getContext() {
        return getApplicationContext();
    }


    /**
     * 流程 ⑧
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出界面时若正在录制，需要停止录制，否则容易出现不可预知的crash
//        stopRecord();
        //退出Camera Capture
        if (mPresenter.getCapture() != null) {
            mPresenter.getCapture().destroy();
        }
        //退出Recorder
        if (mPresenter.getRecorder() != null) {
            mPresenter.getRecorder().onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogKit.INSTANCE.d(Constant.TAG,"preview onResume....");

        tvZoom.setText("1.0X");
        requestRecordFunctionPermissions();
    }

    /**
     * 解决 录制预览中，切换后台或锁屏，时长超过1分钟，返回后预览黑屏的问题。
     */
    @Override
    protected void onStop() {
        super.onStop();
        LogKit.INSTANCE.d(Constant.TAG,"preview onStop....");
        mPresenter.getCapture().close();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogKit.INSTANCE.d(Constant.TAG,"preview onRestart....");
        mPresenter.getCapture().open();
    }


    @Override
    public void onItemClick(String type) {

        LogKit.INSTANCE.d(Constant.TAG,"onItemClick:" + type);

        showFeature(type);
        topFunction.setVisibility(View.VISIBLE);
    }

    /**
     * 展示某一个 feature 面板
     * Show a feature panel
     *
     * @param tag tag use to mark Fragment 用于标志 Fragment 的 tag
     */
    protected void showFeature(String tag) {
        if (surfaceView == null) return;
        if (showingFragment() != null) {
            getSupportFragmentManager().beginTransaction().hide(showingFragment()).commitNow();
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.board_enter, R.anim.board_exit);
        Fragment fragment = fm.findFragmentByTag(tag);

        if (fragment == null) {
            fragment = generateFragment(tag);
            if (fragment == null) {
                ToastUtils.show("fragment为空");
                return;
            }
            ft.add(R.id.fragment_container, fragment, tag).show(fragment).commitNow();
        } else {
            ft.show(fragment).commitNow();
        }
        ((BaseFeatureFragment) fragment).refreshIcon(CURRENT_FEATURE);
        showOrHideBoard(false);
    }

    /**
     * 展示或关闭菜单面板
     * show board
     *
     * @param show 展示
     */
    private void showOrHideBoard(boolean show) {
        if (show) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (showingFragment() == null) {

                    }
                    recordTab.setVisibility(View.VISIBLE);
                    topFunction.setVisibility(View.VISIBLE);
                    tabIndexLine.setVisibility(View.VISIBLE);
                    tvZoom.setVisibility(View.VISIBLE);

                    operateDuetUi();
                }
            }, ANIMATOR_DURATION);
        } else {

            recordTab.setVisibility(View.GONE);
            topFunction.setVisibility(View.INVISIBLE);
            tabIndexLine.setVisibility(View.GONE);
            tvZoom.setVisibility(View.GONE);
            img_duet_change.setVisibility(View.GONE);
        }
    }

    private Fragment showingFragment() {
        if (bottomFragment != null && !bottomFragment.isHidden()) {
            return bottomFragment;
        } else if (mFilterFragment != null && !mFilterFragment.isHidden()) {
            return mFilterFragment;
        } else if (mStickerFragment != null && !mStickerFragment.isHidden()) {
            return mStickerFragment;
        } else if (mEffectFragment != null && !mEffectFragment.isHidden()) {
            return mEffectFragment;
        }
        return null;
    }

    private ICheckAvailableCallback mCheckAvailableCallback = new ICheckAvailableCallback() {
        @Override
        public boolean checkAvailable(int id) {

//            LogKit.INSTANCE.d(Constant.TAG,"checkAvailable-- id为" + id);
//            if (mSavedAnimojiPath != null && !mSavedAnimojiPath.equals("")) {
//                ToastUtils.show(getString(R.string.tip_close_animoji_first));
//                return false;
//            }
            return true;
        }
    };


    /**
     * 根据 TAG 创建对应的 Fragment
     * Create the corresponding Fragment based on TAG
     *
     * @param tag tag
     * @return Fragment
     */
    private Fragment generateFragment(String tag) {

        if (tag.equals(TAG_STICKER)) {  // 道具贴纸

            if (mStickerFragment != null) return mStickerFragment;

            StickerFragment stickerFragment = new TabStickerFragment()
                    .setCheckAvailableCallback(mCheckAvailableCallback)
                    .setType(TYPE_STICKER);
            stickerFragment.setCallback(new StickerFragment.IStickerCallback() {
                @Override
                public void onStickerSelected(final File file) {

                    LogKit.INSTANCE.d(Constant.TAG,file == null ? "所选贴纸file为null" : "file路径：" + file.getAbsolutePath());  // "/stickers/weilandongrizhuang"

                    mPresenter.setSticker(file);

                }
            });
            mStickerFragment = stickerFragment;
            return stickerFragment;

        } else if (tag.equals(TAG_EFFECT) || tag.equals(TAG_FILTER)) { // 美颜

            if (mEffectFragment != null && tag.equals(TAG_EFFECT)) {
                return mEffectFragment;
            } else if (mFilterFragment != null && tag.equals(TAG_FILTER)) {
                return mFilterFragment;
            }

            final EffectFragment effectFragment = generateEffectFragment(tag.equals(TAG_FILTER) ? true : false);
            effectFragment.setCheckAvailableCallback(mCheckAvailableCallback)
                    .setCallback(new EffectFragment.IEffectCallback() {

                        @Override
                        public void updateComposeNodes(final String[] nodes) {

                            StringBuilder sb = new StringBuilder();
                            for (String item : nodes) {
                                sb.append(item);
                                sb.append(" ");
                            }
                            LogKit.INSTANCE.d(Constant.TAG,"updateComposeNodes：" + sb.toString()); // eyeshadow/wanxiahong hair/anlan lip/shaonvfen beauty_Android_camera

                            mPresenter.setComposerNodes(nodes);

                        }

                        @Override
                        public void updateComposeNodeIntensity(final ComposerNode node) {

                            LogKit.INSTANCE.d(Constant.TAG,"updateComposeNodeIntensity： node=" + node.getNode() + " key=" + node.getKey() + " progress=" + node.getValue());

                            mPresenter.updateComposerNode(node, true);

                        }

                        // 选择滤镜后，会回调此方法和onFilterValueChanged
                        @Override
                        public void onFilterSelected(final File file) {  //   /Filter_01_38

                            LogKit.INSTANCE.d(Constant.TAG,"onFilterSelected： file=" + (file != null ? file.getAbsolutePath() : "file为null"));

                            mPresenter.onFilterSelected(file);
                        }


                        @Override
                        public void onFilterValueChanged(final float cur) {
                            LogKit.INSTANCE.d(Constant.TAG,"onFilterValueChanged： cur=" + cur);

                            mPresenter.onFilterValueChanged(cur);
                        }

                        @Override
                        public void setEffectOn(final boolean isOn) {
                            LogKit.INSTANCE.d(Constant.TAG,"setEffectOn： isOn=" + isOn);
                        }

                        @Override
                        public void onDefaultClick() {
                            LogKit.INSTANCE.d(Constant.TAG,"onDefaultClick....");
//                            onFragmentWorking(effectFragment);
                        }
                    });
            if (tag.equals(TAG_EFFECT)) {
                mEffectFragment = effectFragment;
            } else if (tag.equals(TAG_FILTER)) {
                mFilterFragment = effectFragment;
            }
            return effectFragment;

        }
        return null;
    }

    private EffectFragment generateEffectFragment(boolean filter) {
        EffectFragment effectFragment = new EffectFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("body", true);
        bundle.putBoolean("only_filter", filter);
        bundle.putSerializable("effect_type", getEffectType());
        effectFragment.setArguments(bundle);
        return effectFragment;
//        return null;
    }


    protected EffectType getEffectType() {
        Serializable s = getIntent().getSerializableExtra("effect_type");
        if (!(s instanceof EffectType)) return EffectType.CAMERA;
        return (EffectType) s;
    }


    /**
     * 定义一个回调接口，用于当用户选择其中一个面板时，
     * 关闭其他面板的回调，此接口由各 Fragment 实现，
     * 在 onClose() 方法中要完成各 Fragment 中 UI 的初始化，
     * 即关闭用户已经开启的开关
     * <p>
     * Define a callback interface for when a user selects one of the panels，
     * close the callback of the other panel, which is implemented by each Fragment
     * In the onClose() method, initialize the UI of each Fragment:
     * turn off the switch that the user has already turned on
     */
    public interface OnCloseListener {
        void onClose();
    }


    public interface ICheckAvailableCallback {
        boolean checkAvailable(int id);
    }


    public enum EffectType {
        CAMERA,
        VIDEO
    }

    public void showTime(String time) {
        LogKit.INSTANCE.d(Constant.TAG,"time:" + time);
        tv_record_time.setText(time);
    }

    //是否已经录制了一段，如果录制了，控制ui上没法点击切换画幅和720P
    private boolean hasRecord = false;

    //isFromTakeVideo是否从录制视频后的操作 如果是 hasRecord = true
    public void showFeature(boolean isFromTakeVideo) {
        if (isFromTakeVideo) hasRecord = true;
        tabIndexLine.setVisibility(View.VISIBLE);
        recordTab.setVisibility(View.VISIBLE);
        topFunction.setVisibility(View.VISIBLE);
        tvZoom.setVisibility(View.VISIBLE);
        ivCapture.setVisibility(View.GONE);

        operateDuetUi();
    }

    public void hideTime() {
        hasRecord = false;
        if (isDuet) img_duet_change.setVisibility(View.VISIBLE);
        tv_record_time.setVisibility(View.GONE);
    }

    public void hideFeature() {
        tabIndexLine.setVisibility(View.GONE);
        recordTab.setVisibility(View.GONE);
        topFunction.setVisibility(View.INVISIBLE);
        tvZoom.setVisibility(View.GONE);
        img_duet_change.setVisibility(View.GONE);

        tv_record_time.setVisibility(CURRENT_FEATURE == FEATURE_PIC ? View.GONE : View.VISIBLE);
    }


    private int PERMISSION_REQUEST_CODE = 0;
    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 申请录制模块需要的权限
     */
    private void requestRecordFunctionPermissions() {
        if (!PermissionUtil.hasPermission(this, permissions)) {
            LogKit.INSTANCE.d(Constant.TAG,"无权限，申请权限----");
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 所有权限都确认完后 会回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (!PermissionUtil.hasPermission(this, permissions)) {
            Toast.makeText(this, getString(R.string.ck_tips_permission_require), Toast.LENGTH_LONG).show();
        } else {
            ToastUtils.show(getString(R.string.ck_tips_permission_granted));
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        Fragment showingFragment = showingFragment();
        if (showingFragment instanceof EffectFragment || showingFragment instanceof StickerFragment) {
            closeFeature(true);
        } else {
            super.onBackPressed();
        }
    }
}
