package com.smartattend.app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.smartattend.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNav, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.studentDashboardFragment || id == R.id.scheduleFragment || 
                    id == R.id.geoVerifyFragment || id == R.id.reportsFragment) {
                    
                    if (binding.bottomNav.getMenu().findItem(R.id.studentDashboardFragment) == null) {
                        binding.bottomNav.getMenu().clear();
                        binding.bottomNav.inflateMenu(R.menu.bottom_nav_menu);
                    }
                    binding.bottomNav.setVisibility(View.VISIBLE);
                } else if (id == R.id.teacherDashboardFragment || id == R.id.teacherScheduleFragment || 
                           id == R.id.teacherReportsFragment) {
                    
                    if (binding.bottomNav.getMenu().findItem(R.id.teacherDashboardFragment) == null) {
                        binding.bottomNav.getMenu().clear();
                        binding.bottomNav.inflateMenu(R.menu.bottom_nav_teacher_menu);
                    }
                    binding.bottomNav.setVisibility(View.VISIBLE);
                } else if (id == R.id.profileFragment) {
                    binding.bottomNav.setVisibility(View.VISIBLE);
                } else {
                    binding.bottomNav.setVisibility(View.GONE);
                }
            });
        }


    }
}
