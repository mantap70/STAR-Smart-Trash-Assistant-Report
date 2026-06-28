package com.mantao.star;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Satu baris hasil scan sampah yang berhasil dikenali (confidence di atas threshold),
 * lengkap dengan nama material, proses daur ulang, dan eco points-nya.
 */
@Entity(tableName = "scan_history")
public class ScanHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "display_name")
    public String displayName; // misal "Plastic Bottle"

    @ColumnInfo(name = "raw_label")
    public String rawLabel; // label asli dari MobileNet, misal "water bottle"

    @ColumnInfo(name = "category")
    public String category; // "Organik" atau "Anorganik"

    @ColumnInfo(name = "process_name")
    public String processName; // misal "PET Cycle"

    @ColumnInfo(name = "eco_points")
    public int ecoPoints;

    @ColumnInfo(name = "timestamp")
    public long timestamp;
}