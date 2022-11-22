package com.vesdk.verecorder.record.demo.fragment;

import static com.ss.android.vesdk.VECameraSettings.CAMERA_FLASH_MODE.CAMERA_FLASH_OFF;
import static com.ss.android.vesdk.VECameraSettings.CAMERA_FLASH_MODE.CAMERA_FLASH_ON;
import static com.ss.android.vesdk.VECameraSettings.CAMERA_FLASH_MODE.CAMERA_FLASH_TORCH;
import static com.vesdk.verecorder.record.demo.PreviewActivity.FEATURE_PIC;
import static com.vesdk.verecorder.record.demo.PreviewActivity.FEATURE_VIDEO;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vesdk.vebase.log.LogKit;
import com.ss.android.vesdk.VECameraCapture;
import com.ss.android.vesdk.VEFrameAvailableListener;
import com.ss.android.vesdk.VEImageUtils;
import com.ss.android.vesdk.VERecorder;
import com.ss.android.vesdk.VEUtils;
import com.vesdk.vebase.Constant;
import com.vesdk.vebase.DemoApplication;
import com.vesdk.vebase.LiveDataBus;
import com.vesdk.vebase.ToastUtils;
import com.vesdk.vebase.demo.base.BaseFragment;
import com.vesdk.vebase.old.util.FileUtil;
import com.vesdk.verecorder.R;
import com.vesdk.verecorder.record.demo.PreviewActivity;
import com.vesdk.verecorder.record.demo.adapter.VideoAdapter;
import com.vesdk.verecorder.record.demo.view.CircularProgressView;
import com.vesdk.verecorder.record.demo.view.CountDownDialog;
import com.vesdk.verecorder.record.demo.view.CustomLinearLayout;
import com.vesdk.verecorder.record.preview.model.CountDown;
import com.vesdk.verecorder.record.preview.viewmodel.PreviewModel;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PreviewBottomFragment extends BaseFragment implements CountDownDialog.Callback {
    private static final String TAG = "PreviewBottom";
    public static final int TIME_DELAY = 0;
    private VECameraCapture capture;
    private VERecorder recorder;
    private TextView tv_pic_back, tv_pic_save, tv_editor;
    private LinearLayout llBeauty, llLeft;


    public static final String TAG_EFFECT = "effect";
    public static final String TAG_STICKER = "sticker";
    public static final String TAG_FILTER = "filter";
    public static final String TAG_ALGORITHM = "algorithm";
    public static final String TAG_ANIMOJI = "animoji";
    public static final String TAG_ARSCAN = "arscan";

    private boolean isGoingToEditorActivity = false;

    public static PreviewBottomFragment newInstance() {
        Bundle args = new Bundle();
        PreviewBottomFragment fragment = new PreviewBottomFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private CircularProgressView btStart;
    private RelativeLayout rl_recyclerview;

    private LinearLayout llBottom1;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recorder_fragment_bottom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }


    protected void init(Bundle savedInstanceState) {

        rl_recyclerview = (RelativeLayout) findViewById(R.id.rl_recycleview);
        rl_recyclerview.setVisibility(View.GONE);

        ImageButton ib_go_editor = (ImageButton) findViewById(R.id.ib_go_editor);
        ib_go_editor.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goEditorActivity();
            }
        });

        llBottom1 = (LinearLayout) findViewById(R.id.ll_bottom1);
        btStart = (CircularProgressView) findViewById(R.id.progress);
        btStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCircleStart();
            }
        });
        tv_editor = (TextView) findViewById(R.id.tv_editor); //拍照后 导入剪辑的字体
        tv_pic_back = (TextView) findViewById(R.id.tv_pic_back); //拍照后的返回按钮
        tv_pic_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_pic_back.setVisibility(View.GONE);
                tv_pic_save.setVisibility(View.GONE);
                tv_editor.setVisibility(View.GONE);

                llBeauty.setVisibility(View.VISIBLE);
                llLeft.setVisibility(View.VISIBLE);

                ((PreviewActivity) requireActivity()).showFeature(false);
                btStart.setSelected(!btStart.isSelected());
                capture.startPreview();
            }
        });

        tv_pic_save = (TextView) findViewById(R.id.tv_pic_save); //拍照后的保存按钮
        tv_pic_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LogKit.INSTANCE.d(Constant.TAG, "点击保存");
                ToastUtils.show(getString(R.string.ck_save_success) + picPath);
            }
        });

        tv_pic_back.setVisibility(View.GONE);
        tv_pic_save.setVisibility(View.GONE);
        tv_editor.setVisibility(View.GONE);

        LinearLayout ll_sticker = (LinearLayout) findViewById(R.id.ll_sticker);
        ll_sticker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(TAG_STICKER);
            }
        });
        LinearLayout ll_filter = (LinearLayout) findViewById(R.id.ll_filter);
        ll_filter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(TAG_FILTER);
            }
        });

        llLeft = (LinearLayout) findViewById(R.id.ll_left);
        llBeauty = (LinearLayout) findViewById(R.id.ll_beauty);
        llBeauty.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(TAG_EFFECT);
            }
        });

        customLayout = (CustomLinearLayout) findViewById(R.id.customLayout);
        customLayout.setVisibility(View.INVISIBLE);
        customLayout.setonClick(new CustomLinearLayout.OnCustomClickItem() {
            @Override
            public void onClick(int duration, float speed, CustomLinearLayout.TimeStatus mSelectedTime, CustomLinearLayout.SpeedStatus mSelectedSpeed) {
                currentSpeed = speed;
                // mSelectedTime默认回调为TIME_free  duration默认为-1
                // 合拍模式下 无需更改mCurrentTime和maxDuration
                if (!((PreviewActivity) requireActivity()).getIsDuet()) {
                    mCurrentTime = mSelectedTime;
                    maxDuration = duration * 1000;
                }

            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.bottom_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(DemoApplication.context());  //LinearLayoutManager中定制了可扩展的布局排列接口，子类按照接口中的规范来实现就可以定制出不同排雷方式的布局了
        //配置布局，默认为vertical（垂直布局），下边这句将布局改为水平布局
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        mVideoItemList = new ArrayList<>();
        adapter = new VideoAdapter(mVideoItemList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new VideoAdapter.OnItemClickListener() {
            @Override
            public void onDeleteIconClick(View view, int position) {
                recorder.deleteLastFrag();
                if (mVideoItemList.size() == 0) {
                    llBottom1.setVisibility(View.VISIBLE);
                    rl_recyclerview.setVisibility(View.GONE);
                    ((PreviewActivity) requireActivity()).hideTime();
                }
            }

            @Override
            public void onItemClick(View view, int position) {
                //点击预览拍摄的视频
                String recordedVideoPath = recorder.getRecordedVideoPaths()[position];
                previewExportVideo(recordedVideoPath);
            }
        });

        LiveDataBus.getInstance().with("start", String.class).observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

                clickCircleStart();
            }
        });

        /*
         * 如果是合拍模式 布局需要有些更改
         */
        if (((PreviewActivity) requireActivity()).getIsDuet()) {
            refreshFeature(FEATURE_VIDEO);
            String duetPath = ((PreviewActivity) requireActivity()).getDuetVideoPath();
            maxDuration = VEUtils.getVideoFileInfo(duetPath).duration; // 单位 ms
            LogKit.INSTANCE.d(Constant.TAG, "maxDuration:" + maxDuration);
            customLayout.setDurationHide(true); // 合拍模式下隐藏录制时长选择
            mCurrentTime = CustomLinearLayout.TimeStatus.TIME_15;
        }
    }

    private void previewExportVideo(String exportFilePath) {
        File file = new File(exportFilePath);
        Uri uri;
        String packageName = requireActivity().getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(requireContext(), packageName + ".FileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "video/*");
        startActivity(intent);
    }


    public void inject(VECameraCapture capture, VERecorder recorder, PreviewModel previewModel) {
        this.capture = capture;
        this.recorder = recorder;
        this.mPreviewModel = previewModel;

        if (countDownObserver == null) {
            countDownObserver = new Observer<CountDown>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onChanged(@Nullable CountDown countDown) {
                    LogKit.INSTANCE.d(Constant.TAG, "countDown----" + countDown.name + "  " + countDown.delay);
                    startDialogCountDown(countDown.delay);
                }
            };
            previewModel.countDown.observe(this, countDownObserver);
        }

    }


    /**
     * 点击圆形按钮 开始录制或拍照
     */
    private void clickCircleStart() {
        if (mCurrent_feature == FEATURE_PIC) { // 拍照
            if (mPreviewModel.getCountDown().delay == 0 || btStart.isSelected()) { // //直接开始拍照
                startTakePic();
            } else {
                mPreviewModel.delayRecord(); // 定时录制
            }
        } else if (mCurrent_feature == FEATURE_VIDEO) { // 录像

            if (mPreviewModel.getCountDown().delay == 0 || mCurrentStatus == CameraStatus.Recording) { //直接开始录制 || !isFirst
                takeVideo();
            } else {
                mPreviewModel.delayRecord(); // 定时录制
            }
        }

    }

    private List<Bitmap> mVideoItemList;
    private CustomLinearLayout customLayout;
    private VideoAdapter adapter;

    private void goEditorActivity() {
        if (isGoingToEditorActivity) return;
        isGoingToEditorActivity = true;
        ArrayList<String> videoPaths = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        String[] recordedVideoPaths = recorder.getRecordedVideoPaths();
        //防止每次分段录制的视频名称一致1_frag_v 2_frag_v，导致到编辑页的轨道缓存问题，重命名解决
        for (String recordedVideoPath : recordedVideoPaths) {
            String newName = recordedVideoPath + "_" + currentTime;
            LogKit.INSTANCE.d(Constant.TAG, "片段路径newName:" + newName);
            videoPaths.add(newName);
            new File(recordedVideoPath).renameTo(new File(newName));
        }

        Intent intent = new Intent();
        intent.setPackage(requireActivity().getPackageName());
        intent.setAction("record_sdk_action_ve");
        intent.putExtra("extra_key_from_type", 1);
        intent.putExtra("extra_media_type", 3);
        intent.putStringArrayListExtra("extra_video_paths", videoPaths);
        startActivity(intent);
        requireActivity().finish();
    }

    private String picPath;

    /**
     * 开始拍照
     */
    private void startTakePic() {

        if (btStart.isSelected()) {
            Intent intent = new Intent();
            intent.setPackage(requireActivity().getPackageName());
            intent.setAction("record_sdk_action_ve");
            intent.putExtra("extra_key_from_type", 1);
            intent.putExtra("extra_media_type", 1);
            ArrayList<String> videoPaths = new ArrayList<>();
            videoPaths.add(picPath);
            intent.putStringArrayListExtra("extra_video_paths", videoPaths);
            startActivity(intent);
            requireActivity().finish();
            return;
        }

        int width = 720;
        int height = 1280;
        if (mPreviewModel.getResolution() != null) {
            width = mPreviewModel.getResolution().width;
            height = mPreviewModel.getResolution().height;
        }

        final boolean flashOn = ((PreviewActivity) requireActivity()).getFlashOn();
        capture.switchFlashMode(flashOn ? CAMERA_FLASH_TORCH : CAMERA_FLASH_OFF);

        recorder.shotScreen(width, height, false, true, new VERecorder.IBitmapShotScreenCallback() {
            @Override
            public void onShotScreen(Bitmap bitmap, int ret) {
                if (bitmap == null) {
                    //防止鉴权过去bitmap是null导致崩溃
                    Log.e(TAG, "bitmap == null & ret : " + ret);
                    return;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        btStart.setSelected(!btStart.isSelected());
                        tv_pic_back.setVisibility(View.VISIBLE);
                        tv_pic_save.setVisibility(View.VISIBLE);
                        tv_editor.setVisibility(View.VISIBLE);
                        llBeauty.setVisibility(View.GONE);
                        llLeft.setVisibility(View.GONE);
                    }
                });

                capture.switchFlashMode(flashOn ? CAMERA_FLASH_OFF : CAMERA_FLASH_OFF);
                picPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + "picture_" + System.currentTimeMillis() + ".png";
                LiveDataBus.getInstance().with("fragment", Bitmap.class).postValue(bitmap);
                Log.d(TAG, "onImage: ----" + bitmap + "  " + Thread.currentThread().getName());
                VEImageUtils.compressToJPEG(bitmap, 80, picPath);
                //通知相册刷新
                sendNotice();
            }
        }, false);

    }

    private void sendNotice() {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(picPath)));
        requireActivity().sendBroadcast(intent);
    }

    private CameraStatus mCurrentStatus = CameraStatus.STOP;

    /**
     * 定时结束
     */
    @Override
    public void onFinish() {
        if (mCurrent_feature == FEATURE_PIC) { // 拍照
            startTakePic();
        } else if (mCurrent_feature == FEATURE_VIDEO) { // 录像
            takeVideo();
        }
    }

    public enum CameraStatus {
        STOP,
        Recording
    }


    public boolean isFeatureVideo() {
        return mCurrent_feature == FEATURE_VIDEO;
    }

    /**
     * 开始录制
     */
    public void takeVideo() {
        if (!isFeatureVideo()) {
            ToastUtils.show(getString(R.string.ck_tips_please_click_video_btn));
            return;
        }

        boolean flashOn = ((PreviewActivity) requireActivity()).getFlashOn();
        if (mCurrentStatus == CameraStatus.STOP) {
            if (mCurrentTime != CustomLinearLayout.TimeStatus.TIME_free && recorder.getEndFrameTime() >= maxDuration) {
                LogKit.INSTANCE.d(Constant.TAG, "run到达录制时长1111--------" + recorder.getEndFrameTime());
                ToastUtils.show(getString(R.string.ck_tips_reached_recording_time));
                return;
            }
            capture.switchFlashMode(flashOn ? CAMERA_FLASH_TORCH : CAMERA_FLASH_OFF);
            customLayout.showBottom();
            hideVideoFeature(true); //开始录制时隐藏面板

            btStart.setSelected(true);
            btStart.setBackgroundResource(R.drawable.bt_video_selector_restart); // R.drawable.bg_take_pic_selector
            recorder.startRecord(currentSpeed);
            mCurrentStatus = CameraStatus.Recording;
            // 不是自由时间的时候 才会有进度条
            mHandler.post(timeRunnable);
        } else {
            capture.switchFlashMode(flashOn ? CAMERA_FLASH_ON : CAMERA_FLASH_OFF);
            mHandler.removeCallbacks(timeRunnable);
            LogKit.INSTANCE.d(Constant.TAG, "totalDuration:" + recorder.getEndFrameTime());
            btStart.setSelected(false);
            recorder.stopRecord();
            mCurrentStatus = CameraStatus.STOP;

            btStart.setProgress(0);

            hideVideoFeature(false); //停止录制时展现面板

            if (recorder == null) {
                //防止鉴权无权限崩溃
                LogKit.INSTANCE.e("PreviewBottomFragment", "recorder == null", null);
                return;
            }

            String[] videoPaths = recorder.getRecordedVideoPaths();
            if (videoPaths == null || videoPaths.length <= 0) {
                //防止鉴权无权限崩溃
                LogKit.INSTANCE.e("PreviewBottomFragment", "recorder.getRecordedVideoPaths() is empty", null);
                return;
            }

            //视频缩略图
            VEUtils.getVideoFrames2(videoPaths[videoPaths.length - 1], new int[]{0}, 0, 0, false, new VEFrameAvailableListener() {
                @Override
                public boolean processFrame(ByteBuffer frame, int width, int height, int ptsMs) {
                    Bitmap stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    stitchBmp.copyPixelsFromBuffer(frame.position(0));
                    mVideoItemList.add(stitchBmp);
                    adapter.setList(mVideoItemList);
                    adapter.notifyDataSetChanged();
                    return true;
                }
            });

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LogKit.INSTANCE.d(Constant.TAG, "bottomFragment onPause....");
        // 退到后台 停止录制
        if (mCurrent_feature == FEATURE_VIDEO && mCurrentStatus == CameraStatus.Recording) {
            LogKit.INSTANCE.d(Constant.TAG, "bottomFragment onPause1111111....");
            takeVideo();
        }
    }

    // 点击录制后，是否隐藏面板
    private void hideVideoFeature(boolean hide) {
        rl_recyclerview.setVisibility(hide ? View.GONE : View.VISIBLE);
        llLeft.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
        llBeauty.setVisibility(hide ? View.INVISIBLE : View.VISIBLE);

        if (hide) {
            llBottom1.setVisibility(View.GONE);
            ((PreviewActivity) requireActivity()).hideFeature();
        } else {
            ((PreviewActivity) requireActivity()).showFeature(true);
        }
    }

    private CustomLinearLayout.TimeStatus mCurrentTime = CustomLinearLayout.TimeStatus.TIME_free;
    private int maxDuration = -1 * 1000;
    private float currentSpeed = 1.0f;
    private Observer<CountDown> countDownObserver;

    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            long endFrameTime = recorder.getEndFrameTime();
            LogKit.INSTANCE.d(Constant.TAG, "run--------" + endFrameTime);
            String time = FileUtil.stringForTime((int) endFrameTime);
            ((PreviewActivity) requireActivity()).showTime(time);
            if (mCurrentTime != CustomLinearLayout.TimeStatus.TIME_free) {
                btStart.setMaxDuration(maxDuration);
                btStart.setProgress((int) endFrameTime);
                if (endFrameTime >= maxDuration) {
                    LogKit.INSTANCE.d(Constant.TAG, "run到达录制时长--------" + (endFrameTime));
                    takeVideo();
                    //合拍模式下 直接进入编辑页
                    if (((PreviewActivity) requireActivity()).getIsDuet()) {
                        goEditorActivity();
                    }
                } else {
                    mHandler.postDelayed(this, TIME_DELAY);
                }
            } else {
                mHandler.postDelayed(this, TIME_DELAY);
            }
        }
    };

    private PreviewModel mPreviewModel;

    public void startDialogCountDown(int time) {
        CountDownDialog countDownDialog = new CountDownDialog(requireContext());
        countDownDialog.show();
        countDownDialog.start(time);
        countDownDialog.setCallBack(this);
    }

    /**
     * 定义RecyclerView选项单击事件的回调接口
     */
    public interface OnItemClickListener {

        void onItemClick(String type);
    }

    private OnItemClickListener onClickListener;

    //提供setter方法
    public void setOnClickListener(OnItemClickListener onItemClickListener) {
        this.onClickListener = onItemClickListener;
    }

    private int mCurrent_feature = FEATURE_PIC;

    public void refreshFeature(int current_feature) {
        mCurrent_feature = current_feature;
        if (current_feature == FEATURE_VIDEO) {
            btStart.setBackgroundResource(R.drawable.bt_video_selector); // R.drawable.bg_take_pic_selector
            customLayout.setVisibility(View.VISIBLE);
        } else if (current_feature == FEATURE_PIC) {
            btStart.setBackgroundResource(R.drawable.bt_pic_selector);
            customLayout.setVisibility(View.INVISIBLE);
        }
    }

}
