package com.vesdk.verecorder.record.demo.adapter;

import android.content.Context;
import  androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.vesdk.vebase.log.LogKit;
import com.vesdk.vebase.Constant;
import com.vesdk.vebase.demo.model.ButtonItem;
import com.vesdk.verecorder.R;
import com.vesdk.verecorder.record.demo.ButtonView;

import java.util.List;

/**
 * 每个项有两个状态，是否点亮，是否使用，如果能直接根据每一个项的 id 通过 map 查找点亮状态和使用状态，就可以解决逐项通知的窘态
 */
public class ButtonViewRVAdapter<T extends ButtonItem> extends RecyclerView.Adapter<ButtonViewRVAdapter.ViewHolder> {
    protected List<T> mItemList;
    protected OnItemClickListener<T> mListener;

    public ButtonViewRVAdapter(List<T> itemList, OnItemClickListener<T> listener) {
        this(itemList, listener, 0);
    }

    public ButtonViewRVAdapter(List<T> itemList, OnItemClickListener<T> listener, int selectItem) {
        mItemList = itemList;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recorder_item_button_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final T item = mItemList.get(position);
        if (item == null) return;
        Context context = holder.bv.getContext();

        if ( position == 0 ){
            holder.bv.setTitle(isOn ? context.getString(R.string.ck_close) : context.getString(R.string.ck_enable));
            holder.bv.setIcon(isOn ? R.drawable.ic_beauty_close : R.drawable.ic_eye);
        }else {
           LogKit.INSTANCE.d(Constant.TAG,"ddd2--- position:"  + position  );

            holder.bv.setIcon(item.getIcon());
            holder.bv.setTitle(context.getString(item.getTitle()));

        }

//        if (item.getDesc() != 0) {
//            holder.bv.setDesc(context.getString(item.getDesc()));
//        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void setItemList(List<T> itemList) {
        mItemList = itemList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ButtonView bv;

        ViewHolder(View itemView) {
            super(itemView);

            bv = (ButtonView) itemView;
        }
    }

    public interface OnItemClickListener<T extends ButtonItem> {
        void onItemClick(T item, int position);
    }

    private boolean isOn = true ;
    public void setOn(boolean isOn) {
        this.isOn = isOn;
    }
}
