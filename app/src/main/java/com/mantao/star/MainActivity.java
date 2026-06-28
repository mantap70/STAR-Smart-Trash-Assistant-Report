package com.mantao.star;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Bottom Navigation
    private LinearLayout navHome, navLocate, navEco, navHistory, navScan;

    // Bento Grid Cards
    private LinearLayout cardReport, cardTrack, cardHistory;
    private LinearLayout cardChatbot, cardInfo, cardLocate;
    private LinearLayout cardEco, cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        initViews();
        loadUsername();
        setupBentoCards();
        setupBottomNav();
    }

    // ─────────────────────────────────────────
    //  Init
    // ─────────────────────────────────────────

    private void initViews() {
        navHome    = findViewById(R.id.navHome);
        navLocate  = findViewById(R.id.navLocate);
        navScan    = findViewById(R.id.navScan);
        navEco     = findViewById(R.id.navEco);
        navHistory = findViewById(R.id.navHistory);

        cardReport  = findViewById(R.id.cardReport);
        cardTrack   = findViewById(R.id.cardTrack);
        cardHistory = findViewById(R.id.cardHistory);
        cardChatbot = findViewById(R.id.cardChatbot);
        cardInfo    = findViewById(R.id.cardInfo);
        cardLocate  = findViewById(R.id.cardLocate);
        cardEco     = findViewById(R.id.cardEco);
        cardLogout  = findViewById(R.id.cardLogout);
    }

    // ─────────────────────────────────────────
    //  Load Username dari Intent
    // ─────────────────────────────────────────

    private void loadUsername() {
        String username = getIntent().getStringExtra("USERNAME");
        if (username != null && !username.isEmpty()) {
            TextView tvUsername = findViewById(R.id.tvUsername);
            if (tvUsername != null) tvUsername.setText(username);
        }
    }

    // ─────────────────────────────────────────
    //  Bento Grid Cards
    // ─────────────────────────────────────────

    private void setupBentoCards() {
        cardReport.setOnClickListener(v -> {
            animateCard(v);
            startActivity(new Intent(this, ReportActivity.class));
        });

        cardTrack.setOnClickListener(v -> {
            animateCard(v);
            // TODO: startActivity(new Intent(this, TrackActivity.class));
            Toast.makeText(this, "Track – Live Progress", Toast.LENGTH_SHORT).show();
        });

        cardHistory.setOnClickListener(v -> {
            animateCard(v);
            startActivity(new Intent(this, HistoryActivity.class));
        });

        cardChatbot.setOnClickListener(v -> {
            animateCard(v);
            startActivity(new Intent(this, ChatbotActivity.class));
        });

        cardInfo.setOnClickListener(v -> {
            animateCard(v);
            startActivity(new Intent(this, InfoActivity.class));
        });

        cardLocate.setOnClickListener(v -> {
            animateCard(v);
            startActivity(new Intent(this, LocateActivity.class));
        });

        cardEco.setOnClickListener(v -> {
            animateCard(v);
            Intent intent = new Intent(this, EcoActivity.class);
            intent.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
            startActivity(intent);
        });

        cardLogout.setOnClickListener(v -> {
            animateCard(v);
            showLogoutDialog();
        });
    }

    // ─────────────────────────────────────────
    //  Bottom Navigation
    // ─────────────────────────────────────────

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> setActiveNav(navHome));

        navLocate.setOnClickListener(v -> {
            setActiveNav(navLocate);
            startActivity(new Intent(this, LocateActivity.class));
        });

        // FAB Scan — buka ScanActivity
        navScan.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            ).start();
            startActivity(new Intent(this, ScanActivity.class));
        });

        navEco.setOnClickListener(v -> {
            setActiveNav(navEco);
            Intent intent = new Intent(this, EcoActivity.class);
            intent.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
            startActivity(intent);
        });

        navHistory.setOnClickListener(v -> {
            setActiveNav(navHistory);
            startActivity(new Intent(this, HistoryActivity.class));
        });
    }

    private void setActiveNav(View selected) {
        View[] navItems = { navHome, navLocate, navEco, navHistory };
        for (View item : navItems) {
            if (item == selected) {
                item.setBackgroundResource(R.drawable.bg_nav_active);
            } else {
                item.setBackgroundResource(android.R.color.transparent);
            }
        }
    }

    // ─────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────

    private void animateCard(View v) {
        v.animate()
                .scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                ).start();
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin mengakhiri sesi?")
                .setPositiveButton("Ya, Logout", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}