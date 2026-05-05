package com.smartattend.app.ui.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartattend.app.R;
import com.smartattend.app.databinding.FragmentStudentDashboardBinding;
import com.smartattend.app.model.ScheduleItem;
import com.smartattend.app.utils.TimetableUtils;
import com.smartattend.app.viewmodel.AttendanceViewModel;
import com.smartattend.app.viewmodel.AuthViewModel;
import com.smartattend.app.viewmodel.TimetableViewModel;

import java.util.ArrayList;
import java.util.List;

public class StudentDashboardFragment extends Fragment {

    private FragmentStudentDashboardBinding binding;
    private AuthViewModel authViewModel;

    private static class SubjectItem {
        String name, detail, percent;
        SubjectItem(String name, String detail, String percent) {
            this.name = name; this.detail = detail; this.percent = percent;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private TimetableViewModel timetableViewModel;
    private AttendanceViewModel attendanceViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        timetableViewModel = new ViewModelProvider(requireActivity()).get(TimetableViewModel.class);
        attendanceViewModel = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);
        
        authViewModel.refreshUser();

        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvStudentName.setText(!user.getName().isEmpty() ? user.getName() : user.getEmail().split("@")[0]);
                // Fetch timetable for the batch (assuming CSE_IV_SEM_I for this user)
                timetableViewModel.loadTimetable("CSE_IV_SEM_I");
                attendanceViewModel.loadStudentAttendanceSummary(user.getUid());
            }
        });

        timetableViewModel.fullSchedule.observe(getViewLifecycleOwner(), schedule -> {
            setupSubjectList();
        });

        attendanceViewModel.attendanceSummary.observe(getViewLifecycleOwner(), summary -> {
            if (summary != null && !summary.isEmpty()) {
                float total = 0;
                int presentCount = 0;
                for (AttendanceViewModel.SubjectAttendance sa : summary) {
                    total += sa.percentage;
                    presentCount += sa.presentCount;
                }
                float avg = total / summary.size();
                binding.tvOverallPercent.setText(Math.round(avg) + "%");
                // Update stats row if needed (cardPresent, cardAbsent IDs exist in XML)
                // For now just the hero card
            }
            setupSubjectList();
        });

        binding.btnMarkAttendance.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_dashboard_to_verify)
        );
        
        binding.cardOverallProgress.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_dashboard_to_present)
        );
    }

    private void setupSubjectList() {
        String day = TimetableUtils.getCurrentDayString();
        List<ScheduleItem> daySchedule = timetableViewModel.getScheduleForDay(day);
        List<AttendanceViewModel.SubjectAttendance> attendanceList = attendanceViewModel.attendanceSummary.getValue();
        
        // If Firestore is still loading or empty, fallback to local for demo or show empty
        if (daySchedule.isEmpty() && Boolean.FALSE.equals(timetableViewModel.loading.getValue())) {
            daySchedule = TimetableUtils.getScheduleForDay(day);
        }

        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<ScheduleItem> finalDaySchedule = daySchedule;
        binding.rvSubjects.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
                return new RecyclerView.ViewHolder(v) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ScheduleItem item = finalDaySchedule.get(position);
                ((TextView) holder.itemView.findViewById(R.id.tvSubjectName)).setText(item.getSubject());
                ((TextView) holder.itemView.findViewById(R.id.tvSubjectDetail)).setText(item.getRoom() + " • " + item.getTime());
                
                String percentStr = "0%";
                if (attendanceList != null) {
                    for (AttendanceViewModel.SubjectAttendance sa : attendanceList) {
                        if (sa.subjectName.equalsIgnoreCase(item.getSubject()) || sa.subjectCode.equalsIgnoreCase(item.getSubject())) {
                            percentStr = Math.round(sa.percentage) + "%";
                            break;
                        }
                    }
                }
                ((TextView) holder.itemView.findViewById(R.id.tvPercentage)).setText(percentStr); 
            }

            @Override
            public int getItemCount() {
                return finalDaySchedule.size();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
