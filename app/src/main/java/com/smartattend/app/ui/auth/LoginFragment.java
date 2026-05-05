package com.smartattend.app.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.tabs.TabLayout;
import com.smartattend.app.R;
import com.smartattend.app.databinding.FragmentLoginBinding;
import com.smartattend.app.viewmodel.AuthViewModel;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;
    private String selectedRole = "student";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
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
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.btnLogin.setOnClickListener(v -> {
            String emailOrId = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (emailOrId.isEmpty() || password.isEmpty()) {
                binding.tvError.setText("Please fill all fields");
                binding.tvError.setVisibility(View.VISIBLE);
                return;
            }

            if (!emailOrId.contains("@")) {
                Toast.makeText(requireContext(), "Searching for ID: " + emailOrId + "...", Toast.LENGTH_SHORT).show();
            }

            viewModel.login(emailOrId, password);
        });

        binding.tvRegister.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register)
        );

        viewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
        });

        viewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            Toast.makeText(requireContext(), "Auth Success! Name: " + user.getName() + ", Role: [" + user.getRole() + "]", Toast.LENGTH_LONG).show();

            String role = user.getRole().trim().toLowerCase();
            boolean isFaculty = role.contains("teacher") || 
                                role.contains("faculty") || 
                                role.contains("prof") || 
                                role.contains("teach") ||
                                role.startsWith("t") ||
                                role.startsWith("f");

            if (isFaculty) {
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_teacher);
            } else {
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_student);
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
