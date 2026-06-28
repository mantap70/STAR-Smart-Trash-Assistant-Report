package com.mantao.star;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ArticleDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.article_detail_activity);

        TextView tvIcon        = findViewById(R.id.tvIcon);
        TextView tvCategory    = findViewById(R.id.tvCategory);
        TextView tvTitle       = findViewById(R.id.tvTitle);
        TextView tvReadTime    = findViewById(R.id.tvReadTime);
        TextView tvBody        = findViewById(R.id.tvBody);
        TextView btnBack       = findViewById(R.id.btnBack);
        TextView btnShare      = findViewById(R.id.btnShare);

        btnBack.setOnClickListener(v -> finish());

        String articleId = getIntent().getStringExtra("ARTICLE_ID");
        Article article = ArticleRepository.getById(articleId);

        if (article == null) {
            tvIcon.setText("⚠️");
            tvCategory.setText("");
            tvTitle.setText("Artikel tidak ditemukan");
            tvReadTime.setText("");
            tvBody.setText("Artikel yang kamu cari mungkin sudah dihapus atau tidak tersedia.");
            btnShare.setVisibility(View.GONE);
            return;
        }

        tvIcon.setText(article.emoji);
        tvCategory.setText(article.category.toUpperCase());
        tvTitle.setText(article.title);
        tvReadTime.setText(article.readTimeMinutes + " menit baca");
        tvBody.setText(article.fullBody);

        btnShare.setOnClickListener(v -> shareArticle(article));
    }

    private void shareArticle(Article article) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.title);
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                article.title + "\n\n" + article.shortDescription + "\n\n(dibagikan dari app STAR)");
        startActivity(Intent.createChooser(shareIntent, "Bagikan artikel"));
    }
}