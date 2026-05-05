package com.smartattend.app.viewmodel;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import android.graphics.PointF;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.model.Session;
import com.smartattend.app.model.Subject;
import com.smartattend.app.repository.AttendanceRepository;
import com.smartattend.app.repository.SubjectRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceViewModel extends ViewModel {

    private final AttendanceRepository repo = new AttendanceRepository();
    private final SubjectRepository subjectRepo = new SubjectRepository();

    public static class SubjectAttendance {
        public String subjectCode;
        public String subjectName;
        public int presentCount;
        public int totalCount;
        public float percentage;

        public SubjectAttendance(String subjectCode, String subjectName, int presentCount, int totalCount, float percentage) {
            this.subjectCode = subjectCode;
            this.subjectName = subjectName;
            this.presentCount = presentCount;
            this.totalCount = totalCount;
            this.percentage = percentage;
        }
    }

    private final MutableLiveData<Boolean> _faceDetected = new MutableLiveData<>();
    public final LiveData<Boolean> faceDetected = _faceDetected;

    private final MutableLiveData<Float> _livenessScore = new MutableLiveData<>();
    public final LiveData<Float> livenessScore = _livenessScore;

    private final MutableLiveData<Float> _similarityScore = new MutableLiveData<>();
    public final LiveData<Float> similarityScore = _similarityScore;

    private final MutableLiveData<String> _selfieUrl = new MutableLiveData<>();
    public final LiveData<String> selfieUrl = _selfieUrl;

    private final MutableLiveData<Boolean> _attendanceSubmitted = new MutableLiveData<>();
    public final LiveData<Boolean> attendanceSubmitted = _attendanceSubmitted;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public final LiveData<Boolean> loading = _loading;

    private final MutableLiveData<Float> _verifiedDistance = new MutableLiveData<>(0f);
    public final LiveData<Float> verifiedDistance = _verifiedDistance;

    private Bitmap capturedBitmap;

    public void setVerifiedDistance(float distance) {
        _verifiedDistance.setValue(distance);
    }

    public void detectFace(Bitmap bitmap, String uid, Bitmap referenceBitmap) {
        this.capturedBitmap = bitmap;
        _loading.setValue(true);
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .build();

            FaceDetector detector = FaceDetection.getClient(options);
            detector.process(image)
                    .addOnSuccessListener(faces -> {
                        if (faces.isEmpty()) {
                            _faceDetected.setValue(false);
                            _error.setValue("No face detected. Please take a clearer selfie.");
                            _loading.setValue(false);
                            return;
                        }

                        Face face = faces.get(0);
                        float leftEye = face.getLeftEyeOpenProbability() != null ? face.getLeftEyeOpenProbability() : 0f;
                        float rightEye = face.getRightEyeOpenProbability() != null ? face.getRightEyeOpenProbability() : 0f;
                        float avgScore = (leftEye + rightEye) / 2f;

                        if (avgScore < 0.5f) {
                            _faceDetected.setValue(false);
                            _error.setValue("Liveness check failed. Please open your eyes and try again.");
                            _loading.setValue(false);
                            return;
                        }

                        _livenessScore.setValue(avgScore);
                        
                        if (referenceBitmap != null) {
                            performSimilarityCheck(face, referenceBitmap, options, uid, bitmap);
                        } else {
                            _similarityScore.setValue(1.0f);
                            _faceDetected.setValue(true);
                            uploadAndFinish(uid, bitmap);
                        }
                    })
                    .addOnFailureListener(e -> {
                        _error.setValue(e.getMessage());
                        _loading.setValue(false);
                    });

        } catch (Exception e) {
            _error.setValue(e.getMessage());
            _loading.setValue(false);
        }
    }

    private void performSimilarityCheck(Face currentFace, Bitmap referenceBitmap, FaceDetectorOptions options, String uid, Bitmap currentBitmap) {
        InputImage refImage = InputImage.fromBitmap(referenceBitmap, 0);
        FaceDetector detector = FaceDetection.getClient(options);
        
        detector.process(refImage)
                .addOnSuccessListener(faces -> {
                    if (faces.isEmpty()) {
                        _error.setValue("No face found in profile. Verification continued.");
                        _faceDetected.setValue(true);
                        uploadAndFinish(uid, currentBitmap);
                        return;
                    }
                    
                    Face refFace = faces.get(0);
                    float similarity = calculateSimilarity(currentFace, refFace);
                    _similarityScore.setValue(similarity);
                    
                    if (similarity < 0.65f) {
                        _error.setValue("Identity mismatch! Face does not match registered profile.");
                        _loading.setValue(false);
                    } else {
                        _faceDetected.setValue(true);
                        uploadAndFinish(uid, currentBitmap);
                    }
                })
                .addOnFailureListener(e -> {
                    _error.setValue("Similarity check error: " + e.getMessage());
                    _loading.setValue(false);
                });
    }

    private float calculateSimilarity(Face face1, Face face2) {
        int[] landmarkTypes = {
            FaceLandmark.LEFT_EYE, FaceLandmark.RIGHT_EYE, 
            FaceLandmark.NOSE_BASE, FaceLandmark.MOUTH_BOTTOM
        };
        
        float totalDist = 0;
        int count = 0;
        
        for (int type : landmarkTypes) {
            FaceLandmark l1 = face1.getLandmark(type);
            FaceLandmark l2 = face2.getLandmark(type);
            if (l1 != null && l2 != null) {
                PointF p1 = l1.getPosition();
                PointF p2 = l2.getPosition();
                
                float n1x = (p1.x - face1.getBoundingBox().left) / (float) face1.getBoundingBox().width();
                float n1y = (p1.y - face1.getBoundingBox().top) / (float) face1.getBoundingBox().height();
                
                float n2x = (p2.x - face2.getBoundingBox().left) / (float) face2.getBoundingBox().width();
                float n2y = (p2.y - face2.getBoundingBox().top) / (float) face2.getBoundingBox().height();
                
                totalDist += (float) Math.sqrt(Math.pow(n1x - n2x, 2) + Math.pow(n1y - n2y, 2));
                count++;
            }
        }
        
        if (count == 0) return 0f;
        float avgDist = totalDist / count;
        return Math.max(0, 1.0f - (avgDist * 2.5f)); 
    }

    private void uploadAndFinish(String uid, Bitmap bitmap) {
        repo.uploadSelfie(uid, bitmap)
                .addOnSuccessListener(url -> {
                    _selfieUrl.setValue(url);
                    _loading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _loading.setValue(false);
                });
    }

    public void uploadCapturedSelfie(String uid) {
        if (capturedBitmap == null) {
            _error.setValue("No photo captured to upload.");
            return;
        }
        _loading.setValue(true);
        repo.uploadSelfie(uid, capturedBitmap)
                .addOnSuccessListener(url -> {
                    _selfieUrl.setValue(url);
                    _loading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _loading.setValue(false);
                });
    }

    public void submitAttendance(String studentId, String studentName, Session session, float distanceMetres, float faceScore, String selfieUrl) {
        _loading.setValue(true);
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(studentId);
        record.setStudentName(studentName);
        record.setSessionId(session.getSessionId());
        record.setSubjectCode(session.getSubjectCode());
        record.setSubjectName(session.getSubjectName());
        record.setGeoVerified(true);
        record.setDistanceMetres(distanceMetres);
        record.setFaceVerified(true);
        record.setFaceMatchScore(faceScore);
        record.setSelfieUrl(selfieUrl);

        repo.submitAttendance(record)
                .addOnSuccessListener(result -> {
                    _loading.setValue(false);
                    _attendanceSubmitted.setValue(true);
                })
                .addOnFailureListener(e -> {
                    _loading.setValue(false);
                    _error.setValue(e.getMessage());
                });
    }

    private final MutableLiveData<List<AttendanceRecord>> _allAttendance = new MutableLiveData<>();
    public final LiveData<List<AttendanceRecord>> allAttendance = _allAttendance;

    private com.google.firebase.firestore.ListenerRegistration attendanceListener = null;

    public void loadAllAttendance() {
        if (attendanceListener != null) attendanceListener.remove();
        
        _loading.setValue(true);
        attendanceListener = repo.listenToAllAttendance(list -> {
            _allAttendance.setValue(list);
            _loading.setValue(false);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (attendanceListener != null) attendanceListener.remove();
    }

    private final MutableLiveData<List<SubjectAttendance>> _attendanceSummary = new MutableLiveData<>();
    public final LiveData<List<SubjectAttendance>> attendanceSummary = _attendanceSummary;

    private final MutableLiveData<List<AttendanceRecord>> _studentRecords = new MutableLiveData<>();
    public final LiveData<List<AttendanceRecord>> studentRecords = _studentRecords;

    public void loadStudentAttendanceSummary(String studentId) {
        _loading.setValue(true);
        subjectRepo.getStudentSubjects(studentId).addOnSuccessListener(subjects -> {
            if (subjects.isEmpty()) {
                _attendanceSummary.setValue(new ArrayList<>());
                _loading.setValue(false);
                return;
            }

            repo.getStudentAttendance(studentId).addOnSuccessListener(records -> {
                Map<String, Integer> presentMap = new HashMap<>();
                for (AttendanceRecord rec : records) {
                    if (rec.isGeoVerified() && rec.isFaceVerified()) {
                        presentMap.put(rec.getSubjectCode(), presentMap.getOrDefault(rec.getSubjectCode(), 0) + 1);
                    }
                }

                List<SubjectAttendance> summaries = new ArrayList<>();
                for (Subject s : subjects) {
                    int present = presentMap.getOrDefault(s.getCode(), 0);
                    int total = 30; // Placeholder total classes for demo
                    float percent = (present / (float) total) * 100f;
                    summaries.add(new SubjectAttendance(s.getCode(), s.getName(), present, total, percent));
                }
                _attendanceSummary.setValue(summaries);
                _loading.setValue(false);
            });
        }).addOnFailureListener(e -> {
            _error.setValue(e.getMessage());
            _loading.setValue(false);
        });
    }

    public void loadStudentAttendanceDetails(String studentId) {
        _loading.setValue(true);
        repo.getStudentAttendance(studentId).addOnSuccessListener(records -> {
            _studentRecords.setValue(records);
            _loading.setValue(false);
        }).addOnFailureListener(e -> {
            _error.setValue(e.getMessage());
            _loading.setValue(false);
        });
    }

    public void clearError() {
        _error.setValue(null);
    }
}
