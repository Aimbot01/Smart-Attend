package com.smartattend.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smartattend.app.model.ScheduleItem;
import com.smartattend.app.repository.TimetableRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TimetableViewModel extends ViewModel {

    private final TimetableRepository repo = new TimetableRepository();
    
    private final MutableLiveData<List<ScheduleItem>> _fullSchedule = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<ScheduleItem>> fullSchedule = _fullSchedule;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public final LiveData<Boolean> loading = _loading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    public void loadTimetable(String batchId) {
        _loading.setValue(true);
        repo.getTimetable(batchId)
                .addOnSuccessListener(list -> {
                    _fullSchedule.setValue(list);
                    _loading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue(e.getMessage());
                    _loading.setValue(false);
                });
    }

    public List<ScheduleItem> getScheduleForDay(String day) {
        List<ScheduleItem> current = _fullSchedule.getValue();
        if (current == null) return new ArrayList<>();
        return current.stream()
                .filter(item -> item.getDay().equalsIgnoreCase(day))
                .collect(Collectors.toList());
    }
}
