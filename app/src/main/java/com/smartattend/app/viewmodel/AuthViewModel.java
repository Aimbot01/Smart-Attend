package com.smartattend.app.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smartattend.app.model.User;
import com.smartattend.app.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repo = new AuthRepository();

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    public final LiveData<User> user = _user;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public final LiveData<Boolean> loading = _loading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    public AuthViewModel() {
        if (isLoggedIn()) {
            refreshUser();
        }
    }

    public void refreshUser() {
        try {
            repo.getCurrentUser().addOnSuccessListener(userData -> {
                if (userData != null) {
                    Log.d("AuthViewModel", "refreshUser result: name=" + userData.getName() + ", role=" + userData.getRole());
                    _user.setValue(userData);
                }
            }).addOnFailureListener(e -> {
                Log.e("AuthViewModel", "refreshUser error", e);
            });
        } catch (Exception e) {
            Log.e("AuthViewModel", "refreshUser error", e);
        }
    }

    public void login(String email, String password) {
        _loading.setValue(true);
        repo.login(email, password)
                .addOnSuccessListener(result -> {
                    _loading.setValue(false);
                    _user.setValue(result);
                })
                .addOnFailureListener(e -> {
                    _loading.setValue(false);
                    _error.setValue(e.getMessage());
                });
    }

    public void register(String name, String email, String password, String role, String studentId, String department, int semester, String faceImageUrl) {
        _loading.setValue(true);
        repo.register(name, email, password, role, studentId, department, semester, faceImageUrl)
                .addOnSuccessListener(result -> {
                    _loading.setValue(false);
                    _user.setValue(result);
                })
                .addOnFailureListener(e -> {
                    _loading.setValue(false);
                    _error.setValue(e.getMessage());
                });
    }

    public void logout() {
        repo.logout();
        _user.setValue(null);
    }

    public boolean isLoggedIn() {
        return repo.isLoggedIn();
    }

    public void clearError() {
        _error.setValue(null);
    }
}
