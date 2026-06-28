package com.mantao.star;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    public interface Listener {
        void onArticleClick(Article article);
        void onBookmarkToggle(Article article);
    }

    private final List<Article> articles = new ArrayList<>();
    private Set<String> bookmarkedIds = new HashSet<>();
    private final Listener listener;

    public ArticleAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setArticles(List<Article> newArticles) {
        articles.clear();
        articles.addAll(newArticles);
        notifyDataSetChanged();
    }

    public void setBookmarkedIds(Set<String> ids) {
        this.bookmarkedIds = ids;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);

        holder.tvIcon.setText(article.emoji);
        holder.tvCategory.setText(article.category.toUpperCase());
        holder.tvTitle.setText(article.title);
        holder.tvReadTime.setText(article.readTimeMinutes + " menit baca");

        boolean bookmarked = bookmarkedIds.contains(article.id);
        holder.tvBookmark.setAlpha(bookmarked ? 1f : 0.4f);

        holder.itemView.setOnClickListener(v -> listener.onArticleClick(article));
        holder.tvBookmark.setOnClickListener(v -> listener.onBookmarkToggle(article));
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvCategory, tvTitle, tvReadTime, tvBookmark;

        ViewHolder(View itemView) {
            super(itemView);
            tvIcon      = itemView.findViewById(R.id.tvIcon);
            tvCategory  = itemView.findViewById(R.id.tvCategory);
            tvTitle     = itemView.findViewById(R.id.tvTitle);
            tvReadTime  = itemView.findViewById(R.id.tvReadTime);
            tvBookmark  = itemView.findViewById(R.id.tvBookmark);
        }
    }
}