package com.vesdk.verecorder.record.demo.adapter;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;


import com.vesdk.vebase.log.LogKit;
import com.vesdk.vebase.CommonUtils;
import com.vesdk.vebase.Constant;
import com.vesdk.vebase.ToastUtils;
import com.vesdk.vebase.demo.model.EffectButtonItem;
import com.vesdk.verecorder.record.demo.PreviewActivity;

import java.util.List;

/**
 *  on 2020/8/18 11:49
 */
public class EffectButtonViewRVAdapter extends ButtonViewRVAdapter<EffectButtonItem> {
    private int mType;
    private SparseArray<Float> mProgressMap;
    private SparseIntArray mSelectMap;
    private PreviewActivity.ICheckAvailableCallback mCheckAvailableCallback;

    public EffectButtonViewRVAdapter(List<EffectButtonItem> itemList, OnItemClickListener listener) {
        super(itemList, listener);
    }

    public EffectButtonViewRVAdapter(List<EffectButtonItem> itemList, OnItemClickListener listener, int selectItem) {
        super(itemList, listener, selectItem);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final EffectButtonItem item = mItemList.get(position);

        if ( position == 0 ){
            holder.bv.change(false );
//            holder.bv.pointChange(false);
        }else {
            LogKit.INSTANCE.d(Constant.TAG,"ddd2--- position:"  + position + "  flag:" + (mSelectMap.get(mType) == item.getNode().getId() || mSelectMap.get(mType) == position) );
//            LogKit.INSTANCE.d(Constant.TAG,"position:" + position + "  选中" + (mSelectMap != null && (mSelectMap.get(mType) == item.getNode().getId() || mSelectMap.get(mType) == position)));
            holder.bv.change(mSelectMap != null && (mSelectMap.get(mType) == item.getNode().getId() || mSelectMap.get(mType) == position));
//            holder.bv.pointChange(mProgressMap != null && (mProgressMap.get(item.getNode().getId(), 0F) > 0 || mProgressMap.get(mType, 0F) > 0));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()) {
                    ToastUtils.show("too fast click");
                    return;
                }
                if (mCheckAvailableCallback != null &&
                        !mCheckAvailableCallback.checkAvailable(item.getNode().getId())) {
                    return;
                }
                mListener.onItemClick(item, holder.getAdapterPosition());
            }
        });
    }

    public void setCheckAvailableCallback(PreviewActivity.ICheckAvailableCallback callback) {
        mCheckAvailableCallback = callback;
    }

    public void setType(int type) {
        mType = type;
    }

    public void setProgressMap(SparseArray<Float> progressMap) {
        mProgressMap = progressMap;
    }

    public void setSelectMap(SparseIntArray selectMap) {
        mSelectMap = selectMap;
    }


}
