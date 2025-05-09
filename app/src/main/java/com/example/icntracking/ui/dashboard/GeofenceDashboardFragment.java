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
import com.example.icntracking.databinding.FragmentDashboardGeofenceBinding;

import java.util.ArrayList;
import java.util.List;

public class GeofenceDashboardFragment extends Fragment {
    private FragmentDashboardGeofenceBinding binding;
    private final List<Sensor> sensorList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardGeofenceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Skapa 7 sensorer i kolumn 3
        sensorList.clear();
        for (int row = 1; row <= 7; row++) {
            String sensorId = "sensor_" + row + "_3";
            Sensor sensor = new Sensor(
                    sensorId,
                    R.drawable.baseline_sensors_24,       // sensor-ikon
                    0.5f,
                    R.drawable.baseline_remove_red_eye_24 // triggad-ikon
            );
            sensorList.add(sensor);
        }

        // Rita initialt upp
        updateGeofence(root);
        return root;
    }

    private void updateGeofence(View root) {
        for (Sensor sensor : sensorList) {
            // 1) Rita sensor-ikon i kolumn 3
            String cellSensorId = sensor.getId().replace("sensor_", "cell_");
            int sensorFrame = getResources()
                    .getIdentifier(cellSensorId, "id", requireContext().getPackageName());
            View sensorCell = root.findViewById(sensorFrame);
            if (sensorCell != null) {
                ImageView iv = sensorCell.findViewById(R.id.sensorIcon);
                iv.setImageResource(sensor.getSensorIconResId());
                iv.setAlpha(sensor.getSensorAlpha());
            }

            // 2) Om triggad, visa red_eye i kolumn 1
            if (sensor.isTriggered()) {
                String cellEyeId = cellSensorId.replace("_3", "_1");
                int eyeFrame = getResources()
                        .getIdentifier(cellEyeId, "id", requireContext().getPackageName());
                View eyeCell = root.findViewById(eyeFrame);
                if (eyeCell != null) {
                    ImageView eyeIv = eyeCell.findViewById(R.id.sensorIcon);
                    eyeIv.setVisibility(View.VISIBLE);
                    eyeIv.setImageResource(R.drawable.baseline_remove_red_eye_24);
                    eyeIv.setAlpha(1f);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
