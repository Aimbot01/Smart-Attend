package com.smartattend.app.ui.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smartattend.app.R;
import com.smartattend.app.databinding.FragmentTeacherReportsBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.ViewModelProvider;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.viewmodel.AttendanceViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TeacherReportsFragment extends Fragment {

    private FragmentTeacherReportsBinding binding;
    private AttendanceViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        binding.rvReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        viewModel.allAttendance.observe(getViewLifecycleOwner(), records -> {
            if (records != null) {
                binding.rvReports.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_report, parent, false);
                        return new RecyclerView.ViewHolder(v) {};
                    }

                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                        AttendanceRecord record = records.get(position);
                        ((TextView) holder.itemView.findViewById(R.id.tvStudentName)).setText(record.getStudentName());
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
                        String dateStr = sdf.format(new Date(record.getTimestamp()));
                        ((TextView) holder.itemView.findViewById(R.id.tvStudentId)).setText(record.getSubjectCode() + " • " + dateStr);

                        TextView tvStatus = holder.itemView.findViewById(R.id.tvStatus);
                        boolean verified = record.isFaceVerified() && record.isGeoVerified();
                        tvStatus.setText(verified ? "Verified" : "Pending");

                        if (verified) {
                            tvStatus.setTextColor(Color.parseColor("#10B981"));
                            tvStatus.setBackgroundResource(R.drawable.bg_rounded_grey);
                        } else {
                            tvStatus.setTextColor(Color.parseColor("#EF4444"));
                            tvStatus.setBackgroundResource(R.drawable.bg_rounded_orange_light);
                        }
                    }

                    @Override
                    public int getItemCount() {
                        return records.size();
                    }
                });
            }
        });

        viewModel.loadAllAttendance();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
