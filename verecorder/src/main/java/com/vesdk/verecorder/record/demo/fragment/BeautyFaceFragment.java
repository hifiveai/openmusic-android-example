package com.vesdk.verecorder.record.demo.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.vesdk.vebase.demo.fragment.BaseFeatureFragment;
import com.vesdk.vebase.demo.model.EffectButtonItem;
import com.vesdk.vebase.demo.present.ItemGetPresenter;
import com.vesdk.vebase.demo.present.contract.ItemGetContract;
import com.vesdk.verecorder.R;
import com.vesdk.verecorder.record.demo.PreviewActivity;
import com.vesdk.verecorder.record.demo.adapter.ButtonViewRVAdapter;
import com.vesdk.verecorder.record.demo.adapter.EffectButtonViewRVAdapter;

import java.util.List;

import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_BEAUTY_FACE;

public class BeautyFaceFragment extends BaseFeatureFragment<ItemGetContract.Presenter, BeautyFaceFragment.IBeautyCallBack>
        implements EffectFragment.IRefreshFragment, ButtonViewRVAdapter.OnItemClickListener<EffectButtonItem>, ItemGetContract.View {
    private RecyclerView rv;
    private int mType;
    private SparseArray<Float> mProgressMap;
    private SparseIntArray mSelectMap;
    private PreviewActivity.ICheckAvailableCallback mCheckAvailableCallback;
    private PreviewActivity.EffectType mEffectType;

    private EffectButtonViewRVAdapter adapter ;
    public interface IBeautyCallBack {
        void onBeautySelect(EffectButtonItem item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.recorder_fragment_beauty, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPresenter(new ItemGetPresenter());

        rv = view.findViewById(R.id.rv_beauty);
        List<EffectButtonItem> items = mPresenter.getItems(mType);
        adapter = new EffectButtonViewRVAdapter(items, this);
        adapter.setCheckAvailableCallback(mCheckAvailableCallback);
        adapter.setType(mType);
        adapter.setOn(mType == TYPE_BEAUTY_FACE ? true : false  );
        adapter.setSelectMap(mSelectMap);
        adapter.setProgressMap(mProgressMap);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);
    }

    public BeautyFaceFragment setType(int type) {
        mType = type;
        return this;
    }
    public void setAdapterType(int type) {
        adapter.setType(type);
    }
    public BeautyFaceFragment setOn(boolean isOn) {
        adapter.setOn(isOn);
        return this ;
    }

    public BeautyFaceFragment setProgressMap(SparseArray<Float> progressMap) {
        mProgressMap = progressMap;
        return this;
    }

    public BeautyFaceFragment setSelectMap(SparseIntArray selectMap) {
        mSelectMap = selectMap;
        return this;
    }

    public BeautyFaceFragment setEffectType(PreviewActivity.EffectType effectType) {
        mEffectType = effectType;
        return this;
    }

    public BeautyFaceFragment setCheckAvailableCallback(PreviewActivity.ICheckAvailableCallback checkAvailableCallback) {
        mCheckAvailableCallback = checkAvailableCallback;
        return this;
    }

    @Override
    public void onItemClick(EffectButtonItem item, int position) {
        getCallback().onBeautySelect(item);
        refreshUI();
    }

    @Override
    public void refreshUI() {
        if (rv == null) return;
        ButtonViewRVAdapter adapter = (ButtonViewRVAdapter) rv.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

//    @Override
    public PreviewActivity.EffectType getEffectType() {
        return mEffectType;
    }
}
