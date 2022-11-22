package com.vesdk.verecorder.record.demo.view;

import android.content.Context;
import  androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.bytedance.android.winnow.WinnowAdapter;
import com.bytedance.android.winnow.WinnowHolder;


import com.vesdk.verecorder.R;

import java.util.Arrays;

/**
 * time : 2020/11/24
 *
 * description :
 */
public class RecordTabView extends RecyclerView {

    private int defaultSelectIndex;
    private OnSelectedListener listener;

    public RecordTabView(Context context) {
        this(context, null);
    }

    public RecordTabView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordTabView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs, defStyle);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context, HORIZONTAL, false);
        setLayoutManager(layoutManager);
        WinnowAdapter adapter = WinnowAdapter.create(ItemHolder.class).addHolderListener(new WinnowAdapter.HolderListener<ItemHolder>() {
            @Override
            protected void onHolderCreated(@NonNull final ItemHolder holder) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        scrollToPosition(holder.getAdapterPosition());
                        if (listener != null) {
                            listener.onSelected(holder);
                        }
                    }
                });
            }
        });
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
                    int position = layoutManager.findFirstCompletelyVisibleItemPosition();
                    if (listener != null) {
                        listener.onSelected((WinnowHolder) recyclerView.getChildViewHolder(layoutManager.findViewByPosition(Math.abs(position))));
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        setAdapter(adapter);
        String[] items = {
                context.getString(R.string.ck_record_photo),
                context.getString(R.string.ck_record_video)};
        adapter.addItems(Arrays.asList(items));
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(this);
        post(new Runnable() {
            @Override
            public void run() {
                scrollToPosition(defaultSelectIndex);
            }
        });

    }

    public int getDefaultSelectIndex() {
        return defaultSelectIndex;
    }

    public void setDefaultSelectIndex(int defaultSelectIndex) {
        this.defaultSelectIndex = defaultSelectIndex;
    }

    public OnSelectedListener getListener() {
        return listener;
    }

    public void setListener(OnSelectedListener listener) {
        this.listener = listener;
    }

    public static class ItemHolder extends WinnowHolder<String> {
        TextView tv;

        public ItemHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }

        @Override
        protected int getLayoutRes() {
            return R.layout.recorder_item_tab;
        }

        @Override
        protected void onBindData(@NonNull String s) {
            tv.setText(s);
        }
    }

    public interface OnSelectedListener {
        void onSelected(WinnowHolder holder);
    }

}
