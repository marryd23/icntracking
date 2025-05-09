package com.example.icntracking.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.icntracking.databinding.FragmentHomeBinding;
import com.example.icntracking.ui.dashboard.DashboardViewModel; // <-- lägg till

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // 1) Hämta HomeViewModel för texten (oförändrat)
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        // 2) Hämta DashboardViewModel (delad med DashboardFragment)
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(requireActivity())
                        .get(DashboardViewModel.class);

        // 3) Inflatera layouten via ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 4) Observera din gamla text
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // 5) Koppla knapparna till DashboardViewModel
        binding.buttonGrid.setOnClickListener(v ->
                dashboardViewModel.setViewMode("grid")
        );
        binding.buttonGeofence.setOnClickListener(v ->
                dashboardViewModel.setViewMode("geofence")
        );

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
