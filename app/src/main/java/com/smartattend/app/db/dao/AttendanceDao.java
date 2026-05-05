package com.smartattend.app.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.smartattend.app.db.AttendanceEntity;

import java.util.List;

@Dao
public interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AttendanceEntity record);

    @Query("SELECT * FROM attendance_cache WHERE studentId = :studentId ORDER BY timestamp DESC")
    LiveData<List<AttendanceEntity>> getAllByStudent(String studentId);

    @Query("SELECT * FROM attendance_cache WHERE studentId = :studentId AND subjectCode = :code")
    List<AttendanceEntity> getBySubject(String studentId, String code);

    @Query("SELECT * FROM attendance_cache WHERE synced = 0")
    List<AttendanceEntity> getUnsynced();

    @Query("UPDATE attendance_cache SET synced = 1 WHERE id = :id")
    void markSynced(String id);

    @Query("SELECT COUNT(*) FROM attendance_cache WHERE studentId = :sid AND subjectCode = :code AND geoVerified = 1 AND faceVerified = 1")
    int getVerifiedCount(String sid, String code);
}
