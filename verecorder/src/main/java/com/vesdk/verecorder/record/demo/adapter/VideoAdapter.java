package com.vesdk.verecorder.record.demo.adapter;


import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vesdk.verecorder.R;

import java.util.List;

/**
 *
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<Bitmap> mVideoItemList;

    public VideoAdapter( List<Bitmap> mVideoItemList ) {
        this.mVideoItemList = mVideoItemList;
    }

    public void setList(List<Bitmap> mVideoItemList){
        this.mVideoItemList = mVideoItemList;
    }
    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recorder_item_record_video, parent, false);
        RecyclerView.ViewHolder holder = new ViewHolder(view);

        return (ViewHolder) holder;
    }

    @Override
    public void onBindViewHolder(VideoAdapter.ViewHolder holder, int position) {

//        holder.video_item.setImageResource(R.mipmap.ic_launcher_round);
        holder.video_item.setImageBitmap(mVideoItemList.get(position));
        if ( position == mVideoItemList.size() -1 ){
            holder.ivClose.setVisibility(View.VISIBLE);
        }else {
            holder.ivClose.setVisibility(View.GONE);
        }
//        holder.ivClose.setOnClickListener(this);

    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView video_item,ivClose;

        public ViewHolder(View view) {
            super(view);
            video_item = view.findViewById(R.id.video_item);
            ivClose = view.findViewById(R.id.iv_close);

            ivClose.setOnClickListener(this);
            video_item.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if ( onItemClickListener == null ) return;
            if (view.getId() == R.id.iv_close){
                mVideoItemList.remove(mVideoItemList.size() -1 );
                notifyDataSetChanged();
                onItemClickListener.onDeleteIconClick( view, getAdapterPosition() );
            }else if (view.getId() == R.id.video_item){
                onItemClickListener.onItemClick( view, getAdapterPosition() );
            }

        }
    }

    /**
     * 定义RecyclerView选项单击事件的回调接口
     */
    public interface OnItemClickListener{//也可以不在这个activity或者是fragment中来声明接口，可以在项目中单独创建一个interface，就改成static就OK
        void onDeleteIconClick(View view, int position);
        void onItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;//声明一下这个接口
    //提供setter方法
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return mVideoItemList.size();
    }
}