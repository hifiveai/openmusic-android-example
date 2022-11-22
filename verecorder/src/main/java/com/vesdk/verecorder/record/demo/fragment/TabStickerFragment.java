package com.vesdk.verecorder.record.demo.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import  androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.vesdk.vebase.log.LogKit;
import com.google.android.material.tabs.TabLayout;
import com.vesdk.vebase.Constant;
import com.vesdk.vebase.LiveDataBus;
import com.vesdk.vebase.demo.present.contract.StickerContract;
import com.vesdk.verecorder.R;
import com.vesdk.verecorder.record.demo.PreviewActivity;
import com.vesdk.verecorder.record.demo.adapter.FragmentVPAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.vesdk.verecorder.record.demo.PreviewActivity.FEATURE_PIC;
import static com.vesdk.verecorder.record.demo.PreviewActivity.FEATURE_VIDEO;

/**
 *  on 2020-03-12 11:59
 */
public class TabStickerFragment
        extends StickerFragment
        implements PreviewActivity.OnCloseListener, StickerContract.View, StickerFragment.IStickerCallbackWithFragment {
    private TabLayout tl;
    private ViewPager vp;
    private List<Fragment> mFragments;
    private StickerFragment mLastSelectFragment;
    private ImageView ic_sticker_reset;
    private LinearLayout ll_top;
    private RelativeLayout rl_bottom;
    private View bottom_start , bottom_view ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recorder_fragment_identify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vp = view.findViewById(R.id.vp_identify);
        tl = view.findViewById(R.id.tl_identify);
        ll_top = view.findViewById(R.id.ll_top);
        rl_bottom = view.findViewById(R.id.rl_bottom);
        ic_sticker_reset = view.findViewById(R.id.ic_sticker_reset);
        bottom_view = view.findViewById(R.id.bottom_view);

        view.findViewById(R.id.tv_reset).setVisibility(View.GONE);

        view.findViewById(R.id.iv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((PreviewActivity)getActivity()).closeFeature(true);
            }
        });

        bottom_start = view.findViewById(R.id.bottom_start);
        bottom_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogKit.INSTANCE.d(Constant.TAG,"TabStickerFragment点击了面板上的开始按钮 onTouch----");
                ((PreviewActivity)getActivity()).closeFeature(false);
                LiveDataBus.getInstance().with("start",String.class).postValue("start");
            }
        });

        initView();
        initVP();
    }

    private void initView() {
        if (getView() == null || getContext() == null) return;
        getView().setBackgroundColor(ActivityCompat.getColor(getContext(), R.color.transparent));
//        ll_top.setBackgroundColor(ActivityCompat.getColor(getContext(), R.color.ck_colorBg));
//        rl_bottom.setBackgroundColor(ActivityCompat.getColor(getContext(), R.color.ck_colorBg));
//        bottom_view.setBackgroundColor(ActivityCompat.getColor(getContext(), R.color.ck_colorBg));
    }

    private void initVP() {
        List<String> titles = new ArrayList<>();
        mFragments = new ArrayList<>();

        // 2d
        titles.add(getString(R.string.ck_sticker_2d));
        mFragments.add(new StickerFragment().setType(mType+1).setCheckAvailableCallback(mCheckAvailableCallback).setCallback(this));
        // complex
        titles.add(getString(R.string.ck_sticker_complex));
        mFragments.add(new StickerFragment().setType(mType+2).setCheckAvailableCallback(mCheckAvailableCallback).setCallback(this));
        // 3d
        titles.add(getString(R.string.ck_sticker_3d));
        mFragments.add(new StickerFragment().setType(mType+3).setCheckAvailableCallback(mCheckAvailableCallback).setCallback(this));

        final FragmentVPAdapter adapter = new FragmentVPAdapter(getChildFragmentManager(), mFragments, titles);
        vp.setAdapter(adapter);
        vp.setOffscreenPageLimit(mFragments.size());
        tl.setupWithViewPager(vp);

        ic_sticker_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCallback() != null) {
                    getCallback().onStickerSelected(null);
                    //为了重新刷新
                    if (mLastSelectFragment != null) {
                        mLastSelectFragment.onClose();
                        mLastSelectFragment = null ;
                    }
                }
            }
        });
    }

    @Override
    public void setSelectItem(String sticker) {
        for (Fragment fragment : mFragments) {
            if (fragment instanceof StickerFragment) {
                ((StickerFragment) fragment).setSelectItem(sticker);
            }
        }
    }

    @Override
    public void onClose() {
        for (Fragment fragment : mFragments) {
            if (fragment instanceof StickerFragment) {
                ((StickerFragment) fragment).onClose();
            }
        }
    }

    @Override
    public void onStickerSelected(File file, StickerFragment fragment) {
        if (mLastSelectFragment != null && mLastSelectFragment != fragment) {
            mLastSelectFragment.onClose();
        }
        mLastSelectFragment = fragment;

        if (getCallback() != null) {
            getCallback().onStickerSelected(file);
        }
    }

    @Override
    public void refreshIcon(int current_feature) {
        if (current_feature == FEATURE_VIDEO) {
            bottom_start.setBackgroundResource(R.drawable.bt_video_selector); // R.drawable.bg_take_pic_selector
        } else if (current_feature == FEATURE_PIC) {
            bottom_start.setBackgroundResource(R.drawable.bt_pic_selector);
        }
    }
}
