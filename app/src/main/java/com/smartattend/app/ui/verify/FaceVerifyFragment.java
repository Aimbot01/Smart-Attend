package com.smartattend.app.ui.verify;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.graphics.drawable.Drawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.smartattend.app.R;
import com.smartattend.app.databinding.FragmentFaceVerifyBinding;
import com.smartattend.app.model.Session;
import com.smartattend.app.model.User;
import com.smartattend.app.viewmodel.AttendanceViewModel;
import com.smartattend.app.viewmodel.AuthViewModel;
import com.smartattend.app.viewmodel.SessionViewModel;

public class FaceVerifyFragment extends Fragment {

    private FragmentFaceVerifyBinding binding;
    private AttendanceViewModel attendanceViewModel;
    private SessionViewModel sessionViewModel;
    private AuthViewModel authViewModel;
    private Bitmap selfieBitmap;
    private Bitmap referenceBitmap;

    private Uri photoUri;
    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && photoUri != null) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(
                                requireContext().getContentResolver().openInputStream(photoUri)
                        );
                        if (bitmap != null) {
                            selfieBitmap = bitmap;
                            Glide.with(this).load(bitmap).circleCrop().into(binding.ivSelfie);
                            binding.tvFaceStatus.setText("Selfie captured ✅ Ready to verify");
                            binding.btnVerify.setEnabled(true);
                            binding.tvError.setVisibility(View.GONE);
                        }
                    } catch (IOException e) {
                        Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private Uri createPhotoUri() {
        File imagesDir = new File(requireContext().getCacheDir(), "images");
        if (!imagesDir.exists()) imagesDir.mkdirs();
        File photoFile = new File(imagesDir, "selfie_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                photoFile
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFaceVerifyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attendanceViewModel = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);
        sessionViewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getFaceImageUrl() != null && !user.getFaceImageUrl().isEmpty()) {
                Glide.with(this).load(user.getFaceImageUrl()).into(binding.ivProfileReference);
                
                // Also download as bitmap for similarity check
                Glide.with(this).asBitmap().load(user.getFaceImageUrl()).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        referenceBitmap = resource;
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
            }
        });

        binding.btnCaptureCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                try {
                    photoUri = createPhotoUri();
                    cameraLauncher.launch(photoUri);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnDemoBypass.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Demo: Bypassing photo verification", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.action_face_to_success);
        });

        binding.btnVerify.setOnClickListener(v -> {
            if (selfieBitmap == null) return;
            String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            if (uid == null) return;

            binding.layoutChecks.setVisibility(View.VISIBLE);
            binding.progressFace.setVisibility(View.VISIBLE);
            binding.tvError.setVisibility(View.GONE);
            binding.btnVerify.setEnabled(false);

            if (Boolean.TRUE.equals(attendanceViewModel.faceDetected.getValue())) {
                // If face was already detected but upload failed, just retry upload
                attendanceViewModel.uploadCapturedSelfie(uid);
            } else {
                // Initial attempt: detect face + matching then upload
                attendanceViewModel.detectFace(selfieBitmap, uid, referenceBitmap);
            }
        });

        attendanceViewModel.faceDetected.observe(getViewLifecycleOwner(), detected -> {
            if (detected == null) return;
            binding.progressFace.setVisibility(View.GONE);
            if (detected) {
                binding.tvCheck1.setText("✅ Face detected");
                binding.tvCheck2.setText("✅ Liveness confirmed");
                binding.tvCheck3.setText("⏳ Uploading selfie...");
            } else {
                binding.tvCheck1.setText("❌ Face not detected");
                binding.btnVerify.setEnabled(true);
            }
        });

        attendanceViewModel.selfieUrl.observe(getViewLifecycleOwner(), url -> {
            if (url == null) return;
            binding.tvCheck3.setText("✅ Selfie uploaded");
            Session session = sessionViewModel.validatedSession.getValue();
            if (session == null) {
                Toast.makeText(requireContext(), "Demo: Bypassing server submit", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigate(R.id.action_face_to_success);
                return;
            }

            User user = authViewModel.user.getValue();
            if (user == null) return;
            Float score = attendanceViewModel.livenessScore.getValue();
            Float distance = attendanceViewModel.verifiedDistance.getValue();
            attendanceViewModel.submitAttendance(
                    user.getUid(),
                    user.getName(),
                    session,
                    distance != null ? distance : 0f,
                    score != null ? score : 0f,
                    url
            );
        });

        attendanceViewModel.attendanceSubmitted.observe(getViewLifecycleOwner(), submitted -> {
            if (Boolean.TRUE.equals(submitted)) {
                NavHostFragment.findNavController(this).navigate(R.id.action_face_to_success);
            }
        });

        attendanceViewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error == null) return;
            binding.tvError.setText(error);
            binding.tvError.setVisibility(View.VISIBLE);
            binding.btnVerify.setEnabled(true);
            attendanceViewModel.clearError();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
