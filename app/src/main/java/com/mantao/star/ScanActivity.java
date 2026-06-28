package com.mantao.star;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.TextureView;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.Collections;

public class ScanActivity extends AppCompatActivity {

    // ─── Views ───
    private TextureView cameraPreview;
    private android.view.View scanLine;
    private TextView tvStatus;
    private ImageView ivFlash;
    private FrameLayout btnFlash, btnGallery;
    private LinearLayout navHome, navLocate, navEco, navHistory, navScan;

    // ─── Camera2 ───
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private String cameraId;
    private boolean isFlashOn = false;
    private boolean isFlashSupported = false;

    // ─── Scan line animator ───
    private ObjectAnimator scanAnimator;

    // ─── Waste classifier (TFLite, MobileNetV2 + mapping organik/anorganik) ───
    private WasteClassifier wasteClassifier;
    private Handler classifyHandler;
    private static final long CLASSIFY_INTERVAL_MS = 800; // jeda antar inference, biar gak berat
    private static final float CONFIDENCE_THRESHOLD = 0.35f; // model 1000 kelas, confidence wajar lebih rendah dari model binary

    // ─── State untuk simpan ke history (biar gak spam tiap 800ms) ───
    private String lastSavedLabel = null;
    private long lastSavedTimestamp = 0L;
    private static final long SAVE_COOLDOWN_MS = 4000;

    private final Runnable classifyRunnable = new Runnable() {
        @Override
        public void run() {
            runClassificationOnce();
            if (classifyHandler != null) {
                classifyHandler.postDelayed(classifyRunnable, CLASSIFY_INTERVAL_MS);
            }
        }
    };

    // ─── Permission launcher ───
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    openCamera();
                } else {
                    Toast.makeText(this,
                            "Izin kamera diperlukan untuk fitur scan",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    // ─── Gallery picker launcher ───
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) handleGalleryImage(uri);
            });

    // ════════════════════════════════════════
    //  TextureView Listener
    // ════════════════════════════════════════

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface,
                                                      int width, int height) {
                    openCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface,
                                                        int width, int height) { }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) { }
            };

    // ─── Camera2 state callback ───
    private final CameraDevice.StateCallback cameraStateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    Toast.makeText(ScanActivity.this,
                            "Kamera error: " + error, Toast.LENGTH_SHORT).show();
                }
            };

    // ════════════════════════════════════════
    //  Lifecycle
    // ════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        setContentView(R.layout.scan_activity);

        initViews();
        setupBottomNav();
        setupSideButtons();
        initClassifier();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if (cameraPreview.isAvailable()) {
            checkCameraPermission();
        } else {
            cameraPreview.setSurfaceTextureListener(surfaceTextureListener);
        }

        startScanLineAnimation();
    }

    @Override
    protected void onPause() {
        stopRealtimeClassification();
        closeCamera();
        stopBackgroundThread();
        stopScanLineAnimation();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (wasteClassifier != null) {
            wasteClassifier.close();
            wasteClassifier = null;
        }
        super.onDestroy();
    }

    // ════════════════════════════════════════
    //  Init Views
    // ════════════════════════════════════════

    private void initViews() {
        cameraPreview = findViewById(R.id.cameraPreview);
        scanLine      = findViewById(R.id.scanLine);
        tvStatus      = findViewById(R.id.tvStatus);
        ivFlash       = findViewById(R.id.ivFlash);
        btnFlash      = findViewById(R.id.btnFlash);
        btnGallery    = findViewById(R.id.btnGallery);

        navHome    = findViewById(R.id.navHome);
        navLocate  = findViewById(R.id.navLocate);
        navEco     = findViewById(R.id.navEco);
        navHistory = findViewById(R.id.navHistory);
        navScan    = findViewById(R.id.navScan);
    }

    // ════════════════════════════════════════
    //  Waste Classifier (TFLite) — init
    // ════════════════════════════════════════

    private void initClassifier() {
        classifyHandler = new Handler(getMainLooper());
        try {
            wasteClassifier = new WasteClassifier(this);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal load model klasifikasi", Toast.LENGTH_SHORT).show();
        }
    }

    // ════════════════════════════════════════
    //  Background Thread
    // ════════════════════════════════════════

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ════════════════════════════════════════
    //  Permission
    // ════════════════════════════════════════

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // ════════════════════════════════════════
    //  Camera2 — Open
    // ════════════════════════════════════════

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            // Ambil kamera belakang
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics chars = manager.getCameraCharacteristics(id);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;

                    // Cek flash support
                    Boolean flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    isFlashSupported = flashAvailable != null && flashAvailable;
                    break;
                }
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                manager.openCamera(cameraId, cameraStateCallback, backgroundHandler);
            }

        } catch (CameraAccessException e) {
            Toast.makeText(this, "Gagal akses kamera", Toast.LENGTH_SHORT).show();
        }
    }

    // ════════════════════════════════════════
    //  Camera2 — Preview Session
    // ════════════════════════════════════════

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = cameraPreview.getSurfaceTexture();
            if (texture == null) return;

            texture.setDefaultBufferSize(1920, 1080);
            Surface surface = new Surface(texture);

            previewRequestBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(
                    Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) return;
                            captureSession = session;

                            previewRequestBuilder.set(
                                    CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            );

                            try {
                                captureSession.setRepeatingRequest(
                                        previewRequestBuilder.build(),
                                        null,
                                        backgroundHandler
                                );
                                updateStatus("DETECTING\nOBJECTS...");
                                startRealtimeClassification();
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(ScanActivity.this,
                                    "Gagal konfigurasi kamera", Toast.LENGTH_SHORT).show();
                        }
                    },
                    backgroundHandler
            );

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════
    //  Camera2 — Close
    // ════════════════════════════════════════

    private void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    // ════════════════════════════════════════
    //  Real-time Waste Classification
    // ════════════════════════════════════════

    private void startRealtimeClassification() {
        if (classifyHandler != null) {
            classifyHandler.removeCallbacks(classifyRunnable);
            classifyHandler.post(classifyRunnable);
        }
    }

    private void stopRealtimeClassification() {
        if (classifyHandler != null) {
            classifyHandler.removeCallbacks(classifyRunnable);
        }
    }

    private void runClassificationOnce() {
        if (wasteClassifier == null || cameraPreview == null || !cameraPreview.isAvailable()) return;

        Bitmap frame = cameraPreview.getBitmap();
        if (frame == null || backgroundHandler == null) return;

        backgroundHandler.post(() -> {
            WasteClassifier.Result result = wasteClassifier.classify(frame);
            runOnUiThread(() -> showClassificationResult(result));
        });
    }

    private void showClassificationResult(WasteClassifier.Result result) {
        if (result.confidence < CONFIDENCE_THRESHOLD) {
            updateStatus("DETECTING\nOBJECTS...");
            return;
        }
        updateStatus(result.category.toUpperCase() + "\n(" + result.rawLabel + ")");
        maybeSaveToHistory(result);
    }

    /**
     * Simpan ke history hanya kalau ini deteksi BARU (label berubah) atau sudah lewat
     * cooldown sejak penyimpanan terakhir — supaya gak nyimpen entry yang sama berkali-kali
     * tiap 800ms selagi kamera diam di objek yang sama.
     */
    private void maybeSaveToHistory(WasteClassifier.Result result) {
        long now = System.currentTimeMillis();
        boolean isNewLabel = !result.rawLabel.equals(lastSavedLabel);
        boolean cooldownPassed = (now - lastSavedTimestamp) >= SAVE_COOLDOWN_MS;

        if (!isNewLabel && !cooldownPassed) return;

        lastSavedLabel = result.rawLabel;
        lastSavedTimestamp = now;

        if (backgroundHandler == null) return;
        backgroundHandler.post(() -> {
            EcoPointsMapper.MaterialInfo info = EcoPointsMapper.getInfo(result.rawLabel, result.category);

            ScanHistory entry = new ScanHistory();
            entry.displayName = info.displayName;
            entry.rawLabel = result.rawLabel;
            entry.category = result.category;
            entry.processName = info.processName;
            entry.ecoPoints = info.ecoPoints;
            entry.timestamp = now;

            AppDatabase.getInstance(getApplicationContext()).scanHistoryDao().insert(entry);
        });
    }

    // ════════════════════════════════════════
    //  Flash
    // ════════════════════════════════════════

    private void toggleFlash() {
        if (!isFlashSupported || captureSession == null) {
            Toast.makeText(this, "Flash tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        isFlashOn = !isFlashOn;

        try {
            previewRequestBuilder.set(
                    CaptureRequest.FLASH_MODE,
                    isFlashOn
                            ? CaptureRequest.FLASH_MODE_TORCH
                            : CaptureRequest.FLASH_MODE_OFF
            );
            captureSession.setRepeatingRequest(
                    previewRequestBuilder.build(), null, backgroundHandler
            );

            // Visual feedback — alpha untuk indikator flash ON/OFF
            ivFlash.setAlpha(isFlashOn ? 1.0f : 0.5f);
            Toast.makeText(this,
                    isFlashOn ? "Flash ON" : "Flash OFF",
                    Toast.LENGTH_SHORT).show();

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════
    //  Gallery
    // ════════════════════════════════════════

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void handleGalleryImage(Uri uri) {
        updateStatus("ANALYZING\nIMAGE...");
        Toast.makeText(this, "Gambar dipilih, sedang dianalisis...",
                Toast.LENGTH_SHORT).show();

        // TODO: decode Bitmap dari uri (BitmapFactory / ContentResolver) lalu panggil
        // wasteClassifier.classify(bitmap) di background thread, sama seperti runClassificationOnce()
        scanLine.postDelayed(() -> updateStatus("DETECTING\nOBJECTS..."), 2000);
    }

    // ════════════════════════════════════════
    //  Scan Line Animation
    // ════════════════════════════════════════

    private void startScanLineAnimation() {
        if (scanAnimator != null && scanAnimator.isRunning()) return;

        scanLine.post(() -> {
            int parentHeight = ((android.view.View) scanLine.getParent()).getHeight();
            if (parentHeight == 0) {
                scanLine.postDelayed(this::startScanLineAnimation, 300);
                return;
            }

            float range = parentHeight * 0.22f;

            scanAnimator = ObjectAnimator.ofFloat(
                    scanLine, "translationY", -range, range
            );
            scanAnimator.setDuration(2000);
            scanAnimator.setRepeatMode(ValueAnimator.REVERSE);
            scanAnimator.setRepeatCount(ValueAnimator.INFINITE);
            scanAnimator.setInterpolator(new LinearInterpolator());
            scanAnimator.start();
        });
    }

    private void stopScanLineAnimation() {
        if (scanAnimator != null) {
            scanAnimator.cancel();
            scanAnimator = null;
        }
    }

    // ════════════════════════════════════════
    //  Side Buttons
    // ════════════════════════════════════════

    private void setupSideButtons() {
        btnFlash.setOnClickListener(v -> toggleFlash());
        btnGallery.setOnClickListener(v -> openGallery());

        if (navScan != null) {
            navScan.setOnClickListener(v -> {
                v.animate()
                        .scaleX(0.9f).scaleY(0.9f).setDuration(100)
                        .withEndAction(() ->
                                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                        .start();
                // Tombol ini sekarang memicu satu kali klasifikasi langsung (selain mode realtime otomatis)
                updateStatus("SCANNING...");
                Toast.makeText(this, "Menganalisis...", Toast.LENGTH_SHORT).show();
                runClassificationOnce();
            });
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

        navLocate.setOnClickListener(v ->
                startActivity(new Intent(this, LocateActivity.class)));

        navEco.setOnClickListener(v ->
                startActivity(new Intent(this, EcoActivity.class)));

        navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
    }

    // ════════════════════════════════════════
    //  Helper
    // ════════════════════════════════════════

    private void updateStatus(String text) {
        runOnUiThread(() -> {
            if (tvStatus != null) tvStatus.setText(text);
        });
    }
}