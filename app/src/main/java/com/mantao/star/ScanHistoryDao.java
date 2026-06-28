package com.mantao.star;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ScanHistoryDao {

    @Insert
    void insert(ScanHistory entry);

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    List<ScanHistory> getAll();
}