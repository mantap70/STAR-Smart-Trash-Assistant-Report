package com.mantao.star;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_BONUS = 2;

    // Setiap elemen adalah salah satu dari: String (header), HistoryItem (baris biasa), atau BonusCard
    private final List<Object> rows = new ArrayList<>();

    public void setRows(List<Object> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object row = rows.get(position);
        if (row instanceof String) return TYPE_HEADER;
        if (row instanceof BonusCard) return TYPE_BONUS;
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(inflater.inflate(R.layout.item_history_header, parent, false));
            case TYPE_BONUS:
                return new BonusViewHolder(inflater.inflate(R.layout.item_history_bonus, parent, false));
            default:
                return new ItemViewHolder(inflater.inflate(R.layout.item_history_row, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvHeader.setText((String) row);

        } else if (holder instanceof BonusViewHolder) {
            BonusCard bonus = (BonusCard) row;
            BonusViewHolder h = (BonusViewHolder) holder;
            h.tvLabel.setText(bonus.label);
            h.tvTitle.setText(bonus.title);
            h.tvPoints.setText("+" + bonus.points);

        } else if (holder instanceof ItemViewHolder) {
            HistoryItem item = (HistoryItem) row;
            ItemViewHolder h = (ItemViewHolder) holder;
            h.tvTitle.setText(item.title);
            h.tvSubtitle.setText(item.subtitle);
            h.tvPoints.setText("+" + item.ecoPoints);

            if (item.photoPath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(item.photoPath);
                if (bitmap != null) {
                    h.ivThumb.setImageBitmap(bitmap);
                } else {
                    h.ivThumb.setImageResource(R.drawable.add_a_photo);
                }
            } else {
                h.ivThumb.setImageResource(
                        item.type == HistoryItem.TYPE_SCAN ? R.drawable.eco_leaf : R.drawable.add_a_photo
                );
            }
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderViewHolder(View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }

    static class BonusViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel, tvTitle, tvPoints;
        BonusViewHolder(View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvBonusLabel);
            tvTitle = itemView.findViewById(R.id.tvBonusTitle);
            tvPoints = itemView.findViewById(R.id.tvBonusPoints);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvSubtitle, tvPoints;
        ItemViewHolder(View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvPoints = itemView.findViewById(R.id.tvPoints);
        }
    }
}