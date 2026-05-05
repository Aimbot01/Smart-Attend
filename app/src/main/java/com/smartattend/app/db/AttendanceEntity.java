package com.smartattend.app.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "attendance_cache")
public class AttendanceEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String studentId;
    private String subjectCode;
    private String subjectName;
    private String sessionId;
    private long timestamp;
    private boolean geoVerified;
    private boolean faceVerified;
    private float distanceMetres;
    private float faceMatchScore;
    private boolean synced;

    public AttendanceEntity(@NonNull String id, String studentId, String subjectCode, String subjectName, String sessionId, long timestamp, boolean geoVerified, boolean faceVerified, float distanceMetres, float faceMatchScore, boolean synced) {
        this.id = id;
        this.studentId = studentId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.geoVerified = geoVerified;
        this.faceVerified = faceVerified;
        this.distanceMetres = distanceMetres;
        this.faceMatchScore = faceMatchScore;
        this.synced = synced;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isGeoVerified() { return geoVerified; }
    public void setGeoVerified(boolean geoVerified) { this.geoVerified = geoVerified; }

    public boolean isFaceVerified() { return faceVerified; }
    public void setFaceVerified(boolean faceVerified) { this.faceVerified = faceVerified; }

    public float getDistanceMetres() { return distanceMetres; }
    public void setDistanceMetres(float distanceMetres) { this.distanceMetres = distanceMetres; }

    public float getFaceMatchScore() { return faceMatchScore; }
    public void setFaceMatchScore(float faceMatchScore) { this.faceMatchScore = faceMatchScore; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
}
