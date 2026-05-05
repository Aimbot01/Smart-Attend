package com.smartattend.app.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.smartattend.app.model.Subject;

import java.util.ArrayList;
import java.util.List;

public class SubjectRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<List<Subject>> getTeacherSubjects(String teacherId) {
        return db.collection("subjects")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .continueWith(task -> {
                    List<Subject> list = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult().getDocuments()) {
                            Subject s = doc.toObject(Subject.class);
                            if (s != null) list.add(s);
                        }
                    }
                    return list;
                });
    }

    public Task<List<Subject>> getStudentSubjects(String studentId) {
        return db.collection("subjects")
                .whereArrayContains("enrolledStudents", studentId)
                .get()
                .continueWith(task -> {
                    List<Subject> list = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult().getDocuments()) {
                            Subject s = doc.toObject(Subject.class);
                            if (s != null) list.add(s);
                        }
                    }
                    return list;
                });
    }

    public void seedSubjects(String teacherId, String teacherName) {
        List<Subject> list = new ArrayList<>();
        list.add(new Subject("OOP-CS2023", "OOP using Java", teacherId, teacherName, "Lab 005", "MON/WED", new java.util.ArrayList<>()));
        list.add(new Subject("DAA-CS2024", "DAA", teacherId, teacherName, "NB 106", "TUE/THU", new java.util.ArrayList<>()));
        list.add(new Subject("MAD-CS2025", "Mobile App Dev", teacherId, teacherName, "NB 102", "FRI", new java.util.ArrayList<>()));

        for (Subject s : list) {
            db.collection("subjects").document(s.getCode()).set(s);
        }
    }
}
