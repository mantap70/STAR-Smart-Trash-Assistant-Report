package com.mantao.star;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class InfoActivity extends AppCompatActivity implements ArticleAdapter.Listener {

    private static final String PREFS_NAME = "star_prefs";
    private static final String KEY_BOOKMARKS = "bookmarked_articles";

    // ─── Views ───
    private LinearLayout heroCard;
    private TextView tvHeroTitle, tvHeroDescription;
    private TextView tvSearchToggle;
    private EditText etSearch;
    private TextView chipAll, chipTips, chipBerita, chipInovasi, chipKomunitas;
    private RecyclerView recyclerArticles;
    private EditText etEmail;
    private TextView btnSubscribe;

    private LinearLayout navHome, navLocate, navEco, navHistory;

    // ─── State ───
    private ArticleAdapter adapter;
    private Article featuredArticle;
    private String selectedCategory = "Semua";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.info_activity);

        initViews();
        setupHero();
        setupSearch();
        setupCategoryChips();
        setupRecycler();
        setupNewsletter();
        setupBottomNav();

        applyFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBookmarks();
    }

    private void initViews() {
        heroCard           = findViewById(R.id.heroCard);
        tvHeroTitle        = findViewById(R.id.tvHeroTitle);
        tvHeroDescription  = findViewById(R.id.tvHeroDescription);

        tvSearchToggle = findViewById(R.id.tvSearchToggle);
        etSearch       = findViewById(R.id.etSearch);

        chipAll        = findViewById(R.id.chipAll);
        chipTips       = findViewById(R.id.chipTips);
        chipBerita     = findViewById(R.id.chipBerita);
        chipInovasi    = findViewById(R.id.chipInovasi);
        chipKomunitas  = findViewById(R.id.chipKomunitas);

        recyclerArticles = findViewById(R.id.recyclerArticles);

        etEmail       = findViewById(R.id.etEmail);
        btnSubscribe  = findViewById(R.id.btnSubscribe);

        navHome    = findViewById(R.id.navHome);
        navLocate  = findViewById(R.id.navLocate);
        navEco     = findViewById(R.id.navEco);
        navHistory = findViewById(R.id.navHistory);
    }

    // ════════════════════════════════════════
    //  Hero (artikel featured)
    // ════════════════════════════════════════

    private void setupHero() {
        List<Article> all = ArticleRepository.getAll();
        for (Article a : all) {
            if (a.featured) {
                featuredArticle = a;
                break;
            }
        }
        if (featuredArticle == null && !all.isEmpty()) featuredArticle = all.get(0);

        if (featuredArticle != null) {
            tvHeroTitle.setText(featuredArticle.title);
            tvHeroDescription.setText(featuredArticle.shortDescription);
            heroCard.setOnClickListener(v -> openDetail(featuredArticle));
        }
    }

    // ════════════════════════════════════════
    //  Search
    // ════════════════════════════════════════

    private void setupSearch() {
        tvSearchToggle.setOnClickListener(v -> {
            boolean visible = etSearch.getVisibility() == View.VISIBLE;
            etSearch.setVisibility(visible ? View.GONE : View.VISIBLE);
            if (visible) etSearch.setText("");
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                applyFilters();
            }

            @Override public void afterTextChanged(Editable s) { }
        });
    }

    // ════════════════════════════════════════
    //  Filter kategori
    // ════════════════════════════════════════

    private void setupCategoryChips() {
        chipAll.setOnClickListener(v -> selectCategory("Semua"));
        chipTips.setOnClickListener(v -> selectCategory("Tips & Trik"));
        chipBerita.setOnClickListener(v -> selectCategory("Berita Lingkungan"));
        chipInovasi.setOnClickListener(v -> selectCategory("Inovasi Hijau"));
        chipKomunitas.setOnClickListener(v -> selectCategory("Komunitas"));
        updateChipStyles();
    }

    private void selectCategory(String category) {
        selectedCategory = category;
        updateChipStyles();
        applyFilters();
    }

    private void updateChipStyles() {
        TextView[] chips = { chipAll, chipTips, chipBerita, chipInovasi, chipKomunitas };
        String[] labels = { "Semua", "Tips & Trik", "Berita Lingkungan", "Inovasi Hijau", "Komunitas" };

        for (int i = 0; i < chips.length; i++) {
            boolean active = labels[i].equals(selectedCategory);
            chips[i].setBackgroundResource(active ? R.drawable.bg_button_primary : R.drawable.bg_pebble_card);
            chips[i].setTextColor(getColor(active ? R.color.on_primary : R.color.on_surface_variant));
        }
    }

    // ════════════════════════════════════════
    //  RecyclerView + filter gabungan
    // ════════════════════════════════════════

    private void setupRecycler() {
        recyclerArticles.setLayoutManager(new LinearLayoutManager(this));
        recyclerArticles.setNestedScrollingEnabled(false);
        adapter = new ArticleAdapter(this);
        recyclerArticles.setAdapter(adapter);
        refreshBookmarks();
    }

    private void refreshBookmarks() {
        if (adapter != null) adapter.setBookmarkedIds(loadBookmarks());
    }

    private void applyFilters() {
        List<Article> filtered = new ArrayList<>();
        for (Article a : ArticleRepository.getAll()) {
            if (a.featured) continue; // sudah ditampilkan terpisah di hero

            boolean matchCategory = selectedCategory.equals("Semua") || a.category.equals(selectedCategory);
            boolean matchSearch = searchQuery.isEmpty()
                    || a.title.toLowerCase(Locale.getDefault()).contains(searchQuery.toLowerCase(Locale.getDefault()));

            if (matchCategory && matchSearch) filtered.add(a);
        }
        adapter.setArticles(filtered);
    }

    // ════════════════════════════════════════
    //  Newsletter (placeholder, belum terhubung ke layanan email asli)
    // ════════════════════════════════════════

    private void setupNewsletter() {
        btnSubscribe.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Masukkan email yang valid", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this,
                    "Terima kasih! (fitur newsletter belum benar-benar terhubung ke layanan email)",
                    Toast.LENGTH_LONG).show();
            etEmail.setText("");
        });
    }

    // ════════════════════════════════════════
    //  Adapter callbacks
    // ════════════════════════════════════════

    @Override
    public void onArticleClick(Article article) {
        openDetail(article);
    }

    @Override
    public void onBookmarkToggle(Article article) {
        Set<String> bookmarks = loadBookmarks();
        if (bookmarks.contains(article.id)) {
            bookmarks.remove(article.id);
        } else {
            bookmarks.add(article.id);
        }
        saveBookmarks(bookmarks);
        adapter.setBookmarkedIds(bookmarks);
    }

    private Set<String> loadBookmarks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>()));
    }

    private void saveBookmarks(Set<String> bookmarks) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putStringSet(KEY_BOOKMARKS, bookmarks).apply();
    }

    private void openDetail(Article article) {
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra("ARTICLE_ID", article.id);
        startActivity(intent);
    }

    // ════════════════════════════════════════
    //  Bottom Navigation (tidak ada item aktif — Info diakses dari card Home)
    // ════════════════════════════════════════

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navLocate.setOnClickListener(v ->
                startActivity(new Intent(this, LocateActivity.class)));

        navEco.setOnClickListener(v ->
                startActivity(new Intent(this, EcoActivity.class)));

        navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        View navScanView = findViewById(R.id.navScan);
        if (navScanView != null) {
            navScanView.setOnClickListener(v ->
                    startActivity(new Intent(this, ScanActivity.class)));
        }
    }
}