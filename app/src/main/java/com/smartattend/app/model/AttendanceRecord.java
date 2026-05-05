package com.smartattend.app.model;

import java.util.Objects;

public class AttendanceRecord {
    private String id = "";
    private String studentId = "";
    private String studentName = "";
    private String sessionId = "";
    private String subjectCode = "";
    private String subjectName = "";
    private long timestamp = 0L;
    private boolean geoVerified = false;
    private float distanceMetres = 0f;
    private boolean faceVerified = false;
    private float faceMatchScore = 0f;
    private String selfieUrl = "";
    private boolean proxyBlocked = false;

    public AttendanceRecord() {}

    public AttendanceRecord(String id, String studentId, String studentName, String sessionId, String subjectCode, String subjectName, long timestamp, boolean geoVerified, float distanceMetres, boolean faceVerified, float faceMatchScore, String selfieUrl, boolean proxyBlocked) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.sessionId = sessionId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.timestamp = timestamp;
        this.geoVerified = geoVerified;
        this.distanceMetres = distanceMetres;
        this.faceVerified = faceVerified;
        this.faceMatchScore = faceMatchScore;
        this.selfieUrl = selfieUrl;
        this.proxyBlocked = proxyBlocked;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isGeoVerified() { return geoVerified; }
    public void setGeoVerified(boolean geoVerified) { this.geoVerified = geoVerified; }

    public float getDistanceMetres() { return distanceMetres; }
    public void setDistanceMetres(float distanceMetres) { this.distanceMetres = distanceMetres; }

    public boolean isFaceVerified() { return faceVerified; }
    public void setFaceVerified(boolean faceVerified) { this.faceVerified = faceVerified; }

    public float getFaceMatchScore() { return faceMatchScore; }
    public void setFaceMatchScore(float faceMatchScore) { this.faceMatchScore = faceMatchScore; }

    public String getSelfieUrl() { return selfieUrl; }
    public void setSelfieUrl(String selfieUrl) { this.selfieUrl = selfieUrl; }

    public boolean isProxyBlocked() { return proxyBlocked; }
    public void setProxyBlocked(boolean proxyBlocked) { this.proxyBlocked = proxyBlocked; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceRecord that = (AttendanceRecord) o;
        return timestamp == that.timestamp && geoVerified == that.geoVerified && Float.compare(that.distanceMetres, distanceMetres) == 0 && faceVerified == that.faceVerified && Float.compare(that.faceMatchScore, faceMatchScore) == 0 && proxyBlocked == that.proxyBlocked && Objects.equals(id, that.id) && Objects.equals(studentId, that.studentId) && Objects.equals(studentName, that.studentName) && Objects.equals(sessionId, that.sessionId) && Objects.equals(subjectCode, that.subjectCode) && Objects.equals(subjectName, that.subjectName) && Objects.equals(selfieUrl, that.selfieUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, studentId, studentName, sessionId, subjectCode, subjectName, timestamp, geoVerified, distanceMetres, faceVerified, faceMatchScore, selfieUrl, proxyBlocked);
    }
}
