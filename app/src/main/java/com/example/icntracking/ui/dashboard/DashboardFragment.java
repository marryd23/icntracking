package com.example.icntracking.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.icntracking.R;
import com.example.icntracking.Sensor;  // ← Lägg till import för Sensor
import com.example.icntracking.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // 1) Hämta delad ViewModel
        DashboardViewModel viewModel =
                new ViewModelProvider(requireActivity())
                        .get(DashboardViewModel.class);

        // 2) Inflata host-layout
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 3) RESET-knappen: nollställ alla sensorer och ladda om barn-fragmentet
        binding.buttonReset.setOnClickListener(v -> {
            Sensor.resetAll();
            // Kolla vilket läge som är valt
            String mode = viewModel.getViewMode().getValue();
            Fragment child = "geofence".equals(mode)
                    ? new GeofenceDashboardFragment()
                    : new GridDashboardFragment();
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.replace(R.id.dashboardContent, child);
            ft.commit();
        });

        // 4) Observera viewMode och byt titel + fragment
        viewModel.getViewMode().observe(getViewLifecycleOwner(), mode -> {
            // A) Sätt rubriken dynamiskt
            String title = mode.substring(0,1).toUpperCase() + mode.substring(1);
            binding.textDashboard.setText(title);

            // B) Byt under-fragment
            Fragment child = "grid".equals(mode)
                    ? new GridDashboardFragment()
                    : new GeofenceDashboardFragment();
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.replace(R.id.dashboardContent, child);
            ft.commit();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
