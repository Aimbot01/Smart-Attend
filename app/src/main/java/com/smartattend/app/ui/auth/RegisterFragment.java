package com.smartattend.app.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.tabs.TabLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import com.bumptech.glide.Glide;
import com.smartattend.app.R;
import com.smartattend.app.databinding.FragmentRegisterBinding;
import com.smartattend.app.repository.AttendanceRepository;
import com.smartattend.app.viewmodel.AuthViewModel;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;
    private String selectedRole = "student";
    private Bitmap faceBitmap;
    private Uri photoUri;
    private final AttendanceRepository attendanceRepo = new AttendanceRepository();

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && photoUri != null) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(
                                requireContext().getContentResolver().openInputStream(photoUri)
                        );
                        if (bitmap != null) {
                            faceBitmap = bitmap;
                            Glide.with(this).load(bitmap).circleCrop().into(binding.ivFace);
                        }
                    } catch (IOException e) {
                        binding.tvError.setText("Failed to load image");
                    }
                }
            }
    );

    private Uri createPhotoUri() {
        File imagesDir = new File(requireContext().getCacheDir(), "images");
        if (!imagesDir.exists()) imagesDir.mkdirs();
        File photoFile = new File(imagesDir, "enroll_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                photoFile
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.roleToggle.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedRole = (tab != null && tab.getPosition() == 0) ? "student" : "teacher";
                binding.tilStudentId.setHint(selectedRole.equals("student") ? "Student ID" : "Faculty ID");
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.btnCaptureFace.setOnClickListener(v -> {
            photoUri = createPhotoUri();
            cameraLauncher.launch(photoUri);
        });

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String studentId = binding.etStudentId.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                binding.tvError.setText("Please fill all fields");
                binding.tvError.setVisibility(View.VISIBLE);
                return;
            }

            if (selectedRole.equals("student") && faceBitmap == null) {
                binding.tvError.setText("Face enrollment is required for students");
                binding.tvError.setVisibility(View.VISIBLE);
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnRegister.setEnabled(false);

            if (faceBitmap != null) {
                // Upload to a generic folder first (or use email as ID)
                attendanceRepo.uploadSelfie("pending_" + email.replace(".", "_"), faceBitmap)
                        .addOnSuccessListener(url -> {
                            viewModel.register(name, email, password, selectedRole, studentId, "CSE", 1, url);
                        })
                        .addOnFailureListener(e -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnRegister.setEnabled(true);
                            binding.tvError.setText("Face upload failed: " + e.getMessage());
                            binding.tvError.setVisibility(View.VISIBLE);
                        });
            } else {
                viewModel.register(name, email, password, selectedRole, studentId, "CSE", 1, "");
            }
        });

        binding.tvLogin.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_register_to_login)
        );

        viewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
        });

        viewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            if (user.getRole().equals("teacher")) {
                NavHostFragment.findNavController(this).navigate(R.id.action_register_to_teacher);
            } else {
                NavHostFragment.findNavController(this).navigate(R.id.action_register_to_student);
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error == null) return;
            binding.tvError.setText(error);
            binding.tvError.setVisibility(View.VISIBLE);
            viewModel.clearError();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
