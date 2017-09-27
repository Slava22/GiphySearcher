package com.example.andrushk.giphysearcher;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.felipecsl.gifimageview.library.GifImageView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.UrlViewHolder> {

    private Context mContext;
    private List<Gif> urls;
    private int screenWidth;

    public RecyclerViewAdapter(List<Gif> urls, Context mContext, int screenWidth) {
        this.urls = urls;
        this.mContext = mContext;
        this.screenWidth = screenWidth;
    }

    @Override
    public long getItemId(int position) {
        return urls.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    @Override
    public UrlViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview, viewGroup, false);
        UrlViewHolder urlViewHolder = new UrlViewHolder(v);
        return urlViewHolder;
    }

    @Override
    public void onBindViewHolder(final UrlViewHolder gifViewHolder, int i) {
        Glide.with(mContext).load(urls.get(i).getUrlGif()).into(gifViewHolder.gifImageView);

        double koef = (double) urls.get(i).getWidth() / screenWidth;
        gifViewHolder.cv.getLayoutParams().height = (int) (urls.get(i).getHeight() / koef);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class UrlViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        GifImageView gifImageView;

        UrlViewHolder(final View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            gifImageView = (GifImageView) itemView.findViewById(R.id.gif);

            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, GifActivity.class);
                    intent.putExtra("width", urls.get(getAdapterPosition()).getWidth());
                    intent.putExtra("height", urls.get(getAdapterPosition()).getHeight());
                    intent.putExtra("screenWidth", screenWidth);
                    intent.putExtra("url", urls.get(getAdapterPosition()).getUrlGif());
                    mContext.startActivity(intent);
                }
            });
        }
    }
}