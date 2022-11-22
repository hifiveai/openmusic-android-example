// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
package com.vesdk.verecorder.record.demo.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.vesdk.vebase.demo.fragment.BaseFeatureFragment;
import com.vesdk.vebase.demo.present.contract.FilterContract;
import com.vesdk.verecorder.R;
import com.vesdk.vebase.demo.present.FilterPresenter;
import com.vesdk.verecorder.record.demo.PreviewActivity;
import com.vesdk.verecorder.record.demo.adapter.FilterRVAdapter;

import java.io.File;

/**
 * 滤镜
 */
public class FilterFragment extends BaseFeatureFragment<FilterContract.Presenter, FilterFragment.IFilterCallback>
        implements FilterRVAdapter.OnItemClickListener, FilterContract.View, EffectFragment.IRefreshFragment {
    private RecyclerView rv;
    private PreviewActivity.ICheckAvailableCallback mCheckAvailableCallback;
    private SparseIntArray mSelectMap;

    public interface IFilterCallback {
        void onFilterSelected(File file, int position);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rv = (RecyclerView) View.inflate(getContext(), R.layout.recorder_fragment_filter, null);
        return rv;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPresenter(new FilterPresenter());

        FilterRVAdapter adapter = new FilterRVAdapter(mPresenter.getItems(), this);
        adapter.setCheckAvailableCallback(mCheckAvailableCallback);
        adapter.setSelectMap(mSelectMap);

        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);
    }

    FilterFragment setCheckAvailableCallback(PreviewActivity.ICheckAvailableCallback callback) {
        mCheckAvailableCallback = callback;
        return this;
    }

    public FilterFragment setSelectMap(SparseIntArray selectMap) {
        mSelectMap = selectMap;
        return this;
    }

    @Override
    public void onItemClick(File file, int position) {
        if (getCallback() == null) {
            return;
        }
        getCallback().onFilterSelected(file, position);
        refreshUI();
    }

    @Override
    public void refreshUI() {
        if (rv == null) return;
        FilterRVAdapter adapter = (FilterRVAdapter) rv.getAdapter();
        if (adapter == null) return;
        adapter.notifyDataSetChanged();;
    }
}
