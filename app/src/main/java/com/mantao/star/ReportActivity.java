package com.mantao.star;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportActivity extends AppCompatActivity {

    private static final String[] CATEGORIES = {
            "Illegal Dumping", "Overflowing Bin", "Littering",
            "Hazardous Waste", "Other"
    };

    // ─── Views ───
    private FrameLayout uploadBox, btnGps;
    private ImageView ivPhotoPreview;
    private LinearLayout uploadPlaceholder, categoryBox, btnSubmit;
    private TextView tvCategory;
    private EditText etLocation, etDescription;

    private LinearLayout navHome, navLocate, navEco, navHistory;

    // ─── State ───
    private Uri selectedPhotoUri;
    private String selectedCategory = CATEGORIES[0];
    private Double selectedLatitude;
    private Double selectedLongitude;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LocationManager locationManager;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    showPhotoPreview(uri);
                }
            });

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (granted) {
                    fetchCurrentLocation();
                } else {
                    Toast.makeText(this, "Izin lokasi diperlukan untuk fitur ini", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.report_activity);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        initViews();
        setupUpload();
        setupCategory();
        setupLocation();
        setupSubmit();
        setupBottomNav();
    }

    // ════════════════════════════════════════
    //  Init Views
    // ════════════════════════════════════════

    private void initViews() {
        uploadBox         = findViewById(R.id.uploadBox);
        uploadPlaceholder = findViewById(R.id.uploadPlaceholder);
        ivPhotoPreview    = findViewById(R.id.ivPhotoPreview);
        categoryBox       = findViewById(R.id.categoryBox);
        tvCategory        = findViewById(R.id.tvCategory);
        etLocation        = findViewById(R.id.etLocation);
        btnGps            = findViewById(R.id.btnGps);
        etDescription     = findViewById(R.id.etDescription);
        btnSubmit         = findViewById(R.id.btnSubmit);

        navHome    = findViewById(R.id.navHome);
        navLocate  = findViewById(R.id.navLocate);
        navEco     = findViewById(R.id.navEco);
        navHistory = findViewById(R.id.navHistory);
    }

    // ════════════════════════════════════════
    //  Upload Foto
    // ════════════════════════════════════════

    private void setupUpload() {
        uploadBox.setOnClickListener(v -> galleryLauncher.launch("image/*"));
    }

    private void showPhotoPreview(Uri uri) {
        ivPhotoPreview.setImageURI(uri);
        ivPhotoPreview.setVisibility(View.VISIBLE);
        uploadPlaceholder.setVisibility(View.GONE);
    }

    // ════════════════════════════════════════
    //  Kategori
    // ════════════════════════════════════════

    private void setupCategory() {
        categoryBox.setOnClickListener(v -> {
            int checkedIndex = indexOfCategory(selectedCategory);
            new AlertDialog.Builder(this)
                    .setTitle("Pilih Kategori")
                    .setSingleChoiceItems(CATEGORIES, checkedIndex, (dialog, which) -> {
                        selectedCategory = CATEGORIES[which];
                        tvCategory.setText(selectedCategory);
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    private int indexOfCategory(String value) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(value)) return i;
        }
        return 0;
    }

    // ════════════════════════════════════════
    //  Lokasi (GPS + Geocoder)
    // ════════════════════════════════════════

    private void setupLocation() {
        btnGps.setOnClickListener(v -> {
            boolean fineGranted = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean coarseGranted = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (fineGranted || coarseGranted) {
                fetchCurrentLocation();
            } else {
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });

        // Kalau user edit teks lokasi secara manual, anggap koordinat GPS yang lama gak valid lagi.
        // (reverseGeocode() akan set ulang lat/lng yang benar setelah baris setText-nya, jadi gak masalah)
        etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedLatitude = null;
                selectedLongitude = null;
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        Toast.makeText(this, "Mencari lokasi...", Toast.LENGTH_SHORT).show();

        Location best = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            best = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (best == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            best = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (best != null) {
            reverseGeocode(best.getLatitude(), best.getLongitude());
            return;
        }

        // Belum ada lokasi tersimpan sebelumnya, minta update sekali
        String provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ? LocationManager.GPS_PROVIDER
                : LocationManager.NETWORK_PROVIDER;

        try {
            locationManager.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    reverseGeocode(location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) { }

                @Override
                public void onProviderEnabled(String provider) { }

                @Override
                public void onProviderDisabled(String provider) {
                    Toast.makeText(ReportActivity.this, "Aktifkan GPS untuk fitur ini", Toast.LENGTH_SHORT).show();
                }
            }, Looper.getMainLooper());
        } catch (Exception e) {
            Toast.makeText(this, "Gagal mengambil lokasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void reverseGeocode(double lat, double lng) {
        executor.execute(() -> {
            String address = null;
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> results = geocoder.getFromLocation(lat, lng, 1);
                if (results != null && !results.isEmpty()) {
                    address = results.get(0).getAddressLine(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final String finalAddress = address != null
                    ? address
                    : String.format(Locale.getDefault(), "%.6f, %.6f", lat, lng);

            runOnUiThread(() -> {
                etLocation.setText(finalAddress); // ini trigger TextWatcher yang clear lat/lng ke null
                selectedLatitude = lat;           // di-set ulang sesudahnya, jadi nilai final-nya benar
                selectedLongitude = lng;
            });
        });
    }

    // ════════════════════════════════════════
    //  Submit
    // ════════════════════════════════════════

    private void setupSubmit() {
        btnSubmit.setOnClickListener(v -> {
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                    .start();
            trySubmitReport();
        });
    }

    private void trySubmitReport() {
        String location = etLocation.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Lokasi belum diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Deskripsi belum diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);

        executor.execute(() -> {
            String photoPath = null;
            if (selectedPhotoUri != null) {
                photoPath = copyPhotoToInternalStorage(selectedPhotoUri);
            }

            Report report = new Report();
            report.photoPath = photoPath;
            report.category = selectedCategory;
            report.location = location;
            report.description = description;
            report.latitude = selectedLatitude;
            report.longitude = selectedLongitude;
            report.timestamp = System.currentTimeMillis();

            AppDatabase.getInstance(getApplicationContext()).reportDao().insert(report);

            runOnUiThread(() -> {
                btnSubmit.setEnabled(true);
                showSuccessDialog();
            });
        });
    }

    /** Copy foto dari galeri ke internal storage app, supaya path-nya stabil dan gak hilang. */
    private String copyPhotoToInternalStorage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return null;

            File outDir = new File(getFilesDir(), "report_photos");
            if (!outDir.exists()) outDir.mkdirs();

            File outFile = new File(outDir, "report_" + System.currentTimeMillis() + ".jpg");

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            if (bitmap == null) return null;

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            }

            return outFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Laporan Terkirim")
                .setMessage("Terima kasih! Laporan kamu sudah tersimpan dan bisa dilihat di halaman History.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    // ════════════════════════════════════════
    //  Bottom Navigation (struktur sama dengan halaman lain)
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

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}