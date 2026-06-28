package com.mantao.star;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReportDao {

    @Insert
    void insert(Report report);

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    List<Report> getAllReports();
}