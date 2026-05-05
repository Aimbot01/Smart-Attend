package com.smartattend.app.repository;

import android.graphics.Bitmap;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartattend.app.model.AttendanceRecord;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AttendanceRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<String> uploadSelfie(String uid, Bitmap bitmap) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] bytes = baos.toByteArray();

        MediaManager.get().upload(bytes)
                .unsigned("smart_attend_preset")
                .option("folder", "selfies/" + uid)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytesUploaded, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        tcs.setResult((String) resultData.get("secure_url"));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        tcs.setException(new Exception(error.getDescription()));
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        tcs.setException(new Exception("Upload rescheduled"));
                    }
                }).dispatch();

        return tcs.getTask();
    }

    public Task<String> submitAttendance(AttendanceRecord record) {
        return db.collection("attendance")
                .whereEqualTo("studentId", record.getStudentId())
                .whereEqualTo("sessionId", record.getSessionId())
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        return Tasks.forException(new Exception("You already marked attendance for this session"));
                    }
                    String id = UUID.randomUUID().toString();
                    record.setId(id);
                    record.setTimestamp(System.currentTimeMillis());
                    return db.collection("attendance").document(id).set(record).continueWith(setTask -> {
                        if (setTask.isSuccessful()) {
                            return id;
                        } else {
                            throw new Exception("Failed to submit");
                        }
                    });
                });
    }

    public Task<List<AttendanceRecord>> getStudentAttendance(String studentId) {
        return db.collection("attendance")
                .whereEqualTo("studentId", studentId)
                .get()
                .continueWith(task -> {
                    List<AttendanceRecord> list = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult().getDocuments()) {
                            AttendanceRecord rec = doc.toObject(AttendanceRecord.class);
                            if (rec != null) list.add(rec);
                        }
                    }
                    return list;
                });
    }

    public Task<Float> getAttendancePercent(String studentId, String subjectCode) {
        return db.collection("sessions")
                .whereEqualTo("subjectCode", subjectCode)
                .whereEqualTo("isActive", false)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) return Tasks.forResult(0f);
                    int total = task.getResult().size();
                    if (total == 0) return Tasks.forResult(0f);

                    return db.collection("attendance")
                            .whereEqualTo("studentId", studentId)
                            .whereEqualTo("subjectCode", subjectCode)
                            .whereEqualTo("geoVerified", true)
                            .whereEqualTo("faceVerified", true)
                            .get()
                            .continueWith(presentTask -> {
                                if (!presentTask.isSuccessful()) return 0f;
                                int present = presentTask.getResult().size();
                                return (present / (float) total) * 100f;
                            });
                });
    }

    public com.google.firebase.firestore.ListenerRegistration listenToAllAttendance(com.smartattend.app.repository.SessionRepository.OnAttendanceChangeListener listener) {
        return db.collection("attendance")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;
                    List<AttendanceRecord> list = new ArrayList<>();
                    if (snapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                            AttendanceRecord rec = doc.toObject(AttendanceRecord.class);
                            if (rec != null) list.add(rec);
                        }
                    }
                    listener.onChange(list);
                });
    }
}
