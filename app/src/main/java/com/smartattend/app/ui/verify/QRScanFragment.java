package com.smartattend.app.ui.verify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.smartattend.app.R;
import com.smartattend.app.databinding.FragmentQrScanBinding;
import com.smartattend.app.viewmodel.SessionViewModel;

import java.util.concurrent.ExecutionException;

public class QRScanFragment extends Fragment {

    private FragmentQrScanBinding binding;
    private SessionViewModel sessionViewModel;
    private boolean scannedOnce = false;

    private final ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) startCamera();
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQrScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionViewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);

        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 600);
        animation.setDuration(2000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        binding.scanLine.startAnimation(animation);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA);
        }

        sessionViewModel.validatedSession.observe(getViewLifecycleOwner(), session -> {
            if (session != null) {
                NavHostFragment.findNavController(this).navigate(R.id.action_qr_to_geo);
            }
        });

        sessionViewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error == null) return;
            binding.tvScanStatus.setText("❌ " + error);
            binding.tvScanStatus.setTextColor(0xFFEF4444);
            binding.progressScan.setVisibility(View.GONE);
            scannedOnce = false;
        });

        binding.btnSkipDemo.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Warning: Not scanned (Demo mode)", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.action_qr_to_geo);
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), imageProxy -> {
                    if (!scannedOnce) {
                        processImage(imageProxy);
                    } else {
                        imageProxy.close();
                    }
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                binding.tvScanStatus.setText("Camera error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImage(ImageProxy proxy) {
        if (proxy.getImage() == null) {
            proxy.close();
            return;
        }
        InputImage image = InputImage.fromMediaImage(proxy.getImage(), proxy.getImageInfo().getRotationDegrees());
        BarcodeScanner scanner = BarcodeScanning.getClient();
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty()) {
                        String token = barcodes.get(0).getRawValue();
                        if (token != null) {
                            scannedOnce = true;
                            binding.progressScan.setVisibility(View.VISIBLE);
                            binding.tvScanStatus.setText("Verifying code with server...");
                            binding.tvScanStatus.setTextColor(0xFFFFFFFF);
                            sessionViewModel.validateQR(token);
                        }
                    }
                })
                .addOnCompleteListener(task -> proxy.close());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
