package com.vesdk.verecorder.record.demo.fragment;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vesdk.vebase.demo.fragment.BaseFeatureFragment;
import com.vesdk.vebase.demo.model.ComposerNode;
import com.vesdk.vebase.demo.model.EffectButtonItem;
import com.vesdk.vebase.demo.present.ItemGetPresenter;
import com.vesdk.vebase.demo.present.contract.ItemGetContract;
import com.vesdk.verecorder.R;
import com.vesdk.verecorder.record.demo.adapter.ButtonViewRVAdapter;
import com.vesdk.verecorder.record.demo.adapter.EffectButtonViewRVAdapter;


public class MakeupOptionFragment
        extends BaseFeatureFragment<ItemGetContract.Presenter, MakeupOptionFragment.IMakeupOptionCallback>
        implements ButtonViewRVAdapter.OnItemClickListener<EffectButtonItem> {
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recorder_fragment_makeup_option, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPresenter(new ItemGetPresenter());

        rv = view.findViewById(R.id.rv_makeup_option);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    public void setMakeupType(final int type, final SparseIntArray selectMap) {
        if (rv == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setMakeupType(type, selectMap);
                }
            }, 16);
            return;
        }

        EffectButtonViewRVAdapter adapter;
        if (rv.getAdapter() == null) {
            adapter = new EffectButtonViewRVAdapter(mPresenter.getItems(type), this);
            adapter.setType(type);
            adapter.setSelectMap(selectMap);
            rv.setAdapter(adapter);
        } else {
            adapter = (EffectButtonViewRVAdapter) rv.getAdapter();
            adapter.setItemList(mPresenter.getItems(type));
            adapter.setType(type);
            adapter.setSelectMap(selectMap);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(EffectButtonItem item, int position) {
        if (getCallback() == null) {
            return;
        }
        getCallback().onOptionSelect(item.getNode(), position);
        refreshUI();
    }

    public void refreshUI() {
        EffectButtonViewRVAdapter adapter = (EffectButtonViewRVAdapter) rv.getAdapter();
        if (adapter == null) return;
        adapter.notifyDataSetChanged();
    }


    interface IMakeupOptionCallback {

        void onDefaultClick(boolean isReset);

        /**
         * 点击某一项之后，回调给 EffectFragment 处理
         * @param node 点击项的 node
         * @param position 点击项所处位置
         */
        void onOptionSelect(ComposerNode node, int position);
    }
}
