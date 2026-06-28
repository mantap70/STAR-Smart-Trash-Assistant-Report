package com.mantao.star;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private static final long DAY_MS = 24L * 60 * 60 * 1000;
    private static final int STREAK_THRESHOLD_DAYS = 4; // dari 7 hari terakhir, minimal sekian hari ada aktivitas
    private static final int CIVIC_REPORT_POINTS = 30;

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private HistoryAdapter adapter;

    private LinearLayout navHome, navLocate, navEco, navHistory;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.history_activity);

        initViews();
        setupBottomNav();
        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory(); // refresh tiap kali balik ke halaman ini (misal habis submit report baru)
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerHistory);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        navHome    = findViewById(R.id.navHome);
        navLocate  = findViewById(R.id.navLocate);
        navEco     = findViewById(R.id.navEco);
        navHistory = findViewById(R.id.navHistory);
    }

    // ════════════════════════════════════════
    //  Load & gabungkan data
    // ════════════════════════════════════════

    private void loadHistory() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<ScanHistory> scans = db.scanHistoryDao().getAll();
            List<Report> reports = db.reportDao().getAllReports();

            List<HistoryItem> items = new ArrayList<>();

            for (ScanHistory s : scans) {
                HistoryItem item = new HistoryItem();
                item.type = HistoryItem.TYPE_SCAN;
                item.title = s.displayName;
                item.subtitle = formatTime(s.timestamp) + "  •  " + s.processName;
                item.ecoPoints = s.ecoPoints;
                item.photoPath = null;
                item.timestamp = s.timestamp;
                items.add(item);
            }

            for (Report r : reports) {
                HistoryItem item = new HistoryItem();
                item.type = HistoryItem.TYPE_REPORT;
                item.title = r.category != null ? r.category : "Report";
                String loc = r.location != null && r.location.length() > 28
                        ? r.location.substring(0, 28) + "…" : r.location;
                item.subtitle = formatTime(r.timestamp) + "  •  " + (loc != null ? loc : "Report");
                item.ecoPoints = CIVIC_REPORT_POINTS;
                item.photoPath = r.photoPath;
                item.timestamp = r.timestamp;
                items.add(item);
            }

            items.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

            List<Object> rows = buildGroupedRows(items);

            runOnUiThread(() -> {
                adapter.setRows(rows);
                tvEmptyState.setVisibility(items.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                recyclerView.setVisibility(items.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
            });
        });
    }

    // ════════════════════════════════════════
    //  Grouping berdasarkan tanggal + bonus streak
    // ════════════════════════════════════════

    private List<Object> buildGroupedRows(List<HistoryItem> items) {
        List<Object> rows = new ArrayList<>();

        long todayStart = startOfToday();
        long yesterdayStart = todayStart - DAY_MS;
        long weekStart = todayStart - 7 * DAY_MS;

        String currentSection = null;
        boolean bonusInserted = false;

        for (HistoryItem item : items) {
            String section;
            if (item.timestamp >= todayStart) {
                section = "TODAY";
            } else if (item.timestamp >= yesterdayStart) {
                section = "YESTERDAY";
            } else if (item.timestamp >= weekStart) {
                section = "LAST WEEK";
            } else {
                section = "EARLIER";
            }

            if (!section.equals(currentSection)) {
                rows.add(section);
                currentSection = section;

                if (section.equals("LAST WEEK") && !bonusInserted) {
                    int streakDays = countDistinctActiveDays(items, weekStart, todayStart + DAY_MS);
                    if (streakDays >= STREAK_THRESHOLD_DAYS) {
                        BonusCard bonus = new BonusCard();
                        bonus.label = "WEEKLY STREAK BONUS";
                        bonus.title = "Master Recycler";
                        bonus.points = 500;
                        rows.add(bonus);
                    }
                    bonusInserted = true;
                }
            }

            rows.add(item);
        }

        return rows;
    }

    private int countDistinctActiveDays(List<HistoryItem> items, long rangeStart, long rangeEndExclusive) {
        Set<String> days = new HashSet<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (HistoryItem item : items) {
            if (item.timestamp >= rangeStart && item.timestamp < rangeEndExclusive) {
                days.add(dayFormat.format(new Date(item.timestamp)));
            }
        }
        return days.size();
    }

    private long startOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private String formatTime(long timestamp) {
        long todayStart = startOfToday();
        long yesterdayStart = todayStart - DAY_MS;

        if (timestamp >= todayStart || timestamp >= yesterdayStart) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
        }
        return new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    // ════════════════════════════════════════
    //  Bottom Navigation
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

        // navHistory tidak perlu listener navigasi — kita memang sudah di halaman ini

        android.view.View navScanView = findViewById(R.id.navScan);
        if (navScanView != null) {
            navScanView.setOnClickListener(v ->
                    startActivity(new Intent(this, ScanActivity.class)));
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}