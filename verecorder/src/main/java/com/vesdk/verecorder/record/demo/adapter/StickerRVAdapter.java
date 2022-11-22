package com.vesdk.verecorder.record.demo.adapter;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vesdk.vebase.log.LogKit;
import com.vesdk.vebase.CommonUtils;
import com.vesdk.vebase.Constant;
import com.vesdk.vebase.ToastUtils;
import com.vesdk.vebase.demo.model.StickerItem;
import com.vesdk.verecorder.R;
import com.vesdk.verecorder.record.demo.PreviewActivity;

import java.util.List;

import static com.vesdk.vebase.demo.present.contract.StickerContract.TYPE_STICKER;


public class StickerRVAdapter extends SelectRVAdapter<StickerRVAdapter.ViewHolder> {
    private List<StickerItem> mStickerList;
    private OnItemClickListener mListener;
    private PreviewActivity.ICheckAvailableCallback mCheckAvailableCallback;

    public StickerRVAdapter(List<StickerItem> stickers, OnItemClickListener listener) {
        mStickerList = stickers;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recorder_item_sticker, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        LogKit.INSTANCE.d(Constant.TAG, "onBindViewHolder, position: " + position + ", select: " + mSelect + " in " + hashCode());
        final StickerItem item = mStickerList.get(position);

        if (mSelect == position) {
            holder.ll.setBackgroundResource(R.drawable.bg_item_select_selector);
        } else {
            holder.ll.setBackgroundResource(R.drawable.bg_item_unselect_selector);
        }

        holder.iv.setImageResource(item.getIcon());
        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()) {
                    ToastUtils.show("too fast click");
                    return;
                }
                if (mCheckAvailableCallback != null &&
                        !mCheckAvailableCallback.checkAvailable(TYPE_STICKER)) {
                    return;
                }
                // 第0个为清除贴纸按钮，可重复触发，对齐IOS逻辑
                if (mSelect != position || position == 0) {
                    mListener.onItemClick(item);
                    setSelect(position);
                }
            }
        });
    }

    public void setSelectItem(String sticker) {
        for (int i = 0; i < mStickerList.size(); i++) {
            StickerItem item = mStickerList.get(i);
            if (sticker.equals(item.getResource())) {
                setSelect(i);
                return;
            }
        }
        setSelect(-1);
    }

    public void setCheckAvailableCallback(PreviewActivity.ICheckAvailableCallback callback) {
        mCheckAvailableCallback = callback;
    }

    @Override
    public int getItemCount() {
        return mStickerList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(StickerItem item);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout ll;
        ImageView iv;

        ViewHolder(View itemView) {
            super(itemView);
            ll = itemView.findViewById(R.id.ll_item_sticker);
            iv = itemView.findViewById(R.id.iv_item_sticker);
        }
    }
}
