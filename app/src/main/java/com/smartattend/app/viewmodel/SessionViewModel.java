package com.smartattend.app.viewmodel;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.model.Session;
import com.smartattend.app.repository.SessionRepository;

import java.util.List;

public class SessionViewModel extends ViewModel {

    private final SessionRepository repo = new SessionRepository();

    private final MutableLiveData<Session> _session = new MutableLiveData<>();
    public final LiveData<Session> session = _session;

    private final MutableLiveData<String> _qrToken = new MutableLiveData<>();
    public final LiveData<String> qrToken = _qrToken;

    private final MutableLiveData<List<AttendanceRecord>> _presentStudents = new MutableLiveData<>();
    public final LiveData<List<AttendanceRecord>> presentStudents = _presentStudents;

    private final MutableLiveData<Integer> _timerSeconds = new MutableLiveData<>();
    public final LiveData<Integer> timerSeconds = _timerSeconds;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<Session> _validatedSession = new MutableLiveData<>();
    public final LiveData<Session> validatedSession = _validatedSession;

    private ListenerRegistration listenerReg = null;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private int currentSeconds = 300;

    public void startSession(String subjectCode, String subjectName, String teacherId, String teacherName, Location location, String room) {
        repo.createSession(subjectCode, subjectName, teacherId, teacherName, location.getLatitude(), location.getLongitude(), room)
                .addOnSuccessListener(result -> {
                    _session.setValue(result);
                    _qrToken.setValue(result.getQrToken());
                    startTimer(result.getSessionId());
                    startListeningToStudents(result.getSessionId());
                })
                .addOnFailureListener(e -> _error.setValue(e.getMessage()));
    }

    private void startTimer(String sessionId) {
        currentSeconds = 300;
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentSeconds > 0) {
                    _timerSeconds.setValue(currentSeconds);
                    currentSeconds--;
                    handler.postDelayed(this, 1000);
                } else {
                    repo.refreshToken(sessionId)
                            .addOnSuccessListener(newToken -> {
                                _qrToken.setValue(newToken);
                                startTimer(sessionId);
                            });
                }
            }
        };
        handler.post(timerRunnable);
    }

    private void startListeningToStudents(String sessionId) {
        listenerReg = repo.listenToAttendance(sessionId, list -> _presentStudents.setValue(list));
    }

    public void endSession() {
        Session current = _session.getValue();
        if (current == null) return;
        repo.endSession(current.getSessionId());
        if (listenerReg != null) {
            listenerReg.remove();
        }
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
        _session.setValue(null);
    }

    public void validateQR(String scannedToken) {
        repo.validateToken(scannedToken)
                .addOnSuccessListener(_validatedSession::setValue)
                .addOnFailureListener(e -> _error.setValue(e.getMessage()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerReg != null) {
            listenerReg.remove();
        }
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }
}
