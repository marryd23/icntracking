package com.example.icntracking.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.icntracking.R;
import com.example.icntracking.Sensor;
import com.example.icntracking.databinding.FragmentDashboardBinding;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private final List<Sensor> sensorList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Set up ViewModel and view binding
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Step 1: Create 16 sensors (sensor_1_1 to sensor_4_4)
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 4; col++) {
                String sensorId = "sensor_" + row + "_" + col;
                Sensor sensor = new Sensor(
                        sensorId,
                        R.drawable.baseline_sensors_24, // default icon
                        R.color.black,                  // default color
                        1.0f                            // default transparency
                );
                sensorList.add(sensor);
            }
        }

        // Step 2: Update corresponding grid cells in layout
        for (Sensor sensor : sensorList) {
            // Convert sensor_1_1 → cell_1_1 to match layout IDs
            String cellId = sensor.getId().replace("sensor_", "cell_");

            // Get the view ID from resources
            int viewId = getResources().getIdentifier(cellId, "id", requireContext().getPackageName());

            // Find the TextView by ID
            TextView cellView = root.findViewById(viewId);

            if (cellView != null) {
                // Make sure no text interferes with icon positioning
                cellView.setText(null); // Better than ""

                // Keep grid border background
                cellView.setBackgroundResource(R.drawable.cell_border);

                // Set icon centered above (top drawable)
                cellView.setCompoundDrawablesWithIntrinsicBounds(0, sensor.getIconResId(), 0, 0);

                // Set transparency and color
                cellView.setAlpha(sensor.getAlpha());
                cellView.setTextColor(ContextCompat.getColor(requireContext(), sensor.getColorResId()));
            }
        }


        // Set text in top label from ViewModel
        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
