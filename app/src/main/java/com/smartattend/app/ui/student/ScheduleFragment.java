package com.smartattend.app.ui.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;
import com.smartattend.app.R;
import com.smartattend.app.viewmodel.TimetableViewModel;

import com.google.android.material.tabs.TabLayout;
import com.smartattend.app.databinding.FragmentScheduleBinding;
import com.smartattend.app.model.ScheduleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.smartattend.app.utils.TimetableUtils;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;
    private TimetableViewModel timetableViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        timetableViewModel = new ViewModelProvider(requireActivity()).get(TimetableViewModel.class);
        
        binding.rvSchedule.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        
        binding.dayTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateList(tab.getText().toString());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        timetableViewModel.fullSchedule.observe(getViewLifecycleOwner(), schedule -> {
            updateList(binding.dayTabs.getTabAt(binding.dayTabs.getSelectedTabPosition()).getText().toString());
        });

        // Select current day by default
        String currentDay = TimetableUtils.getCurrentDayString();
        int tabIndex = getTabIndexForDay(currentDay);
        binding.dayTabs.getTabAt(tabIndex).select();
    }

    private int getTabIndexForDay(String day) {
        switch (day) {
            case "TUE": return 1;
            case "WED": return 2;
            case "THU": return 3;
            case "FRI": return 4;
            default: return 0;
        }
    }

    private void updateList(String day) {
        List<ScheduleItem> daySchedule = timetableViewModel.getScheduleForDay(day);
        if (daySchedule.isEmpty() && Boolean.FALSE.equals(timetableViewModel.loading.getValue())) {
            daySchedule = TimetableUtils.getScheduleForDay(day);
        }
        binding.rvSchedule.setAdapter(new ScheduleAdapter(daySchedule));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
