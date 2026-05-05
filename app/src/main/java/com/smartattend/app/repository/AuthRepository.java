package com.smartattend.app.repository;

import android.util.Log;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.smartattend.app.model.User;

public class AuthRepository {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<User> login(String emailOrId, String password) {
        if (!emailOrId.contains("@")) {
            return db.collection("users")
                    .whereEqualTo("studentId", emailOrId)
                    .get()
                    .continueWithTask(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String email = task.getResult().getDocuments().get(0).getString("email");
                            if (email != null) {
                                return executeLogin(email, password);
                            } else {
                                return Tasks.forException(new Exception("Email field missing in database for this ID"));
                            }
                        } else {
                            return Tasks.forException(new Exception("ID '" + emailOrId + "' not found. Please ensure you are registered."));
                        }
                    });
        } else {
            return executeLogin(emailOrId, password);
        }
    }

    private Task<User> executeLogin(String email, String password) {
        Log.d("AuthRepository", "Attempting sign-in with email: " + email);
        return auth.signInWithEmailAndPassword(email, password).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult().getUser() == null) {
                return Tasks.forException(new Exception("Login failed", task.getException()));
            }
            String uid = task.getResult().getUser().getUid();
            return db.collection("users").document(uid).get(Source.SERVER).continueWith(docTask -> {
                if (docTask.isSuccessful() && docTask.getResult().exists()) {
                    return docTask.getResult().toObject(User.class);
                } else {
                    throw new Exception("User data not found");
                }
            });
        });
    }

    public Task<User> register(String name, String email, String password, String role, String studentId, String department, int semester, String faceImageUrl) {
        Log.d("AuthRepository", "Starting registration for: " + email);
        return auth.createUserWithEmailAndPassword(email, password).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult().getUser() == null) {
                Log.e("AuthRepository", "Auth creation failed", task.getException());
                return Tasks.forException(new Exception("Registration failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error")));
            }
            String uid = task.getResult().getUser().getUid();
            Log.d("AuthRepository", "Auth success, UID: " + uid);
            User user = new User(uid, name, email, role, studentId, department, semester, faceImageUrl, System.currentTimeMillis());
            return db.collection("users").document(uid).set(user).continueWith(setTask -> {
                if (setTask.isSuccessful()) {
                    Log.d("AuthRepository", "Firestore write success");
                    return user;
                } else {
                    Log.e("AuthRepository", "Firestore write failed", setTask.getException());
                    throw new Exception("Failed to save user data to Firestore");
                }
            });
        });
    }

    public Task<User> getCurrentUser() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forResult(null);
        }
        return db.collection("users").document(currentUser.getUid()).get(Source.SERVER).continueWith(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                return task.getResult().toObject(User.class);
            }
            return null;
        });
    }

    public void logout() {
        auth.signOut();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}
