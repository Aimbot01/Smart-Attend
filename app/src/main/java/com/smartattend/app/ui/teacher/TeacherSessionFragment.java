package com.smartattend.app.ui.teacher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.smartattend.app.databinding.FragmentTeacherSessionBinding;
import com.smartattend.app.model.AttendanceRecord;
import com.smartattend.app.utils.QRGenerator;
import com.smartattend.app.viewmodel.AuthViewModel;
import com.smartattend.app.viewmodel.SessionViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeacherSessionFragment extends Fragment {

    private FragmentTeacherSessionBinding binding;
    private SessionViewModel sessionViewModel;
    private AuthViewModel authViewModel;

    private final ActivityResultLauncher<String> locationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    startSession();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTeacherSessionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionViewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startSession();
        } else {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        sessionViewModel.qrToken.observe(getViewLifecycleOwner(), token -> {
            if (token != null) {
                binding.ivQrCode.setImageBitmap(QRGenerator.generate(token));
            }
        });

        sessionViewModel.timerSeconds.observe(getViewLifecycleOwner(), secs -> {
            if (secs != null) {
                int m = secs / 60;
                int s = secs % 60;
                binding.tvTimer.setText(String.format(Locale.getDefault(), "Refreshes in: %d:%02d", m, s));
            }
        });

        binding.btnRefreshQR.setOnClickListener(v -> {
            // trigger refresh - handled in viewmodel timer normally
        });

        binding.rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        sessionViewModel.presentStudents.observe(getViewLifecycleOwner(), students -> {
            List<AttendanceRecord> displayStudents = students;
            if (displayStudents == null || displayStudents.isEmpty()) {
                displayStudents = new ArrayList<>();
                AttendanceRecord r1 = new AttendanceRecord();
                r1.setStudentName("Aman Sharma");
                r1.setStudentId("BML-2024-CSE-012");
                r1.setFaceMatchScore(0.98f);
                r1.setTimestamp(System.currentTimeMillis());
                displayStudents.add(r1);

                AttendanceRecord r2 = new AttendanceRecord();
                r2.setStudentName("Priya Gupta");
                r2.setStudentId("BML-2024-CSE-045");
                r2.setFaceMatchScore(0.95f);
                r2.setTimestamp(System.currentTimeMillis() - 60000);
                displayStudents.add(r2);

                binding.tvVerifiedCount.setText("2 (Demo)");
                binding.tvTotalCountLarge.setText("Total Verified: 2 (Demo)");
                binding.tvLastStudent.setText("Last: Priya Gupta");
                binding.tvLastStudent.setTextColor(Color.parseColor("#22C55E"));
            } else {
                binding.tvVerifiedCount.setText(String.valueOf(students.size()));
                binding.tvTotalCountLarge.setText("Total Verified: " + students.size());

                AttendanceRecord lastStudent = students.get(students.size() - 1);
                binding.tvLastStudent.setText("Last: " + lastStudent.getStudentName());
                binding.tvLastStudent.setTextColor(Color.parseColor("#22C55E"));
            }

            binding.rvStudents.setAdapter(new VerifiedStudentAdapter(displayStudents));
        });

        binding.btnEndSession.setOnClickListener(v -> {
            sessionViewModel.endSession();
            NavHostFragment.findNavController(this).popBackStack();
        });
    }

    @SuppressLint("MissingPermission")
    private void startSession() {
        LocationServices.getFusedLocationProviderClient(requireContext())
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) return;
                    String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
                    String name = authViewModel.user.getValue() != null ? authViewModel.user.getValue().getName() : "Dr. Nikhil Kumar";
                    
                    String subjectCode = getArguments() != null ? getArguments().getString("subjectCode", "OOP-CS2023") : "OOP-CS2023";
                    String subjectName = getArguments() != null ? getArguments().getString("subjectName", "OOP using Java") : "OOP using Java";
                    String room = getArguments() != null ? getArguments().getString("room", "Lab 005") : "Lab 005";

                    sessionViewModel.startSession(
                            subjectCode,
                            subjectName,
                            uid,
                            name,
                            location,
                            room
                    );
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
