package com.mantao.star;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EcoActivity extends AppCompatActivity {

    // NOTE: nilai ini harus sama dengan CIVIC_REPORT_POINTS di HistoryActivity supaya konsisten
    private static final int CIVIC_REPORT_POINTS = 30;

    private static final int XP_PER_LEVEL = 500;
    private static final double CO2_PER_POINT_KG = 0.05; // estimasi kasar, bukan angka ilmiah presisi
    private static final int RECYCLE_GOAL = 50;
    private static final int REPORT_GOAL = 10;

    // ─── Views ───
    private TextView tvUsername, tvHeroSubtitle, tvLevel, tvXp, tvStreak, tvImpact;
    private TextView tvAchv1Desc, tvAchv2Desc, tvAchv1Status, tvAchv2Status, tvMilestoneDesc, tvViewAll;
    private CircularProgressView progressRing;
    private View progressFill1, progressSpacer1, progressFill2, progressSpacer2;
    private LinearLayout btnExploreQuests;

    private LinearLayout navHome, navLocate, navHistory;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.eco_activity);

        initViews();
        loadUsername();
        setupBottomNav();
        setupPlaceholders();
        loadEcoData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEcoData();
    }

    private void initViews() {
        tvUsername      = findViewById(R.id.tvUsername);
        tvHeroSubtitle  = findViewById(R.id.tvHeroSubtitle);
        tvLevel         = findViewById(R.id.tvLevel);
        tvXp            = findViewById(R.id.tvXp);
        tvStreak        = findViewById(R.id.tvStreak);
        tvImpact        = findViewById(R.id.tvImpact);
        tvAchv1Desc     = findViewById(R.id.tvAchv1Desc);
        tvAchv2Desc     = findViewById(R.id.tvAchv2Desc);
        tvAchv1Status   = findViewById(R.id.tvAchv1Status);
        tvAchv2Status   = findViewById(R.id.tvAchv2Status);
        tvMilestoneDesc = findViewById(R.id.tvMilestoneDesc);
        tvViewAll       = findViewById(R.id.tvViewAll);
        progressRing    = findViewById(R.id.progressRing);
        progressFill1   = findViewById(R.id.progressFill1);
        progressSpacer1 = findViewById(R.id.progressSpacer1);
        progressFill2   = findViewById(R.id.progressFill2);
        progressSpacer2 = findViewById(R.id.progressSpacer2);
        btnExploreQuests = findViewById(R.id.btnExploreQuests);

        navHome    = findViewById(R.id.navHome);
        navLocate  = findViewById(R.id.navLocate);
        navHistory = findViewById(R.id.navHistory);
    }

    private void loadUsername() {
        String username = getIntent().getStringExtra("USERNAME");
        if (username != null && !username.isEmpty()) {
            tvUsername.setText(username);
        }
        // Kalau gak ada extra USERNAME (misal dibuka dari nav halaman lain), tetap pakai default "Eco Warrior"
    }

    private void setupPlaceholders() {
        tvViewAll.setOnClickListener(v ->
                Toast.makeText(this, "Fitur daftar achievement lengkap belum tersedia", Toast.LENGTH_SHORT).show());

        btnExploreQuests.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Quests belum tersedia", Toast.LENGTH_SHORT).show());
    }

    // ════════════════════════════════════════
    //  Hitung & tampilkan data dari Room
    // ════════════════════════════════════════

    private void loadEcoData() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<ScanHistory> scans = db.scanHistoryDao().getAll();
            List<Report> reports = db.reportDao().getAllReports();

            int totalPoints = 0;
            for (ScanHistory s : scans) totalPoints += s.ecoPoints;
            totalPoints += reports.size() * CIVIC_REPORT_POINTS;

            int anorganikCounter = 0;
            for (ScanHistory s : scans) {
                if ("Anorganik".equalsIgnoreCase(s.category)) anorganikCounter++;
            }
            final int anorganikCount = anorganikCounter;
            final int reportCount = reports.size();
            final int totalItemsRecycled = scans.size();

            int streakDays = computeConsecutiveStreak(scans, reports);

            long monthStart = startOfThisMonth();
            int monthlyPoints = 0;
            for (ScanHistory s : scans) if (s.timestamp >= monthStart) monthlyPoints += s.ecoPoints;
            for (Report r : reports) if (r.timestamp >= monthStart) monthlyPoints += CIVIC_REPORT_POINTS;
            double co2SavedKg = monthlyPoints * CO2_PER_POINT_KG;

            int level = (totalPoints / XP_PER_LEVEL) + 1;
            int xpInLevel = totalPoints % XP_PER_LEVEL;

            runOnUiThread(() -> renderUi(
                    level, xpInLevel, co2SavedKg, streakDays,
                    totalItemsRecycled, anorganikCount, reportCount
            ));
        });
    }

    private void renderUi(int level, int xpInLevel, double co2SavedKg, int streakDays,
                          int totalItemsRecycled, int anorganikCount, int reportCount) {

        tvLevel.setText("LVL " + level);
        tvXp.setText(xpInLevel + " / " + XP_PER_LEVEL + " XP");
        progressRing.setProgress(xpInLevel / (float) XP_PER_LEVEL);

        tvHeroSubtitle.setText(String.format(Locale.getDefault(),
                "You've saved an estimated %.1fkg of CO2 this month. Keep going, Earth warrior!",
                co2SavedKg));

        tvStreak.setText(streakDays + (streakDays == 1 ? " Day" : " Days"));
        tvImpact.setText(totalItemsRecycled + " Items");

        // Achievement 1: Recycle Master (berdasarkan jumlah scan kategori Anorganik)
        float fraction1 = Math.min(1f, anorganikCount / (float) RECYCLE_GOAL);
        setProgressBar(progressFill1, progressSpacer1, fraction1);
        tvAchv1Desc.setText("Processed " + anorganikCount + " / " + RECYCLE_GOAL + " recyclable items");
        tvAchv1Status.setText(fraction1 >= 1f ? "✓" : (fraction1 > 0f ? "⭐" : "🔒"));

        // Achievement 2: Report Expert (berdasarkan jumlah laporan dikirim)
        float fraction2 = Math.min(1f, reportCount / (float) REPORT_GOAL);
        setProgressBar(progressFill2, progressSpacer2, fraction2);
        tvAchv2Desc.setText(reportCount + " / " + REPORT_GOAL + " cleanup reports submitted");
        tvAchv2Status.setText(fraction2 >= 1f ? "✓" : (fraction2 > 0f ? "⭐" : "🔒"));

        // CTA milestone dinamis
        tvMilestoneDesc.setText("Reach Level " + (level + 1) +
                " to unlock the next badge and keep your eco streak alive.");
    }

    private void setProgressBar(View fill, View spacer, float fraction) {
        fraction = Math.max(0f, Math.min(1f, fraction));
        LinearLayout.LayoutParams fillParams = (LinearLayout.LayoutParams) fill.getLayoutParams();
        fillParams.weight = fraction;
        fill.setLayoutParams(fillParams);

        LinearLayout.LayoutParams spacerParams = (LinearLayout.LayoutParams) spacer.getLayoutParams();
        spacerParams.weight = 1f - fraction;
        spacer.setLayoutParams(spacerParams);
    }

    // ════════════════════════════════════════
    //  Streak (hari berurutan dengan aktivitas)
    // ════════════════════════════════════════

    private int computeConsecutiveStreak(List<ScanHistory> scans, List<Report> reports) {
        Set<String> activeDays = new HashSet<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (ScanHistory s : scans) activeDays.add(fmt.format(new Date(s.timestamp)));
        for (Report r : reports) activeDays.add(fmt.format(new Date(r.timestamp)));

        Calendar cal = Calendar.getInstance();
        int streak = 0;

        // Kalau hari ini belum ada aktivitas, mulai hitung dari kemarin
        // biar streak gak langsung putus padahal user belum sempat scan hari ini
        if (!activeDays.contains(fmt.format(cal.getTime()))) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        while (activeDays.contains(fmt.format(cal.getTime()))) {
            streak++;
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    private long startOfThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
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

        navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        // navEco tidak perlu listener navigasi — kita memang sudah di halaman ini

        View navScanView = findViewById(R.id.navScan);
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