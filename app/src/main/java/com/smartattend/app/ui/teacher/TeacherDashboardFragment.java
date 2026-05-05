package com.smartattend.app.ui.teacher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.smartattend.app.R;
import com.smartattend.app.databinding.FragmentTeacherDashboardBinding;
import com.smartattend.app.viewmodel.AuthViewModel;
import com.smartattend.app.viewmodel.SessionViewModel;
import com.smartattend.app.viewmodel.SubjectViewModel;

import java.util.List;

public class TeacherDashboardFragment extends Fragment {

    private FragmentTeacherDashboardBinding binding;
    private AuthViewModel authViewModel;
    private SessionViewModel sessionViewModel;
    private SubjectViewModel subjectViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        sessionViewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
        subjectViewModel = new ViewModelProvider(requireActivity()).get(SubjectViewModel.class);
        
        authViewModel.refreshUser();

        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String displayName = !user.getName().trim().isEmpty() ? user.getName() : user.getEmail().split("@")[0];
                binding.tvTeacherName.setText(displayName);
                subjectViewModel.loadTeacherSubjects(user.getUid());
                
                // Temporary: Seed subjects if the list is empty (Wait for load first)
                subjectViewModel.subjects.observe(getViewLifecycleOwner(), list -> {
                    if (list != null && list.isEmpty()) {
                        new com.smartattend.app.repository.SubjectRepository().seedSubjects(user.getUid(), displayName);
                        subjectViewModel.loadTeacherSubjects(user.getUid()); // Reload
                    }
                });
            }
        });

        binding.cardStartSession.setOnClickListener(v -> showSubjectPicker());

        binding.cardSchedule.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_teacher_to_schedule)
        );

        binding.cardReports.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_teacher_to_reports)
        );
    }

    private void showSubjectPicker() {
        if (subjectViewModel.subjects.getValue() == null || subjectViewModel.subjects.getValue().isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "No subjects found. Please add subjects first.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        List<com.smartattend.app.model.Subject> list = subjectViewModel.subjects.getValue();
        String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++) names[i] = list.get(i).getName() + " (" + list.get(i).getCode() + ")";

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Subject")
                .setItems(names, (dialog, which) -> {
                    com.smartattend.app.model.Subject selected = list.get(which);
                    Bundle args = new Bundle();
                    args.putString("subjectCode", selected.getCode());
                    args.putString("subjectName", selected.getName());
                    args.putString("room", selected.getRoom());
                    NavHostFragment.findNavController(this).navigate(R.id.action_teacher_to_session, args);
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
