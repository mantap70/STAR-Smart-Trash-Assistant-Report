package com.mantao.star;

/**
 * Satu baris di feed History — bisa berasal dari ScanHistory atau Report,
 * disatukan jadi satu bentuk biar gampang dirender dan diurutkan bareng.
 */
public class HistoryItem {

    public static final int TYPE_SCAN = 1;
    public static final int TYPE_REPORT = 2;

    public int type;
    public String title;      // displayName (scan) atau category (report)
    public String subtitle;   // waktu + process name (scan) atau waktu + lokasi (report)
    public int ecoPoints;
    public String photoPath;  // null untuk scan, terisi untuk report kalau ada foto
    public long timestamp;
}