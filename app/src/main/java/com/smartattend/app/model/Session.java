package com.smartattend.app.model;

import java.util.Objects;

public class Session {
    private String sessionId = "";
    private String subjectCode = "";
    private String subjectName = "";
    private String teacherId = "";
    private String teacherName = "";
    private String qrToken = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private int radiusMetres = 20;
    private long startTime = 0L;
    private long expiryTime = 0L;
    private boolean isActive = true;
    private String room = "";

    public Session() {}

    public Session(String sessionId, String subjectCode, String subjectName, String teacherId, String teacherName, String qrToken, double latitude, double longitude, int radiusMetres, long startTime, long expiryTime, boolean isActive, String room) {
        this.sessionId = sessionId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.qrToken = qrToken;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMetres = radiusMetres;
        this.startTime = startTime;
        this.expiryTime = expiryTime;
        this.isActive = isActive;
        this.room = room;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getRadiusMetres() { return radiusMetres; }
    public void setRadiusMetres(int radiusMetres) { this.radiusMetres = radiusMetres; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getExpiryTime() { return expiryTime; }
    public void setExpiryTime(long expiryTime) { this.expiryTime = expiryTime; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Double.compare(session.latitude, latitude) == 0 && Double.compare(session.longitude, longitude) == 0 && radiusMetres == session.radiusMetres && startTime == session.startTime && expiryTime == session.expiryTime && isActive == session.isActive && Objects.equals(sessionId, session.sessionId) && Objects.equals(subjectCode, session.subjectCode) && Objects.equals(subjectName, session.subjectName) && Objects.equals(teacherId, session.teacherId) && Objects.equals(teacherName, session.teacherName) && Objects.equals(qrToken, session.qrToken) && Objects.equals(room, session.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, subjectCode, subjectName, teacherId, teacherName, qrToken, latitude, longitude, radiusMetres, startTime, expiryTime, isActive, room);
    }
}
