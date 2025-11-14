package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BottomCarouselAdapter extends RecyclerView.Adapter<BottomCarouselAdapter.VH> {
    public interface OnItemClick { void onClick(int position, BottomItem item); }
    private final List<BottomItem> data;
    private final OnItemClick listener;
    private int selectedPosition = -1;

    public BottomCarouselAdapter(List<BottomItem> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    public void setSelectedPosition(int pos) {
        int old = selectedPosition;
        selectedPosition = pos;
        if (old != -1) notifyItemChanged(old);
        notifyItemChanged(selectedPosition);
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom_carousel, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        BottomItem item = data.get(position);
        h.title.setText(item.getTitle());
        h.icon.setImageResource(item.getIconRes());
        boolean selected = position == selectedPosition;
        h.card.setCardBackgroundColor(selected ? 0xFF272727 : 0xFF1A1A1A);
        h.title.setTextColor(selected ? 0xFFFFFFFF : 0xFFBDBDBD);
        h.icon.setColorFilter(selected ? 0xFFF57C00 : 0xFFBDBDBD);
        float scale = selected ? 1.05f : 1.0f;
        h.card.setScaleX(scale);
        h.card.setScaleY(scale);
        h.itemView.setOnClickListener(v -> {
            setSelectedPosition(position);
            listener.onClick(position, item);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CardView card; ImageView icon; TextView title;
        VH(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardRoot);
            icon = itemView.findViewById(R.id.ivIcon);
            title = itemView.findViewById(R.id.tvLabel);
        }
    }
}
