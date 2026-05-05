package com.smartattend.app.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.model.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SessionRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Session> createSession(String subjectCode, String subjectName, String teacherId, String teacherName, double latitude, double longitude, String room) {
        String sessionId = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        Session session = new Session(sessionId, subjectCode, subjectName, teacherId, teacherName, token, latitude, longitude, 10, now, now + 5 * 60 * 1000L, true, room);

        return db.collection("sessions").document(sessionId).set(session).continueWith(task -> {
            if (task.isSuccessful()) {
                return session;
            } else {
                throw new Exception("Failed to create session", task.getException());
            }
        });
    }

    public Task<String> refreshToken(String sessionId) {
        String newToken = UUID.randomUUID().toString();
        long newExpiry = System.currentTimeMillis() + 5 * 60 * 1000L;
        Map<String, Object> updates = new HashMap<>();
        updates.put("qrToken", newToken);
        updates.put("expiryTime", newExpiry);
        return db.collection("sessions").document(sessionId).update(updates).continueWith(task -> {
            if (task.isSuccessful()) {
                return newToken;
            } else {
                throw new Exception("Failed to refresh token", task.getException());
            }
        });
    }

    public Task<Void> endSession(String sessionId) {
        return db.collection("sessions").document(sessionId).update("isActive", false);
    }

    public Task<Session> validateToken(String scannedToken) {
        // Simplified query to avoid index requirements during initial setup
        return db.collection("sessions")
                .whereEqualTo("qrToken", scannedToken)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Session session = task.getResult().getDocuments().get(0).toObject(Session.class);
                        if (session == null) throw new Exception("Session data error");
                        
                        if (!session.isActive()) {
                            throw new Exception("This class session has ended.");
                        }
                        
                        long now = System.currentTimeMillis();
                        if (now > session.getExpiryTime()) {
                            throw new Exception("QR code has expired. Ask teacher to refresh.");
                        }
                        return session;
                    } else {
                        throw new Exception("Invalid QR code. Please scan again.");
                    }
                });
    }

    public interface OnAttendanceChangeListener {
        void onChange(List<AttendanceRecord> records);
    }

    public ListenerRegistration listenToAttendance(String sessionId, OnAttendanceChangeListener listener) {
        return db.collection("attendance")
                .whereEqualTo("sessionId", sessionId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;
                    List<AttendanceRecord> list = new ArrayList<>();
                    if (snapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                            AttendanceRecord record = doc.toObject(AttendanceRecord.class);
                            if (record != null) list.add(record);
                        }
                    }
                    listener.onChange(list);
                });
    }
}
