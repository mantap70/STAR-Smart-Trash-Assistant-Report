package com.mantao.star;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Satu baris data laporan sampah/insiden yang disimpan secara lokal.
 * Tabel ini nantinya bisa di-query dari halaman History.
 */
@Entity(tableName = "reports")
public class Report {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "photo_path")
    public String photoPath; // path file lokal hasil copy dari galeri, bisa null kalau gak ada foto

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "location")
    public String location;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "latitude")
    public Double latitude; // null kalau lokasi diisi manual tanpa GPS

    @ColumnInfo(name = "longitude")
    public Double longitude; // null kalau lokasi diisi manual tanpa GPS

    @ColumnInfo(name = "timestamp")
    public long timestamp;
}