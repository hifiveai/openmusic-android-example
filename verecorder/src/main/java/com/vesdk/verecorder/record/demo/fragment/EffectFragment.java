package com.vesdk.verecorder.record.demo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.vesdk.vebase.log.LogKit;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.vesdk.vebase.Constant;
import com.vesdk.vebase.LiveDataBus;
import com.vesdk.vebase.demo.fragment.BaseFeatureFragment;
import com.vesdk.vebase.demo.model.ComposerNode;
import com.vesdk.vebase.demo.model.EffectBackup;
import com.vesdk.vebase.demo.model.EffectButtonItem;
import com.vesdk.vebase.demo.present.contract.ItemGetContract;
import com.vesdk.verecorder.R;
import com.vesdk.verecorder.record.demo.EffectContract;
import com.vesdk.verecorder.record.demo.EffectPresenter;
import com.vesdk.verecorder.record.demo.PreviewActivity;
import com.vesdk.vebase.view.ProgressBar;
import com.vesdk.verecorder.record.demo.adapter.FragmentVPAdapter;
import com.vesdk.verecorder.record.demo.adapter.OnPageChangeListenerAdapter;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.vesdk.vebase.demo.present.contract.ItemGetContract.MASK;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.NODE_BEAUTY_CAMERA;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.NODE_RESHAPE_LIVE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_BODY_ENHANCE_HIP;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_FACE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_BRIGHTEN_EYE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_EYE_PLUMP;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_REMOVE_POUCH;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_SINGLE_TO_DOUBLE_EYELID;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_SMILE_FOLDS;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_RESHAPE_WHITEN_TEETH;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_CLOSE;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_FILTER;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_BLUSHER;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_EYEBROW;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_EYESHADOW;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_FACIAL;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_HAIR;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_LIP;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_OPTION;
import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_MAKEUP_PUPIL;
import static com.vesdk.verecorder.record.demo.PreviewActivity.FEATURE_PIC;
import static com.vesdk.verecorder.record.demo.PreviewActivity.FEATURE_VIDEO;


public class EffectFragment extends
        BaseFeatureFragment<EffectContract.Presenter, EffectFragment.IEffectCallback>
        implements PreviewActivity.OnCloseListener, MakeupOptionFragment.IMakeupOptionCallback, View.OnClickListener, ItemGetContract.View, EffectContract.View {
    private int POSITION_BEAUTY = 0;
    private int POSITION_RESHAPE = 1;
    private int POSITION_BODY = 2;
    private int POSITION_MAKEUP = 3;
    private int POSITION_FILTER = 4;

    public static final String TAG_MAKEUP_OPTION_FRAGMENT = "makeup_option";
    public static final int ANIMATION_DURATION = 400;

    public static final float NO_VALUE = -1F;

    // view
    private ProgressBar pb;
    private TabLayout tl;
    private TextView tvTitle;
    private ImageView ivCloseMakeupOption;
    private ViewPager vp;

    private List<Fragment> mFragmentList;
    // 当前选择的效果类型，如磨皮等
    // current effect type
    private int mSelectType = TYPE_CLOSE;
    // current fragment
    private IRefreshFragment mSelectFragment;
    // 效果强度表
    // key 为小项 id，TYPE_BEAUTY_FACE_SMOOTH,TYPE_BEAUTY_FACE_SHARP...，value 为小项强度，0 ~ 1
    private SparseArray<Float> mProgressMap = new SparseArray<>();
    // 每一个 Fragment 中选中的效果，以及美妆各小项的选择情况
    // fragment 的选中效果的 key 为大项的 id，TYPE_BEAUTY_FACE,TYPE_BEAUTY_RESHAPE...，value 是小项的 id，TYPE_BEAUTY_FACE_SMOOTH...
    // 美妆小项的选择情况以小项的 id 为 key，TYPE_MAKEUP_LIP,TYPE_MAKEUP_BLUSHER...，value 是小项的位置，0,1,2...
    // 二者虽然不是一种计量方式，但由于 value 差着几个量级，所以不会出现 key 重复的问题，
    // 此举为了统一小项点亮状态的保存
    private SparseIntArray mSelectMap = new SparseIntArray();
    // 所有选中的效果集合
    // all selected effect
    private SparseArray<ComposerNode> mComposerNodeMap = new SparseArray<>();

    private String mSavedFilterPath;
    private PreviewActivity.ICheckAvailableCallback mCheckAvailableCallback;
    private boolean effectEnable = true;
    private EffectBackup mBackup = new EffectBackup();

    public EffectFragment() {
        setPresenter(new EffectPresenter());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recorder_fragment_effect, container, false);
    }

    private FrameLayout frameLayout_top;
    private LinearLayout ll_top, ll_top_left;
    private boolean only_filter; // 是否仅有特效面板
    private LinearLayout ll_to_normal;
    private RelativeLayout rl_progress;
    private View tv_reset;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rl_progress = view.findViewById(R.id.rl_progress);
        frameLayout_top = view.findViewById(R.id.fragment_top); // fragment的上部分
        ll_top = view.findViewById(R.id.ll_top);
        ll_top_left = view.findViewById(R.id.ll_top_left);
        ll_to_normal = view.findViewById(R.id.ll_to_normal);

        ll_to_normal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        /**
                         * 对比按钮按下，关闭无美颜美妆
                         */
//                        onNormalDown();
                        LiveDataBus.getInstance().with("onPress", String.class).postValue("ACTION_DOWN");
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        /**
                         * 对比按钮松开，恢复美颜美妆
                         */
//                        onNormalUp();
                        LiveDataBus.getInstance().with("onPress", String.class).postValue("ACTION_UP");
                        break;
                }
                return true;
            }
        });


        ll_top.setBackgroundColor(ActivityCompat.getColor(getContext(), R.color.black_all)); // 美体
        Bundle bundle = getArguments();
        if (null != bundle && bundle.getBoolean("only_filter")) {
            only_filter = bundle.getBoolean("only_filter");
//            frameLayout_top.setVisibility(only_filter ? View.GONE : View.VISIBLE);
            ll_top.setVisibility(only_filter ? View.GONE : View.VISIBLE);
            ll_to_normal.setVisibility(View.GONE);
        } else {
            ll_top_left.setVisibility(View.GONE);
        }

        rl_progress.setVisibility(View.INVISIBLE);

        pb = view.findViewById(R.id.pb_effect);
        tl = view.findViewById(R.id.tl_identify);
        tvTitle = view.findViewById(R.id.tv_title_identify);
        ivCloseMakeupOption = view.findViewById(R.id.iv_close_makeup_option);
        vp = view.findViewById(R.id.vp_identify);
        pb.setOnProgressChangedListener(new ProgressBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(ProgressBar progressBar, float progress, boolean isFormUser, int eventAction) {
                if (isFormUser) {
                    LogKit.INSTANCE.d(Constant.TAG, "拖动进度onProgressChanged isFormUser:" + progress);
                    dispatchProgress(progress);
                }
            }
        });
        ivCloseMakeupOption.setOnClickListener(this);
        initVP();

        tv_reset = view.findViewById(R.id.tv_reset);
        // 重置按钮：把所有参数恢复默认 开启
        tv_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                dispatchProgress(only_filter? 0.8f : 0.2f);

                if (tabPosition == 0) {
                    onDefaultClick(true);
                    ((BeautyFaceFragment) beautyFragment).setOn(isBeautyOn = true);
                } else if (tabPosition == 1) { //微整形

                    onReshapeDefaultClick(true);
                    ((BeautyFaceFragment) reshapeFragment).setOn(isReshapeOn = true);

                } else if (tabPosition == 2) {

                    onBodyDefaultClick(true);
                    ((BeautyFaceFragment) bodyFragment).setOn(isBeautyOn = true);
//                    closeBodyEffect(TYPE_BEAUTY_BODY,true );
//                    mPresenter.addDefaultBodyNodes(mComposerNodeMap);
                } else if (tabPosition == 3) {
                    onMakeupDefaultClick(true);
                    ((BeautyFaceFragment) makeupFragment).setOn(isBeautyOn = true);
                }

            }
        });

        tv_reset.setVisibility(only_filter ? View.GONE : View.VISIBLE);

        view.findViewById(R.id.iv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((PreviewActivity) getActivity()).closeFeature(true);
            }
        });
        bottom_start = view.findViewById(R.id.bottom_start);
        bottom_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogKit.INSTANCE.d(Constant.TAG, "点击了面板上的开始按钮 onTouch----");
                ((PreviewActivity) getActivity()).closeFeature(false);
                LiveDataBus.getInstance().with("start", String.class).postValue("start");
            }
        });

    }

    private View bottom_start;  // 开始拍摄的按钮键

    @Override
    public void onResume() {
        super.onResume();

        if (effectEnable) {
//            recoverState();
        }
    }

    private void initVP() {
        mFragmentList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();

        if (only_filter) {
            // 滤镜
            // filter
            mFragmentList.add(new FilterFragment().setSelectMap(mSelectMap)
                    .setCheckAvailableCallback(mCheckAvailableCallback)
                    .setCallback(new FilterFragment.IFilterCallback() {
                        @Override
                        public void onFilterSelected(File file, int position) {
                            // 选择无滤镜的时候 隐藏 滑竿
                            rl_progress.setVisibility(file == null ? View.INVISIBLE : View.VISIBLE);
                            mSelectType = TYPE_FILTER;
                            mSelectMap.put(TYPE_FILTER, position);
                            mSavedFilterPath = file == null ? null : file.getAbsolutePath();
                            updateFilter(mSavedFilterPath);
                            // 选中滤镜之后初始化强度
//                            dispatchProgress(mPresenter.getDefaultValue(mSelectType));
                            Float value = mProgressMap.get(mSelectType);
                            float progress = value == null ? mPresenter.getDefaultValue(mSelectType) : value;
                            dispatchProgress(progress);
                        }
                    }));
            titleList.add(getString(R.string.ck_tab_filter));
        } else {
            // 美颜
            // beauty face
            mFragmentList.add(beautyFragment);
            titleList.add(getString(R.string.ck_tab_face_beautification));

            mFragmentList.add(reshapeFragment);
            titleList.add(getString(R.string.ck_tab_face_beauty_reshape));

            // 美体
            // beauty body
            Bundle bundle = getArguments();
            if (null != bundle && bundle.getBoolean("body")) {
                mFragmentList.add(bodyFragment);
                titleList.add(getString(R.string.ck_tab_face_beauty_body));
            } else {
                // todo temp fix方案
                POSITION_MAKEUP -= 1;
                POSITION_FILTER -= 1;
            }
            // 美妆
            // make
            mFragmentList.add(makeupFragment);

            //初始值
            mSelectMap.put(TYPE_MAKEUP_BLUSHER, 2);
            mSelectMap.put(TYPE_MAKEUP_LIP, 4);
            mSelectMap.put(TYPE_MAKEUP_FACIAL, 3);
            mSelectMap.put(TYPE_MAKEUP_PUPIL, 2);
            mSelectMap.put(TYPE_MAKEUP_EYESHADOW, 4);
            mSelectMap.put(TYPE_MAKEUP_EYEBROW, 3);
            mSelectMap.put(TYPE_MAKEUP_HAIR, 1); //染发

            titleList.add(getString(R.string.ck_tab_face_makeup));
        }


        mSelectFragment = (IRefreshFragment) mFragmentList.get(0);
        FragmentVPAdapter adapter = new FragmentVPAdapter(getChildFragmentManager(),
                mFragmentList, titleList);
        vp.setAdapter(adapter);
        vp.setOffscreenPageLimit(mFragmentList.size());
        vp.addOnPageChangeListener(new OnPageChangeListenerAdapter() {
            @Override
            public void onPageSelected(int position) {
                tabPosition = position;

                mSelectType = mSelectMap.get(positionToType1(position), TYPE_CLOSE);
                pb.setNegativeable(mSelectType == TYPE_BEAUTY_BODY_ENHANCE_HIP);

                Float aFloat = mProgressMap.get(mSelectType, NO_VALUE);
                pb.setProgress(aFloat);

                if (mFragmentList.get(position) instanceof IRefreshFragment) {
                    mSelectFragment = (IRefreshFragment) mFragmentList.get(position);
                }


                showOrHideProgressBar(aFloat == NO_VALUE ? false : true);
            }
        });
        tl.setupWithViewPager(vp);

    }

    private int tabPosition = 0;

    public EffectFragment setCheckAvailableCallback(PreviewActivity.ICheckAvailableCallback callback) {
        mCheckAvailableCallback = callback;
        return this;
    }

    /**
     * 将进度分发出去，有两个出口
     * 1、分到对应的 Fragment 中供其更改 UI
     * 2、传递给 Callback 供 PreviewEffectHelper 渲染
     *
     * @param progress 进度，0～1
     * @param updateUI 是否更新 UI
     */
    private void dispatchProgress(float progress, boolean updateUI) {
        if (mSelectType < 0) return;

        // 美妆，且未选择小项
        if ((mSelectType & MASK) == TYPE_MAKEUP_OPTION && mSelectMap.get(mSelectType, 0) == 0) {
            return;
        }

        if (pb != null) {
            pb.setNegativeable(false);
            if (mSelectType == TYPE_BEAUTY_BODY_ENHANCE_HIP) {
                pb.setNegativeable(true);
            }
        }

        if (updateUI) {
            if (mSelectFragment != null) {
                mSelectFragment.refreshUI();
            }

            if (pb != null && pb.getProgress() != progress) {
                pb.setProgress(progress);
            }
        }

        mProgressMap.put(mSelectType, progress);
        if (mSelectType == TYPE_FILTER) {
            updateFilterIntensity(progress);
        } else {
            // 从 mComposerNodeMap 中取 node
            ComposerNode node = mComposerNodeMap.get(mSelectType);
            if (node == null) {
                LogKit.INSTANCE.e(Constant.TAG, "composer node must be added in mComposerNodeMap before, " +
                        "node not found: " + mSelectType + ", map: " + mComposerNodeMap.toString(), null);
                return;
            }
            node.setValue(progress);
            updateNodeIntensity(node);
        }
    }

    private void dispatchProgress(float progress) {
        dispatchProgress(progress, true);
    }

    private boolean isBeauty4Items(int id) {
        return ((id == TYPE_BEAUTY_RESHAPE_BRIGHTEN_EYE) ||
                (id == TYPE_BEAUTY_RESHAPE_REMOVE_POUCH) ||
                (id == TYPE_BEAUTY_RESHAPE_SMILE_FOLDS) ||
                (id == TYPE_BEAUTY_RESHAPE_WHITEN_TEETH));

    }

    private boolean isBeautyEyeSurgery(int id) {
        return ((id == TYPE_BEAUTY_RESHAPE_SINGLE_TO_DOUBLE_EYELID) ||
                (id == TYPE_BEAUTY_RESHAPE_EYE_PLUMP));

    }

    @Override
    public void onClose() {
        effectEnable = false;
        boolean viewLoaded = getView() != null;
        if (getCallback() == null) {
            return;
        }

        // close effect
        updateComposerNodes(new String[0]);
        updateFilter(null);

        // backup data and clear
        mBackup.backup(mSelectMap, mProgressMap, mSelectType, mSavedFilterPath);
        mSelectMap.clear();
        mProgressMap.clear();
        mSelectType = TYPE_CLOSE;


        // reset UI
        if (viewLoaded) {
            vp.setCurrentItem(0);
            pb.setProgress(0);

            // close MakeupOptionFragment
            MakeupOptionFragment fragment = (MakeupOptionFragment) getChildFragmentManager()
                    .findFragmentByTag(TAG_MAKEUP_OPTION_FRAGMENT);
            if (fragment != null) {
                showOrHideMakeupOptionFragment(false);
            }

            refreshVP();
        }
    }

    @Override
    public void onOptionSelect(ComposerNode node, int position) {
        int selectType = mSelectType;
        // 记录当前选择并更新 UI
        mSelectMap.put(selectType, position);

        int type = node.getId();

        showOrHideProgressBar(type == TYPE_CLOSE ? false : true);
        // 关闭按钮
        if (type == TYPE_CLOSE) {
            mComposerNodeMap.remove(selectType);
            mProgressMap.remove(selectType);
            mSelectMap.delete(selectType);
            pb.setProgress(0);

            updateComposerNodes();
            return;
        }

        mComposerNodeMap.put(selectType, node);
        updateComposerNodes();

        float progress = mProgressMap.get(selectType, mPresenter.getDefaultValue(selectType));
        dispatchProgress(progress);
    }

    /**
     * 默认按钮点击之后，需要将所有的值都设置为默认给定的值，其间主要需要解决三个问题
     * 1。 各功能强度值变动之后，需要更改各 item 的标志点
     * 2。 修改到默认值后，需要回到原来的状态（原来选中的按钮依旧选中，进度条依旧指示当前选中的按钮）
     * 3。 不能影响没有强度或不参与的功能（美体、美妆）
     */
    @Override
    public void onDefaultClick(boolean isReset) {
        if (getCallback() == null) return;

        boolean viewLoaded = getView() != null;

        // backup beauty and reshape page state
        int selectBeauty = mSelectMap.get(TYPE_BEAUTY_FACE, TYPE_CLOSE);
        int selectReshape = mSelectMap.get(TYPE_BEAUTY_RESHAPE, TYPE_CLOSE);
        // set none select when no available select
        if (selectBeauty == TYPE_CLOSE) selectBeauty = -2;
        if (selectReshape == TYPE_CLOSE) selectReshape = -2;

        // clear data

        if (isReset) {
//            mProgressMap.clear();
            mPresenter.removeProgressInMap(mProgressMap, TYPE_BEAUTY_FACE);
//            mPresenter.removeProgressInMap(mProgressMap, TYPE_BEAUTY_RESHAPE);
        }
        mSavedFilterPath = null;
//        mComposerNodeMap.clear();
//        mSelectMap.clear();
        mPresenter.removeNodesOfType(mComposerNodeMap, TYPE_BEAUTY_FACE);
//        mPresenter.removeNodesOfType(mComposerNodeMap,TYPE_BEAUTY_RESHAPE);
        mPresenter.removeTypeInMap(mSelectMap, TYPE_BEAUTY_FACE);
//        mPresenter.removeTypeInMap(mSelectMap,TYPE_BEAUTY_RESHAPE);

        mPresenter.generateDefaultBeautyNodes(mComposerNodeMap);


        //remove beauty4Items caused it has 0 intensity
        //retain only if current selection belongs to beauty4Items
//        if (!isBeauty4Items(mSelectType)) {
//            mComposerNodeMap.remove(TYPE_BEAUTY_RESHAPE_BRIGHTEN_EYE);
//            mComposerNodeMap.remove(TYPE_BEAUTY_RESHAPE_REMOVE_POUCH);
//            mComposerNodeMap.remove(TYPE_BEAUTY_RESHAPE_SMILE_FOLDS);
//            mComposerNodeMap.remove(TYPE_BEAUTY_RESHAPE_WHITEN_TEETH);
//        }

        if (!isBeautyEyeSurgery(mSelectType)) {
            mComposerNodeMap.remove(TYPE_BEAUTY_RESHAPE_SINGLE_TO_DOUBLE_EYELID);
            mComposerNodeMap.remove(TYPE_BEAUTY_RESHAPE_EYE_PLUMP);
        }

        // recover page state
        mSelectMap.put(TYPE_BEAUTY_FACE, selectBeauty);
        mSelectMap.put(TYPE_BEAUTY_RESHAPE, selectReshape);

        // reset effect
        resetEffect(viewLoaded);

        // reset UI
        if (viewLoaded) {
            refreshVP();
            showOrHideMakeupOptionFragment(false);
        }
    }

    public void onReshapeDefaultClick1(boolean isReset) {
        if (getCallback() == null) return;

        boolean viewLoaded = getView() != null;

        if (isReset) {
            closeBeautyReshape();
        }
        mPresenter.addDefaultReshapeNodes(mComposerNodeMap);
        // reset effect
        resetEffect(viewLoaded);

        // reset UI
        if (viewLoaded) {
            refreshVP();
            showOrHideMakeupOptionFragment(false);
        }
    }

    public void onReshapeDefaultClick(boolean isReset) {
        if (getCallback() == null) return;
        getCallback().onDefaultClick();

        boolean viewLoaded = getView() != null;

        closeReshapeEffect(TYPE_BEAUTY_RESHAPE, isReset);
        mPresenter.addDefaultReshapeNodes(mComposerNodeMap);

        // reset effect
        resetEffect(viewLoaded);

        // reset UI
        if (viewLoaded) {
            refreshVP();
            showOrHideMakeupOptionFragment(false);
        }
    }

    public void onBodyDefaultClick(boolean isReset) {
        if (getCallback() == null) return;
        getCallback().onDefaultClick();

        boolean viewLoaded = getView() != null;

        closeBodyEffect(TYPE_BEAUTY_BODY, isReset);
        mPresenter.addDefaultBodyNodes(mComposerNodeMap);

        // reset effect
        resetEffect(viewLoaded);

        // reset UI
        if (viewLoaded) {
            refreshVP();
            showOrHideMakeupOptionFragment(false);
        }
    }

    public void onMakeupDefaultClick(boolean isReset) {
        if (getCallback() == null) return;
        getCallback().onDefaultClick();

        boolean viewLoaded = getView() != null;

        closeMakeupEffect(TYPE_MAKEUP, isReset);
        closeMakeupEffect(TYPE_MAKEUP_OPTION, isReset);

        updateComposerNodes();

        mSelectMap.put(TYPE_MAKEUP, mSelectType);

        if (isReset) {
            mSelectMap.put(TYPE_MAKEUP_BLUSHER, 2);
            mSelectMap.put(TYPE_MAKEUP_LIP, 4);
            mSelectMap.put(TYPE_MAKEUP_FACIAL, 3);
            mSelectMap.put(TYPE_MAKEUP_PUPIL, 2);
            mSelectMap.put(TYPE_MAKEUP_EYESHADOW, 4);
            mSelectMap.put(TYPE_MAKEUP_EYEBROW, 3);
            mSelectMap.put(TYPE_MAKEUP_HAIR, 1); //染发
        }

        //todo 每次开启都是默认的
        mPresenter.addDefaultMakeupNodes(mComposerNodeMap);

        // reset effect
        resetEffect(viewLoaded);

        // reset UI
        if (viewLoaded) {
            refreshVP();
//            showOrHideMakeupOptionFragment(false);
        }
    }

    public void recoverState() {
        effectEnable = true;
        boolean viewLoaded = getView() != null;

        // recover data
        if (mBackup.isEnable()) {
            mSelectType = mBackup.getSelectType();
            mSavedFilterPath = mBackup.getSavedFilterPath();
            SparseIntArray selectMap = mBackup.getSelectMap();
            SparseArray<Float> progressMap = mBackup.getProgressMap();
            for (int i = 0; i < selectMap.size(); i++) {
                int key = selectMap.keyAt(i);
                mSelectMap.put(key, selectMap.get(key));
            }
            for (int i = 0; i < progressMap.size(); i++) {
                int key = progressMap.keyAt(i);
                mProgressMap.put(key, progressMap.get(key));
            }
        }

        // recover effect
        resetEffect(viewLoaded);

        // recover UI
        if (viewLoaded) {
            refreshVP();
        }
    }

    private void resetEffect(boolean viewLoaded) {
        int savedSelectedType = mSelectType;
        updateComposerNodes();
        for (int i = 0; i < mComposerNodeMap.size(); i++) {
            ComposerNode node = mComposerNodeMap.valueAt(i);
            if (mPresenter.hasIntensity(node.getId())) {
                mSelectType = node.getId();
                // =====================
//                dispatchProgress(node.getValue(), false);
//                if (viewLoaded && mSelectType == savedSelectedType) {
//                    pb.setProgress(node.getValue());
//                }
                // ------------
                float progress = mProgressMap.get(mSelectType, NO_VALUE);
                float value = 0;
                if (progress == NO_VALUE) {
                    value = node.getValue();
                } else {
                    value = progress;
                }
                dispatchProgress(value, false);
                if (viewLoaded && mSelectType == savedSelectedType) {
                    pb.setProgress(value);
                }

            }
        }
        updateFilter(mSavedFilterPath);
        if (!TextUtils.isEmpty(mSavedFilterPath)) {
            updateFilterIntensity(mProgressMap.get(TYPE_FILTER, 0F));
        }
        mSelectType = savedSelectedType;
    }

    private void refreshVP() {
        for (Fragment fragment : mFragmentList) {
            if (fragment instanceof BeautyFaceFragment) {
                ((BeautyFaceFragment) fragment).refreshUI();
            }
            // todo wzz
//            else if (fragment instanceof FilterFragment) {
//                ((FilterFragment) fragment).refreshUI();
//            }
        }
    }

    private void updateComposerNodes() {
        updateComposerNodes(mPresenter.generateComposerNodes(mComposerNodeMap));
    }

    private void updateComposerNodes(String[] nodes) {
        if (getCallback() == null) {
            return;
        }
        getCallback().updateComposeNodes(nodes);
    }

    private void updateNodeIntensity(ComposerNode node) {
        if (getCallback() == null) {
            return;
        }
        getCallback().updateComposeNodeIntensity(node);
    }

    private void updateFilter(String filter) {
        if (getCallback() == null) {
            return;
        }
        getCallback().onFilterSelected(TextUtils.isEmpty(filter) ? null : new File(filter));
    }

    private void updateFilterIntensity(float intensity) {
        if (getCallback() == null) {
            return;
        }
        getCallback().onFilterValueChanged(intensity);
    }

    private void setEffectOn(boolean on) {
        if (getCallback() == null) {
            return;
        }
        getCallback().setEffectOn(on);
    }

    private void closeBeautyFace() {
        closeEffect(TYPE_BEAUTY_FACE);
    }


    private void closeBeautyBody() {
        closeEffect(TYPE_BEAUTY_BODY);
    }

    private void closeBeautyReshape() {
        closeEffect(TYPE_BEAUTY_RESHAPE);
    }

    private void closeMakeup() {
        closeEffect(TYPE_MAKEUP);
        closeEffect(TYPE_MAKEUP_OPTION);
    }

    private void closeEffect(int id) {
        mPresenter.removeNodesOfType(mComposerNodeMap, id);
//        mPresenter.removeProgressInMap(mProgressMap, id);
        mPresenter.removeTypeInMap(mSelectMap, id);
        updateComposerNodes();
        pb.setProgress(0);

        Fragment fragment = getFragmentWithType(id);
        if (fragment instanceof BeautyFaceFragment) {
            ((BeautyFaceFragment) fragment).refreshUI();
        }
    }

    private void closeMakeupEffect(int id, boolean makeupReset) {

        mPresenter.removeNodesOfType(mComposerNodeMap, id);

        if (makeupReset) {
            mPresenter.removeProgressInMap(mProgressMap, id);
        }

        if (makeupReset) {
            mPresenter.removeTypeInMap(mSelectMap, id);
        }


        mSelectMap.put(TYPE_MAKEUP, mSelectType);
        mSelectMap.put(TYPE_MAKEUP_OPTION, mSelectType);

//        updateComposerNodes();
        pb.setProgress(0);

        Fragment fragment = getFragmentWithType(id);
        if (fragment instanceof BeautyFaceFragment) {
            ((BeautyFaceFragment) fragment).refreshUI();
        }
    }


    private void closeBodyEffect(int id, boolean bodyReset) {
        mPresenter.removeNodesOfType(mComposerNodeMap, id);
        if (bodyReset) {
            mPresenter.removeProgressInMap(mProgressMap, id);
        }

        mPresenter.removeTypeInMap(mSelectMap, id);

        mSelectMap.put(TYPE_BEAUTY_BODY, mSelectType);

        updateComposerNodes();
        pb.setProgress(0);

        Fragment fragment = getFragmentWithType(id);
        if (fragment instanceof BeautyFaceFragment) {
            ((BeautyFaceFragment) fragment).refreshUI();
        }
    }

    private void closeReshapeEffect(int id, boolean bodyReset) {
        mPresenter.removeNodesOfType(mComposerNodeMap, id);
        if (bodyReset) {
            mPresenter.removeProgressInMap(mProgressMap, id);
        }

        mPresenter.removeTypeInMap(mSelectMap, id);

        mSelectMap.put(TYPE_BEAUTY_RESHAPE, mSelectType);

        updateComposerNodes();
        pb.setProgress(0);

        Fragment fragment = getFragmentWithType(id);
        if (fragment instanceof BeautyFaceFragment) {
            ((BeautyFaceFragment) fragment).refreshUI();
        }
    }

    private void showOrHideProgressBar(boolean isShow) {
//        pb.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        rl_progress.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 显示 or 隐藏 MakeupOptionFragment，在没有实例的情况下会先初始化一个实例
     * 显示一个 MakeupOptionFragment 的时候还会设置其默认选择位置，这个位置保存在
     * {@link this#mSelectMap} 中
     *
     * @param isShow 是否显示
     */
    private void showOrHideMakeupOptionFragment(boolean isShow) {
        FragmentManager manager = getChildFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.board_enter, R.anim.board_exit);
        Fragment makeupOptionFragment = manager.findFragmentByTag(TAG_MAKEUP_OPTION_FRAGMENT);

        if (isShow) {
            tl.setVisibility(View.GONE);
            vp.setVisibility(View.GONE);
            ivCloseMakeupOption.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.animate().alpha(1).setDuration(ANIMATION_DURATION).start();
            ivCloseMakeupOption.animate().alpha(1).setDuration(ANIMATION_DURATION).start();
            if (makeupOptionFragment == null) {
                makeupOptionFragment = generateMakeupOptionFragment();
                ((MakeupOptionFragment) makeupOptionFragment).setMakeupType(mSelectType, mSelectMap);

                transaction.add(R.id.fl_identify, makeupOptionFragment, TAG_MAKEUP_OPTION_FRAGMENT).commit();
            } else {
                ((MakeupOptionFragment) makeupOptionFragment).setMakeupType(mSelectType, mSelectMap);
                transaction.show(makeupOptionFragment).commit();
            }
        } else {
            if (makeupOptionFragment == null) return;
            transaction.hide(makeupOptionFragment).commit();
            tvTitle.animate().alpha(0).setDuration(ANIMATION_DURATION).start();
            ivCloseMakeupOption.animate().alpha(0).setDuration(ANIMATION_DURATION).start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvTitle.setVisibility(View.GONE);
                    ivCloseMakeupOption.setVisibility(View.GONE);
                    tl.setVisibility(View.VISIBLE);
                    vp.setVisibility(View.VISIBLE);
                }
            }, ANIMATION_DURATION);
        }
    }

    private Fragment generateMakeupOptionFragment() {
        return new MakeupOptionFragment().setCallback(this);
    }

    private int positionToType(int position) {
        if (position == POSITION_BEAUTY) {
            return TYPE_BEAUTY_FACE;
        } else if (position == POSITION_RESHAPE) {
            return TYPE_BEAUTY_RESHAPE;
        } else if (position == POSITION_MAKEUP) {
            return TYPE_MAKEUP;
        } else if (position == POSITION_FILTER) {
            return TYPE_FILTER;
        } else if (position == POSITION_BODY) {
            return TYPE_BEAUTY_BODY;
        }
        return 0;
    }

    private int positionToType1(int position) {
        if (position == 0) {
            return TYPE_BEAUTY_FACE;
        } else if (position == 1) {
            return TYPE_BEAUTY_BODY;
        } else if (position == 2) {
            return TYPE_MAKEUP;
        }
        return 0;
    }

    private Fragment getFragmentWithType(int type) {
        if (mFragmentList == null) return null;
        int index = -1;
        switch (type) {
            case TYPE_BEAUTY_FACE:
                index = POSITION_BEAUTY;
                break;
            case TYPE_BEAUTY_RESHAPE:
                index = POSITION_RESHAPE;
                break;
            case TYPE_BEAUTY_BODY:
                index = POSITION_BODY;
                break;
            case TYPE_MAKEUP:
            case TYPE_MAKEUP_OPTION:
                index = POSITION_MAKEUP;
                break;
            case TYPE_FILTER:
                index = POSITION_FILTER;
                break;
        }
        return (index >= 0 && index < mFragmentList.size()) ? mFragmentList.get(index) : null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_close_makeup_option) {
            showOrHideMakeupOptionFragment(false);
            tv_reset.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public PreviewActivity.EffectType getEffectType() {
        Bundle arguments = getArguments();
        if (arguments == null) return PreviewActivity.EffectType.CAMERA;
        Serializable type = arguments.getSerializable("effect_type");
        if (!(type instanceof PreviewActivity.EffectType)) return PreviewActivity.EffectType.CAMERA;
        return (PreviewActivity.EffectType) type;
    }

    @Override
    public void refreshIcon(int current_feature) {
        if (current_feature == FEATURE_VIDEO) {
            bottom_start.setBackgroundResource(R.drawable.bt_video_selector); // R.drawable.bg_take_pic_selector
        } else if (current_feature == FEATURE_PIC) {
            bottom_start.setBackgroundResource(R.drawable.bt_pic_selector);
        }
    }

    /**
     * 用户手动调节 ProgressBar 之后，由此回调至各功能 Fragment 调整 UI
     */
    public interface IRefreshFragment {
        void refreshUI();
    }

    public interface IEffectCallback {
        /**
         * 更新美妆美颜设置
         *
         * @param nodes 字符串数组，存储所有设置的美颜内容，当 node 长度为 0 时意为关闭美妆
         */
        void updateComposeNodes(String[] nodes);

        /**
         * 更新某一个效果的强度
         *
         * @param node 效果对应 ComposerNode
         */
        void updateComposeNodeIntensity(ComposerNode node);

        // 滤镜
        void onFilterSelected(File file);

        void onFilterValueChanged(float cur);

        /**
         * 设置是否处理特效
         *
         * @param isOn if false，则在处理纹理的时候不使用 RenderManager 处理原始纹理，则不会有效果
         */
        void setEffectOn(boolean isOn);

        void onDefaultClick();
    }

    private boolean isBeautyOn = true;

    private Fragment beautyFragment = new BeautyFaceFragment().setType(TYPE_BEAUTY_FACE).setProgressMap(mProgressMap).setSelectMap(mSelectMap)
            .setEffectType(getEffectType())
            .setCheckAvailableCallback(mCheckAvailableCallback).setCallback(new BeautyFaceFragment.IBeautyCallBack() {
                @Override
                public void onBeautySelect(EffectButtonItem item) {
                    int type = item.getNode().getId(); // TYPE_BEAUTY_FACE_SMOOTH   TYPE_BEAUTY_RESHAPE_FACE_OVERALL
                    mSelectType = type;

//                            mSelectMap.put(TYPE_BEAUTY_FACE, mSelectType);
//                            if (type == TYPE_CLOSE) {
//                                closeBeautyFace();
//                                return;
//                            }

                    //-----------------------
                    if (type == TYPE_CLOSE) { //关闭按钮 type == TYPE_CLOSE
                        if (isBeautyOn) { //关闭当前所有特效，眼睛图标改为开启

                            mSelectMap.put(TYPE_BEAUTY_FACE, mSelectType);
//                            mSelectMap.put(TYPE_BEAUTY_RESHAPE, mSelectType);
                            closeBeautyFace();
//                            closeBeautyReshape();

                        } else { // 打开当前所有特效，眼睛图标改为关闭

                            onDefaultClick(false);
                        }

                        isBeautyOn = !isBeautyOn;

                        ((BeautyFaceFragment) beautyFragment).setOn(isBeautyOn);

                        rl_progress.setVisibility(View.INVISIBLE);

                        return;
                    } else {

                        isBeautyOn = true;
                        ((BeautyFaceFragment) beautyFragment).setOn(true); //眼睛改为关闭状态
                        rl_progress.setVisibility(View.VISIBLE);

                        onDefaultClick(false);

                        // 美颜下有两个type：TYPE_BEAUTY_FACE  TYPE_BEAUTY_RESHAPE
                        ((BeautyFaceFragment) beautyFragment).setType(item.getNode().getNode() == NODE_BEAUTY_CAMERA ? TYPE_BEAUTY_FACE : TYPE_BEAUTY_RESHAPE);
                        ((BeautyFaceFragment) beautyFragment).setAdapterType(item.getNode().getNode() == NODE_BEAUTY_CAMERA ? TYPE_BEAUTY_FACE : TYPE_BEAUTY_RESHAPE);
                        mSelectMap.put(item.getNode().getNode() == NODE_BEAUTY_CAMERA ? TYPE_BEAUTY_FACE : TYPE_BEAUTY_RESHAPE, mSelectType);
                        LogKit.INSTANCE.d(Constant.TAG, "setType: " + (item.getNode().getNode() == NODE_BEAUTY_CAMERA ? TYPE_BEAUTY_FACE : TYPE_BEAUTY_RESHAPE));

                    }

                    //-----------------------
                    if (mComposerNodeMap.get(type) == null) {
                        mComposerNodeMap.put(type, item.getNode());
                        updateComposerNodes();
                    }
                    float progress = mProgressMap.get(type, NO_VALUE);
                    if (progress == NO_VALUE) {
                        dispatchProgress(mPresenter.getDefaultValue(mSelectType));
                    } else {
                        dispatchProgress(progress);
                    }
                }
            });


    private boolean isReshapeOn = false;
    private boolean isBodyOn = false;
    private boolean isMakeupOn = false;
    private Fragment reshapeFragment = new BeautyFaceFragment().setType(TYPE_BEAUTY_RESHAPE).setProgressMap(mProgressMap).setSelectMap(mSelectMap)
            .setEffectType(getEffectType())
            .setCheckAvailableCallback(mCheckAvailableCallback).setCallback(new BeautyFaceFragment.IBeautyCallBack() {
                @Override
                public void onBeautySelect(EffectButtonItem item) {
                    int type = item.getNode().getId(); // TYPE_BEAUTY_FACE_SMOOTH   TYPE_BEAUTY_RESHAPE_FACE_OVERALL
                    mSelectType = type;
                    if (type == TYPE_CLOSE) { //关闭按钮 type == TYPE_CLOSE
                        if (isReshapeOn) { //关闭当前所有特效，眼睛图标改为开启

                            mSelectMap.put(TYPE_BEAUTY_RESHAPE, mSelectType);
                            closeBeautyReshape();

                        } else { // 打开当前所有特效，眼睛图标改为关闭

//                            onDefaultClick(false);
                            onReshapeDefaultClick(false);
                        }

                        isReshapeOn = !isReshapeOn;

                        ((BeautyFaceFragment) reshapeFragment).setOn(isReshapeOn);

                        rl_progress.setVisibility(View.INVISIBLE);

                        return;
                    } else {

                        isReshapeOn = true;
                        ((BeautyFaceFragment) reshapeFragment).setOn(true); //眼睛改为关闭状态
                        rl_progress.setVisibility(View.VISIBLE);

//                        onDefaultClick(false);
                        onReshapeDefaultClick(false);

                        // 美颜下有两个type：TYPE_BEAUTY_FACE  TYPE_BEAUTY_RESHAPE
                        ((BeautyFaceFragment) reshapeFragment).setType(TYPE_BEAUTY_RESHAPE);
                        ((BeautyFaceFragment) reshapeFragment).setAdapterType(TYPE_BEAUTY_RESHAPE);

                        mSelectMap.put(item.getNode().getNode() == NODE_RESHAPE_LIVE ? TYPE_BEAUTY_RESHAPE : TYPE_BEAUTY_RESHAPE, mSelectType);
                        LogKit.INSTANCE.d(Constant.TAG, "setType: " + TYPE_BEAUTY_RESHAPE);

                    }

                    //-----------------------
                    if (mComposerNodeMap.get(type) == null) {
                        mComposerNodeMap.put(type, item.getNode());
                        updateComposerNodes();
                    }
                    float progress = mProgressMap.get(type, NO_VALUE);
                    if (progress == NO_VALUE) {
                        dispatchProgress(mPresenter.getDefaultValue(mSelectType));
                    } else {
                        dispatchProgress(progress);
                    }
                }
            });

    private Fragment bodyFragment = new BeautyFaceFragment()
            .setType(TYPE_BEAUTY_BODY).setProgressMap(mProgressMap).setSelectMap(mSelectMap)
            .setEffectType(getEffectType())
            .setCheckAvailableCallback(mCheckAvailableCallback).setCallback(new BeautyFaceFragment.IBeautyCallBack() {
                @Override
                public void onBeautySelect(EffectButtonItem item) {
                    int type = item.getNode().getId();
                    mSelectType = type;
                    mSelectMap.put(TYPE_BEAUTY_BODY, mSelectType);

                    // =========================
//            if (type == TYPE_CLOSE) {
//                closeBeautyBody();
//                return;
//            }
//
//            if (mComposerNodeMap.get(type) == null) {
//                mComposerNodeMap.put(type, item.getNode());
//                updateComposerNodes();
//            }
//            float progress = mProgressMap.get(type, NO_VALUE);
//            if (progress == NO_VALUE) {
//                dispatchProgress(mPresenter.getDefaultValue(mSelectType));
//            } else {
//                dispatchProgress(progress);
//            }

                    // ----------------------------
                    if (type == TYPE_CLOSE) {
                        if (isBodyOn) { // 关闭所有特效

                            closeBodyEffect(TYPE_BEAUTY_BODY, false);
                        } else {

                            onBodyDefaultClick(false);
                        }

                        isBodyOn = !isBodyOn;
                        ((BeautyFaceFragment) bodyFragment).setOn(isBodyOn);
                        rl_progress.setVisibility(View.INVISIBLE);

                        return;

                    } else {
                        isBodyOn = true;
                        ((BeautyFaceFragment) bodyFragment).setOn(true);
                        rl_progress.setVisibility(View.VISIBLE);

                        onBodyDefaultClick(false);
                    }

                    if (mComposerNodeMap.get(type) == null) {
                        mComposerNodeMap.put(type, item.getNode());
                        updateComposerNodes();
                    }

                    float progress = mProgressMap.get(type, NO_VALUE);
                    if (progress == NO_VALUE) {
                        dispatchProgress(mPresenter.getDefaultValue(mSelectType));
                    } else {
                        dispatchProgress(progress);
                    }

                }
            });

    private Fragment makeupFragment = new BeautyFaceFragment().setType(TYPE_MAKEUP).setProgressMap(mProgressMap).setSelectMap(mSelectMap)
            .setEffectType(getEffectType())
            .setCheckAvailableCallback(mCheckAvailableCallback).setCallback(new BeautyFaceFragment.IBeautyCallBack() {
                @Override
                public void onBeautySelect(EffectButtonItem item) {
                    mSelectType = item.getNode().getId();
                    mSelectMap.put(TYPE_MAKEUP, mSelectType);
                    LogKit.INSTANCE.d(Constant.TAG, "ddd1--- " + mSelectType);

                    // ==============================
//            if (mSelectType == TYPE_CLOSE) {
//                closeMakeup();
//                return;
//            }
//
//            // 染发栏不显示滑杆
//            if (item.getNode().getId() == TYPE_MAKEUP_HAIR) {
//                showOrHideProgressBar(false);
//            } else {
//                showOrHideProgressBar(true);
//                pb.setProgress(mProgressMap.get(mSelectType, 0F));
//            }
//
//            tvTitle.setText(item.getTitle());
//            showOrHideMakeupOptionFragment(true);

                    // -------------------------------------------
                    if (mSelectType == TYPE_CLOSE) {
                        if (isMakeupOn) {
//                    closeMakeup();
                            closeMakeupEffect(TYPE_MAKEUP, false);
                            closeMakeupEffect(TYPE_MAKEUP_OPTION, false);
                            updateComposerNodes();
                            // 每一个美妆小项 恢复到默认的选项
                            mSelectMap.put(TYPE_MAKEUP_BLUSHER, 2);
                            mSelectMap.put(TYPE_MAKEUP_LIP, 4);
                            mSelectMap.put(TYPE_MAKEUP_FACIAL, 3);
                            mSelectMap.put(TYPE_MAKEUP_PUPIL, 2);
                            mSelectMap.put(TYPE_MAKEUP_EYESHADOW, 4);
                            mSelectMap.put(TYPE_MAKEUP_EYEBROW, 3);
                            mSelectMap.put(TYPE_MAKEUP_HAIR, 1); //染发

                        } else {
                            onMakeupDefaultClick(false);
                        }

                        isMakeupOn = !isMakeupOn;
                        ((BeautyFaceFragment) makeupFragment).setOn(isMakeupOn);
                        rl_progress.setVisibility(View.INVISIBLE);

                        return;

                    } else {

                        ((BeautyFaceFragment) makeupFragment).setOn(true);
                        rl_progress.setVisibility(View.VISIBLE);
                        // 控制选择了一次补妆后 再点击口红 腮红的大类 不会恢复到默认的美妆设置
                        if (!isMakeupOn) {
                            isMakeupOn = true;
                            onMakeupDefaultClick(false);
                        }

                    }

                    if (item.getNode().getId() == TYPE_MAKEUP_HAIR) {
                        showOrHideProgressBar(false);
                    } else {
                        showOrHideProgressBar(true);
                        pb.setProgress(mProgressMap.get(mSelectType, 0F));
                    }

                    tvTitle.setText(item.getTitle());
                    showOrHideMakeupOptionFragment(true);

                    tv_reset.setVisibility(View.GONE);

                }
            });


    public void init() {
        onDefaultClick(true);
    }

}
