package com.vesdk.verecorder.record.demo.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.vesdk.vebase.CommonUtils;
import com.vesdk.vebase.ToastUtils;
import com.vesdk.vebase.demo.model.FilterItem;
import com.vesdk.verecorder.R;
import com.vesdk.verecorder.record.demo.PreviewActivity;

import java.io.File;
import java.util.List;


import static com.vesdk.vebase.demo.present.contract.ItemGetContract.TYPE_FILTER;

public class FilterRVAdapter extends RecyclerView.Adapter<FilterRVAdapter.ViewHolder> {
    private List<FilterItem> mFilterList;
    private OnItemClickListener mListener;
    private PreviewActivity.ICheckAvailableCallback mCheckAvailableCallback;
    private int mType;
    private SparseIntArray mSelectMap;

    public FilterRVAdapter(List<FilterItem> filterList, OnItemClickListener listener) {
        mFilterList = filterList;
        mListener = listener;
        mType = TYPE_FILTER;
    }

    @Override
    public FilterRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.recorder_item_filter, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(final FilterRVAdapter.ViewHolder holder, final int position) {
        final FilterItem item = mFilterList.get(position);

        if (mSelectMap == null || mSelectMap.get(mType, 0) == position) {
            holder.ll.setBackgroundResource(R.drawable.bg_item_select_selector);
        } else {
            holder.ll.setBackgroundResource(R.drawable.bg_item_unselect_selector);
        }

        holder.iv.setImageResource(item.getIcon());
        holder.tv.setText(item.getTitle());
        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()) {
                    ToastUtils.show("too fast click");
                    return;
                }
                if (mCheckAvailableCallback != null && !mCheckAvailableCallback.checkAvailable(TYPE_FILTER)) {
                    return;
                }
                mListener.onItemClick(item.getResource().equals("") ? null : new File(item.getResource()), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }

    public void setCheckAvailableCallback(PreviewActivity.ICheckAvailableCallback callback) {
        mCheckAvailableCallback = callback;
    }

    public void setSelectMap(SparseIntArray selectMap) {
        mSelectMap = selectMap;
    }

    public interface OnItemClickListener {
        void onItemClick(File file, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout ll;
        ImageView iv;
        TextView tv;

        ViewHolder(View itemView) {
            super(itemView);
            ll = itemView.findViewById(R.id.ll_item_filter);
            iv = itemView.findViewById(R.id.iv_item_filter);
            tv = itemView.findViewById(R.id.tv_item_filter);
        }
    }
}
