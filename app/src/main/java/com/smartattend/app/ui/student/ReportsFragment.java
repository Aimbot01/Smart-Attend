package com.smartattend.app.ui.student;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartattend.app.R;
import com.smartattend.app.databinding.FragmentReportsBinding;
import com.smartattend.app.viewmodel.AttendanceViewModel;
import com.smartattend.app.viewmodel.AuthViewModel;

import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {
    
    private FragmentReportsBinding binding;
    private AttendanceViewModel attendanceViewModel;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attendanceViewModel = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        binding.rvSubjectBreakdown.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                attendanceViewModel.loadStudentAttendanceSummary(user.getUid());
            }
        });

        attendanceViewModel.attendanceSummary.observe(getViewLifecycleOwner(), summaries -> {
            updateOverallUI(summaries);
            binding.rvSubjectBreakdown.setAdapter(new SubjectAdapter(summaries));
        });
    }

    private void updateOverallUI(List<AttendanceViewModel.SubjectAttendance> summaries) {
        if (summaries.isEmpty()) {
            binding.tvOverallPercentage.setText("0%");
            binding.tvStatusMessage.setText("No attendance records found.");
            return;
        }

        float totalPercent = 0;
        for (AttendanceViewModel.SubjectAttendance s : summaries) {
            totalPercent += s.percentage;
        }
        float avg = totalPercent / summaries.size();
        binding.tvOverallPercentage.setText(Math.round(avg) + "%");
        
        if (avg >= 75) {
            binding.tvStatusMessage.setText("Excellent standing! Keep it up.");
        } else if (avg >= 60) {
            binding.tvStatusMessage.setText("Good, but try to improve.");
        } else {
            binding.tvStatusMessage.setText("Attendance low! Be careful.");
        }
    }

    private static class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {
        private final List<AttendanceViewModel.SubjectAttendance> list;
        SubjectAdapter(List<AttendanceViewModel.SubjectAttendance> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AttendanceViewModel.SubjectAttendance item = list.get(position);
            holder.tvName.setText(item.subjectName);
            holder.tvDetail.setText(item.presentCount + "/" + item.totalCount + " Classes");
            holder.tvPercent.setText(Math.round(item.percentage) + "%");
            
            if (item.percentage >= 75) holder.tvPercent.setTextColor(Color.parseColor("#10B981"));
            else if (item.percentage >= 60) holder.tvPercent.setTextColor(Color.parseColor("#F59E0B"));
            else holder.tvPercent.setTextColor(Color.parseColor("#EF4444"));
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetail, tvPercent;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvSubjectName);
                tvDetail = v.findViewById(R.id.tvSubjectDetail);
                tvPercent = v.findViewById(R.id.tvPercentage);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
