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
import com.smartattend.app.databinding.FragmentTeacherScheduleBinding;

import java.util.ArrayList;
import java.util.List;

public class TeacherScheduleFragment extends Fragment {

    private FragmentTeacherScheduleBinding binding;

    private static class SessionItem {
        String name, detail, status;
        SessionItem(String name, String detail, String status) {
            this.name = name; this.detail = detail; this.status = status;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        List<SessionItem> sessions = new ArrayList<>();
        sessions.add(new SessionItem("Data Structures", "Room 201 · 09:00 AM - 10:30 AM", "Completed"));
        sessions.add(new SessionItem("OOP using Java", "Lab 005 · 11:00 AM - 12:30 PM", "Live Now"));
        sessions.add(new SessionItem("Operating Systems", "Lab 002 · 01:00 PM - 02:30 PM", "Upcoming"));
        sessions.add(new SessionItem("Algorithms", "Room 101 · 03:00 PM - 04:30 PM", "Upcoming"));

        binding.rvTeacherSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTeacherSchedule.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
                return new RecyclerView.ViewHolder(v) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                SessionItem session = sessions.get(position);
                ((TextView) holder.itemView.findViewById(R.id.tvSubjectName)).setText(session.name);
                ((TextView) holder.itemView.findViewById(R.id.tvSubjectDetail)).setText(session.detail);

                TextView tvStatus = holder.itemView.findViewById(R.id.tvPercentage);
                tvStatus.setText(session.status);

                if (session.status.equals("Live Now")) {
                    tvStatus.setTextColor(Color.parseColor("#FF6B35"));
                } else if (session.status.equals("Completed")) {
                    tvStatus.setTextColor(Color.parseColor("#10B981"));
                } else {
                    tvStatus.setTextColor(Color.parseColor("#6B7280"));
                }
            }

            @Override
            public int getItemCount() {
                return sessions.size();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
