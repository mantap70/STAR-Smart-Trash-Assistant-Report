package com.mantao.star;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Report.class, ScanHistory.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract ReportDao reportDao();

    public abstract ScanHistoryDao scanHistoryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "star_database"
                    )
                    // Karena masih fase development, skema yang berubah cukup hapus data lama
                    // daripada nulis Migration manual. Ganti ke Migration kalau sudah rilis ke user.
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}