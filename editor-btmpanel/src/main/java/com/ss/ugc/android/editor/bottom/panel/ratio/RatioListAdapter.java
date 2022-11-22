package com.ss.ugc.android.editor.bottom.panel.ratio;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ss.ugc.android.editor.base.constants.TypeConstants;
import com.ss.ugc.android.editor.base.theme.OptPanelViewConfig;
import com.ss.ugc.android.editor.base.theme.ThemeStore;
import com.ss.ugc.android.editor.base.utils.CommonUtils;
import com.ss.ugc.android.editor.bottom.R;
import com.ss.ugc.android.editor.core.utils.Toaster;
import java.util.List;


public class RatioListAdapter extends RecyclerView.Adapter<RatioListAdapter.ViewHolder> {
    private List<RatioItem> mFunctionList;
    private OnItemClickListener mListener;

    private int mType;
    private SparseIntArray mSelectMap;
    private Context context;
    private OptPanelViewConfig optPanelViewConfig = ThemeStore.INSTANCE.getOptPanelViewConfig();

    public RatioListAdapter(Context context, List<RatioItem> filterList, OnItemClickListener listener) {
        this.context = context.getApplicationContext();
        mFunctionList = filterList;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.btm_holder_canvas, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final RatioItem item = mFunctionList.get(position);
        if (mType == TypeConstants.getTYPE_RATIO()) {
            if (mSelectMap == null || mSelectMap.get(mType, 0) == position) {
                int selectedItemColor = R.color.tv_bottom_color; //default color
                if (optPanelViewConfig != null && optPanelViewConfig.getSelectedItemColor() != 0) {
                    selectedItemColor = optPanelViewConfig.getSelectedItemColor();
                }
                holder.tv.setTextColor(context.getResources().getColor(selectedItemColor));
            } else {
                holder.tv.setTextColor(context.getResources().getColor(R.color.write));
            }
        }

        if (item.getIcon() == 0) {
            holder.iv.setVisibility(View.GONE);
        } else {
            holder.iv.setImageResource(item.getIcon());
        }
        holder.tv.setText(item.getTitle());
        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()) {
                    Toaster.show(context.getString(R.string.ck_tips_submitted_too_often));
                    return;
                }
                mListener.onItemClick(item.getTitle(), holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFunctionList.size();
    }


    public void setType(int type) {
        mType = type;
    }

    public void setSelectMap(SparseIntArray selectMap) {
        mSelectMap = selectMap;
    }

    public interface OnItemClickListener {
        void onItemClick(String title, int adapterPosition);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
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
