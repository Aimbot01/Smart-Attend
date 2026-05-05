package com.smartattend.app.ui.verify;

import android.Manifest;
import android.content.pm.PackageManager;
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

import com.smartattend.app.R;
import com.smartattend.app.databinding.FragmentGeoVerifyBinding;
import com.smartattend.app.model.Session;
import com.smartattend.app.utils.GeoVerifier;
import com.smartattend.app.viewmodel.AttendanceViewModel;
import com.smartattend.app.viewmodel.SessionViewModel;

import java.util.Locale;

public class GeoVerifyFragment extends Fragment {

    private FragmentGeoVerifyBinding binding;
    private SessionViewModel sessionViewModel;
    private AttendanceViewModel attendanceViewModel;

    private float verifiedDistance = 0f;

    private final ActivityResultLauncher<String> locationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    startGeoCheck();
                } else {
                    binding.tvError.setText("Location permission denied");
                    binding.tvError.setVisibility(View.VISIBLE);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGeoVerifyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionViewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
        attendanceViewModel = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startGeoCheck();
        } else {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        binding.btnProceed.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_geo_to_face)
        );

        binding.btnDemoBypass.setOnClickListener(v -> {
            android.widget.Toast.makeText(requireContext(), "Demo: Bypassing location check", android.widget.Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.action_geo_to_face);
        });
    }

    private void startGeoCheck() {
        Session session = sessionViewModel.validatedSession.getValue();
        if (session == null) {
            binding.tvError.setText("Session data missing. Please scan QR again.");
            binding.tvError.setVisibility(View.VISIBLE);
            return;
        }

        binding.progressGeo.setVisibility(View.VISIBLE);
        binding.tvGeoStatus.setText("Checking your location...");

        new GeoVerifier(requireContext()).verify(session.getLatitude(), session.getLongitude(), result -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    binding.progressGeo.setVisibility(View.GONE);

                    if (result.error != null) {
                        binding.tvError.setText(result.error);
                        binding.tvError.setVisibility(View.VISIBLE);
                        return;
                    }

                    float dist = result.distanceMetres;
                    verifiedDistance = dist;
                    attendanceViewModel.setVerifiedDistance(dist);
                    binding.tvDistance.setText(String.format(Locale.getDefault(), "%.1fm", dist));

                    if (dist <= 10.0f) {
                        binding.tvDistance.setTextColor(0xFF22C55E);
                        binding.tvGeoStatus.setText("✅ Within range! You can proceed.");
                        binding.tvGeoStatus.setTextColor(0xFF15803D);
                    } else {
                        binding.tvDistance.setTextColor(0xFFEF4444);
                        binding.tvGeoStatus.setText("⚠️ Too far (10m required). Please move closer.");
                        binding.tvGeoStatus.setTextColor(0xFFB91C1C);
                    }
                    binding.btnProceed.setEnabled(true);
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
