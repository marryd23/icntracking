package com.example.icntracking.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.icntracking.R;
import com.example.icntracking.Sensor;
import com.example.icntracking.databinding.FragmentDashboardGridBinding;

import java.util.ArrayList;
import java.util.List;

public class GridDashboardFragment extends Fragment {
    private FragmentDashboardGridBinding binding;
    private final List<Sensor> sensorList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardGridBinding.inflate(inflater, container, false);

        // Skapa sensorer
        sensorList.clear();
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 4; col++) {
                sensorList.add(new Sensor(
                        "sensor_" + row + "_" + col,
                        R.drawable.baseline_sensors_24,
                        0.5f,
                        R.drawable.baseline_remove_red_eye_24
                ));
            }
        }
        // Rita ut rutnätet
        updateGrid(binding.getRoot());

        // OBS: reset‐knappen hanteras nu i host‐fragmentet (DashboardFragment)
        // därför tas denna bort från GridDashboardFragment

        return binding.getRoot();
    }

    private void updateGrid(View root) {
        for (Sensor sensor : sensorList) {
            String cellId = sensor.getId().replace("sensor_", "cell_");
            int frameId = getResources()
                    .getIdentifier(cellId, "id", requireContext().getPackageName());
            View cellFrame = root.findViewById(frameId);
            if (cellFrame == null) continue;

            ImageView sensorIv = cellFrame.findViewById(R.id.sensorIcon);
            ImageView eyeIv    = cellFrame.findViewById(R.id.eyeIcon);

            sensorIv.setImageResource(sensor.getSensorIconResId());
            sensorIv.setAlpha(sensor.getSensorAlpha());

            if (sensor.getEyeAlpha() > 0f) {
                eyeIv.setImageResource(sensor.getEyeIconResId());
                eyeIv.setAlpha(sensor.getEyeAlpha());
                eyeIv.setVisibility(View.VISIBLE);
            } else {
                eyeIv.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
