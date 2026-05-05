package com.smartattend.app.ui.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.smartattend.app.R;
import com.smartattend.app.databinding.FragmentProfileBinding;
import com.smartattend.app.viewmodel.AuthViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        authViewModel.refreshUser();

        authViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String displayName = !user.getName().trim().isEmpty() ? user.getName() : user.getEmail().split("@")[0];
                binding.tvProfileName.setText(displayName);
                binding.etFullName.setText(user.getName());
                binding.etEmail.setText(user.getEmail());

                if (user.getRole().equalsIgnoreCase("teacher") || user.getRole().equalsIgnoreCase("faculty")) {
                    binding.tvProfileId.setText("Faculty ID: " + user.getStudentId());
                } else {
                    binding.tvProfileId.setText("Student ID: " + user.getStudentId());
                }
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            authViewModel.logout();
            NavHostFragment.findNavController(this).navigate(
                    R.id.loginFragment,
                    null,
                    new NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
            );
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
