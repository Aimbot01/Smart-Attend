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
import com.smartattend.app.databinding.FragmentAttendanceListBinding;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.viewmodel.AttendanceViewModel;
import com.smartattend.app.viewmodel.AuthViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PresentDetailsFragment extends Fragment {

    private FragmentAttendanceListBinding binding;
    private AttendanceViewModel attendanceViewModel;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAttendanceListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvTitle.setText("Present Details");

        attendanceViewModel = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        binding.rvAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));

        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                attendanceViewModel.loadStudentAttendanceDetails(user.getUid());
            }
        });

        attendanceViewModel.studentRecords.observe(getViewLifecycleOwner(), records -> {
            List<AttendanceRecord> presentRecords = new ArrayList<>();
            for (AttendanceRecord r : records) {
                if (r.isGeoVerified() && r.isFaceVerified()) {
                    presentRecords.add(r);
                }
            }
            binding.rvAttendance.setAdapter(new AttendanceAdapter(presentRecords));
        });
    }

    private static class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
        private final List<AttendanceRecord> list;
        private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

        AttendanceAdapter(List<AttendanceRecord> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_date, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AttendanceRecord item = list.get(position);
            holder.tvDate.setText(sdf.format(new Date(item.getTimestamp())));
            holder.tvSubject.setText(item.getSubjectName());
            holder.tvStatus.setText("Present");
            holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvSubject, tvStatus;
            ViewHolder(View v) {
                super(v);
                tvDate = v.findViewById(R.id.tvDate);
                tvSubject = v.findViewById(R.id.tvSubject);
                tvStatus = v.findViewById(R.id.tvStatus);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
