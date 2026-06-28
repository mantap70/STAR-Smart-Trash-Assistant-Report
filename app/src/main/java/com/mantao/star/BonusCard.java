package com.mantao.star;

/**
 * Kartu bonus mingguan yang dihitung secara dinamis (bukan disimpan di database),
 * muncul kalau user cukup aktif (scan/report) dalam beberapa hari di seminggu terakhir.
 */
public class BonusCard {
    public String label;  // "WEEKLY STREAK BONUS"
    public String title;  // "Master Recycler"
    public int points;    // 500
}