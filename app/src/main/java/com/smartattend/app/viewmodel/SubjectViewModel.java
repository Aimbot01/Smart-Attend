package com.smartattend.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smartattend.app.model.Subject;
import com.smartattend.app.repository.SubjectRepository;

import java.util.List;

public class SubjectViewModel extends ViewModel {
    private final SubjectRepository repo = new SubjectRepository();

    private final MutableLiveData<List<Subject>> _subjects = new MutableLiveData<>();
    public final LiveData<List<Subject>> subjects = _subjects;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public final LiveData<Boolean> loading = _loading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    public void loadTeacherSubjects(String teacherId) {
        _loading.setValue(true);
        repo.getTeacherSubjects(teacherId)
                .addOnSuccessListener(list -> {
                    _subjects.setValue(list);
                    _loading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _loading.setValue(false);
                });
    }

    public void loadStudentSubjects(String studentId) {
        _loading.setValue(true);
        repo.getStudentSubjects(studentId)
                .addOnSuccessListener(list -> {
                    _subjects.setValue(list);
                    _loading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _loading.setValue(false);
                });
    }
}
