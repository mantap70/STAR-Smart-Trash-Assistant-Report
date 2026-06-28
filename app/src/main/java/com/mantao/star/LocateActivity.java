package com.mantao.star;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocateActivity extends AppCompatActivity {

    // Fallback default (Jakarta) kalau belum ada laporan dengan koordinat sama sekali
    private static final double DEFAULT_LAT = -6.2088;
    private static final double DEFAULT_LNG = 106.8456;

    private MapView mapView;
    private TextView tvReportCount, tvEmptyState;

    private LinearLayout detailCard;
    private ImageView ivDetailPhoto;
    private TextView tvDetailCategory, tvDetailAddress, tvDetailTime, tvDetailDescription;
    private TextView btnOpenMaps, btnCloseDetail;

    private LinearLayout navHome, navEco, navHistory;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Report selectedReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup osmdroid SEBELUM setContentView, sesuai rekomendasi resminya
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        File baseDir = getExternalFilesDir(null);
        if (baseDir != null) {
            Configuration.getInstance().setOsmdroidBasePath(baseDir);
            Configuration.getInstance().setOsmdroidTileCache(new File(baseDir, "osmdroid_tiles"));
        }

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.locate_activity);

        initViews();
        setupMap();
        setupDetailCardActions();
        setupBottomNav();
        loadReportMarkers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        loadReportMarkers(); // refresh kalau ada laporan baru sejak terakhir buka halaman ini
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    private void initViews() {
        mapView       = findViewById(R.id.mapView);
        tvReportCount = findViewById(R.id.tvReportCount);
        tvEmptyState  = findViewById(R.id.tvEmptyState);

        detailCard           = findViewById(R.id.detailCard);
        ivDetailPhoto        = findViewById(R.id.ivDetailPhoto);
        tvDetailCategory     = findViewById(R.id.tvDetailCategory);
        tvDetailAddress      = findViewById(R.id.tvDetailAddress);
        tvDetailTime         = findViewById(R.id.tvDetailTime);
        tvDetailDescription  = findViewById(R.id.tvDetailDescription);
        btnOpenMaps          = findViewById(R.id.btnOpenMaps);
        btnCloseDetail       = findViewById(R.id.btnCloseDetail);

        navHome    = findViewById(R.id.navHome);
        navEco     = findViewById(R.id.navEco);
        navHistory = findViewById(R.id.navHistory);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(new GeoPoint(DEFAULT_LAT, DEFAULT_LNG));
    }

    private void setupDetailCardActions() {
        btnCloseDetail.setOnClickListener(v -> detailCard.setVisibility(View.GONE));

        btnOpenMaps.setOnClickListener(v -> {
            if (selectedReport == null || selectedReport.latitude == null || selectedReport.longitude == null) {
                return;
            }
            openInMaps(selectedReport.latitude, selectedReport.longitude, selectedReport.category);
        });
    }

    // ════════════════════════════════════════
    //  Load laporan & pasang marker
    // ════════════════════════════════════════

    private void loadReportMarkers() {
        executor.execute(() -> {
            List<Report> reports = AppDatabase.getInstance(getApplicationContext()).reportDao().getAllReports();

            List<Report> withCoords = new ArrayList<>();
            for (Report r : reports) {
                if (r.latitude != null && r.longitude != null) withCoords.add(r);
            }

            runOnUiThread(() -> {
                renderMarkers(withCoords);
                tvReportCount.setText("📍  " + withCoords.size() + " Laporan Aktif");
                tvEmptyState.setVisibility(withCoords.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void renderMarkers(List<Report> reports) {
        mapView.getOverlays().clear();

        if (reports.isEmpty()) {
            mapView.invalidate();
            return;
        }

        double sumLat = 0, sumLng = 0;
        for (Report r : reports) {
            sumLat += r.latitude;
            sumLng += r.longitude;

            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(r.latitude, r.longitude));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(r.category);
            marker.setOnMarkerClickListener((m, mv) -> {
                showReportDetail(r);
                return true;
            });
            mapView.getOverlays().add(marker);
        }

        GeoPoint center = new GeoPoint(sumLat / reports.size(), sumLng / reports.size());
        mapView.getController().setCenter(center);
        mapView.getController().setZoom(reports.size() == 1 ? 15.0 : 12.0);
        mapView.invalidate();
    }

    private void showReportDetail(Report report) {
        selectedReport = report;

        tvDetailCategory.setText(report.category != null ? report.category : "Report");
        tvDetailAddress.setText(report.location != null ? report.location : "-");
        tvDetailTime.setText(formatTime(report.timestamp));
        tvDetailDescription.setText(
                TextUtils.isEmpty(report.description) ? "Tidak ada deskripsi tambahan." : report.description
        );

        if (report.photoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(report.photoPath);
            if (bitmap != null) {
                ivDetailPhoto.setImageBitmap(bitmap);
                ivDetailPhoto.setVisibility(View.VISIBLE);
            } else {
                ivDetailPhoto.setVisibility(View.GONE);
            }
        } else {
            ivDetailPhoto.setVisibility(View.GONE);
        }

        detailCard.setVisibility(View.VISIBLE);
    }

    private String formatTime(long timestamp) {
        return new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    private void openInMaps(double lat, double lng, String label) {
        String safeLabel = label != null ? label : "Lokasi Laporan";
        String uri = String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)", lat, lng, lat, lng, Uri.encode(safeLabel));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Tidak ada aplikasi peta yang terpasang", Toast.LENGTH_SHORT).show();
        }
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

        navEco.setOnClickListener(v ->
                startActivity(new Intent(this, EcoActivity.class)));

        navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        // navLocate tidak perlu listener navigasi — kita memang sudah di halaman ini

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