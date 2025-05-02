package com.mellah.mediassist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SlideAdapter
        extends RecyclerView.Adapter<SlideAdapter.SlideViewHolder> {

    private final List<SlideItem> slides;

    public SlideAdapter(List<SlideItem> slides) {
        this.slides = slides;
    }

    @Override
    public SlideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SlideViewHolder holder, int position) {
        SlideItem item = slides.get(position);
        holder.ivImage.setImageResource(item.getImageResId());
        holder.tvTitle.setText(item.getTitle());
        holder.tvDesc.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return slides.size();
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView  tvTitle, tvDesc;

        SlideViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivSlideImage);
            tvTitle = itemView.findViewById(R.id.tvSlideTitle);
            tvDesc  = itemView.findViewById(R.id.tvSlideDesc);
        }
    }
}
