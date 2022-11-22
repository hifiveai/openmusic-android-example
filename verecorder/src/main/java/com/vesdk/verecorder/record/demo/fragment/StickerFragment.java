package com.vesdk.verecorder.record.demo.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.vesdk.vebase.ToastUtils;
import com.vesdk.vebase.demo.fragment.BaseFeatureFragment;
import com.vesdk.vebase.demo.model.StickerItem;
import com.vesdk.vebase.demo.present.StickerPresenter;
import com.vesdk.vebase.demo.present.contract.StickerContract;
import com.vesdk.verecorder.R;
import com.vesdk.verecorder.record.demo.PreviewActivity;
import com.vesdk.verecorder.record.demo.adapter.StickerRVAdapter;

import java.io.File;

public class StickerFragment extends BaseFeatureFragment<StickerContract.Presenter, StickerFragment.IStickerCallback>
        implements StickerRVAdapter.OnItemClickListener, PreviewActivity.OnCloseListener, StickerContract.View {
    private RecyclerView rv;
    protected int mType;
    protected PreviewActivity.ICheckAvailableCallback mCheckAvailableCallback;
    private IStickerCallbackWithFragment mCallbackWithFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rv = (RecyclerView) inflater.inflate(R.layout.recorder_fragment_sticker, container, false);
        return rv;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPresenter(new StickerPresenter());

        StickerRVAdapter adapter = new StickerRVAdapter(mPresenter.getItems(mType), this);
        adapter.setCheckAvailableCallback(mCheckAvailableCallback);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 6));
        rv.setAdapter(adapter);
    }

    public StickerFragment setType(int type) {
        mType = type;
        return this;
    }

    public StickerFragment setCheckAvailableCallback(PreviewActivity.ICheckAvailableCallback callback) {
        mCheckAvailableCallback = callback;
        return this;
    }

    public StickerFragment setCallback(IStickerCallbackWithFragment callback) {
        mCallbackWithFragment = callback;
        return this;
    }

    public void setSelectItem(String sticker) {
        ((StickerRVAdapter) rv.getAdapter()).setSelectItem(sticker);
    }

    @Override
    public void onItemClick(StickerItem item) {
        if (item.hasTip()) {
            ToastUtils.show(item.getTip());
        }
        if (mCallbackWithFragment != null) {
            mCallbackWithFragment.onStickerSelected(item.getResource() == null ? null : new File(item.getResource()), this);
        }
        if (getCallback() != null) {
            getCallback().onStickerSelected(item.getResource() == null ? null : new File(item.getResource()));
        }
    }

    @Override
    public void onClose() {
        ((StickerRVAdapter) rv.getAdapter()).setSelect(-1);
    }


    public interface IStickerCallback {
        void onStickerSelected(File file);
    }

    public interface IStickerCallbackWithFragment {
        void onStickerSelected(File file, StickerFragment fragment);
    }
}
