package com.ss.ugc.android.editor.bottom.videoeffect;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ss.ugc.android.editor.base.imageloder.ImageLoader;
import com.ss.ugc.android.editor.base.imageloder.ImageOption;
import com.ss.ugc.android.editor.base.utils.CommonUtils;
import com.ss.ugc.android.editor.bottom.R;

import java.util.ArrayList;
import java.util.List;

public class EffectRVAdapter extends RecyclerView.Adapter<EffectRVAdapter.ViewHolder> {
    private List<EffectApplyItem> mFilterList = new ArrayList<>();
    private OnItemClickListener mListener;
    private EffectApplyItem mApplyItem;
    private Context context;

    public EffectRVAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        mListener = listener;
    }

    public void setFilterList(List<EffectApplyItem> filterList) {
        this.mFilterList.addAll(filterList);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_effect_apply, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final  EffectApplyItem item = mFilterList.get(position);

        holder.tv.setText(context.getString(item.getTrackNameResId()));

        if (item.getType() == MaterialEffect.APPLY_TYPE_ALL) {
            // 全局
            holder.iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.iv.setImageResource(R.drawable.ic_effect_global);
            holder.iv.setBackgroundColor(context.getResources().getColor(R.color.bg_no_filter));
        } else {
            holder.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (TextUtils.equals(mApplyItem.getTrack().getName(), item.getTrack().getName())) {
                holder.iv.setBackgroundResource(R.drawable.bg_item_focused);
            } else {
                holder.iv.setBackgroundResource(R.drawable.bg_item_unselect_selector);
            }

            ImageLoader.INSTANCE.loadBitmap(context, item.getSlot().getMainSegment().getResource().getResourceFile(),
                    holder.iv, new ImageOption.Builder().build());
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()) {
                    return;
                }
                mListener.onItemClick(item, holder.getAdapterPosition());
                notifyDataSetChanged();
                return;

            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }


    public void setApplyTrack(EffectApplyItem applyItem) {
        this.mApplyItem = applyItem;
        notifyDataSetChanged();
    }

    public EffectApplyItem getApplyTrack() {
        return mApplyItem;
    }

    public interface OnItemClickListener {
        void onItemClick(EffectApplyItem applyItem , int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv;
        View view ;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.view);
            iv = itemView.findViewById(R.id.image_effect);
            tv = itemView.findViewById(R.id.tv_apply);
        }
    }
}