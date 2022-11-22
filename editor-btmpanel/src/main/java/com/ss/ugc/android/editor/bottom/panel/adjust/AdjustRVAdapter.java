package com.ss.ugc.android.editor.bottom.panel.adjust;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.ugc.android.editor.base.imageloder.ImageLoader;
import com.ss.ugc.android.editor.base.imageloder.ImageOption;
import com.ss.ugc.android.editor.base.monitior.ReportConstants;
import com.ss.ugc.android.editor.base.monitior.ReportUtils;
import com.ss.ugc.android.editor.base.resource.ResourceItem;
import com.ss.ugc.android.editor.base.theme.OptPanelViewConfig;
import com.ss.ugc.android.editor.base.theme.ThemeStore;
import com.ss.ugc.android.editor.base.utils.CommonUtils;
import com.ss.ugc.android.editor.bottom.R;
import com.ss.ugc.android.editor.core.utils.Toaster;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdjustRVAdapter extends RecyclerView.Adapter<AdjustRVAdapter.ViewHolder> {
    private List<ResourceItem> mFilterList;
    private OnItemClickListener mListener;
    private int mType;
    private int mCurrentPosition;

    private int colorOn;
    private int colorOff;
    private Context context;
    private int selectedColor = R.color.tv_bottom_color;
//    private OptPanelConfigure optPanelConfigure;

    public AdjustRVAdapter(Context context, List<ResourceItem> filterList, OnItemClickListener listener) {
        mFilterList = filterList;
        mListener = listener;
        this.context = context.getApplicationContext();
        OptPanelViewConfig config = ThemeStore.INSTANCE.getOptPanelViewConfig();
        if (config != null) {
            if (config.getSelectedItemColor() != 0) {
                selectedColor = config.getSelectedItemColor();
            }
        }
        colorOn = ActivityCompat.getColor(context, selectedColor);
        colorOff = ActivityCompat.getColor(context, R.color.colorWhite);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.btm_holder_change_editor, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ResourceItem item = getItem(position);

//        holder.iv.setImageResource(item.getIcon());
        ImageLoader.INSTANCE.loadBitmap(context, item.getIcon(), holder.iv
                , new ImageOption.Builder().build());

        if (mCurrentPosition == position) {
            holder.iv.setColorFilter(colorOn);
            holder.tv.setTextColor(colorOn);
        } else {
            holder.iv.setColorFilter(colorOff);
            holder.tv.setTextColor(Color.WHITE);
        }

//        holder.tv.setText(context.getString(item.getTitle()));
        holder.tv.setText(item.getName());
        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()) {
                    Toaster.show(context.getString(R.string.ck_tips_submitted_too_often));
                    return;
                }
                Map<String,String> param = new HashMap<>();
                param.put("action", item.getName());
                ReportUtils.INSTANCE.doReport(ReportConstants.VIDEO_EDIT_CONFIG_CLICK_EVENT, param);
                mListener.onItemClick(item.getPath().equals("") ? null : new File(item.getPath()), -1, holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
    }

     ResourceItem getItem(int position) {
        return mFilterList.get(position);
    }

    @Override
    public int getItemCount() {
        return mFilterList.size();
    }


    public void setType(int mType) {
        this.mType = mType;
    }

    public void setCurrentPosition(int position) {
        this.mCurrentPosition = position;
        notifyDataSetChanged();
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public interface OnItemClickListener {
        void onItemClick(File file, int type, int position);
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
