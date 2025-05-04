package com.example.icntracking.ui.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.icntracking.R;
import com.example.icntracking.Sensor;
import com.example.icntracking.databinding.FragmentDashboardBinding;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private final List<Sensor> sensorList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // 1) ViewModel + binding
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Hämta knappar
        Button mockButton  = root.findViewById(R.id.button_mock);
        Button resetButton = root.findViewById(R.id.button_reset);

        // Handler och Random för mock
        Handler handler = new Handler(Looper.getMainLooper());
        Random rand = new Random();

        // 2) Skapa sensorer
        sensorList.clear();
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 4; col++) {
                String sensorId = "sensor_" + row + "_" + col;
                Sensor sensor = new Sensor(
                        sensorId,
                        R.drawable.baseline_sensors_24,
                        0.5f,
                        R.drawable.baseline_remove_red_eye_24
                );
                sensorList.add(sensor);
            }
        }

        // 3) Uppdatera grid: ikon + klick‐listeners
        updateGrid(root);

        // Mockup
        mockButton.setOnClickListener(v -> {
            mockButton.setEnabled(false);
            resetButton.setEnabled(false);

            Sensor s12 = getSensorById("sensor_1_2");
            if (s12 != null) { s12.activate(); updateGrid(root); }

            int delay1 = rand.nextInt(2000) + 3000;
            handler.postDelayed(() -> {
                Sensor s22 = getSensorById("sensor_2_2");
                if (s22 != null) { s22.activate(); updateGrid(root); }

                int delay2 = rand.nextInt(2000) + 2000;
                handler.postDelayed(() -> {
                    Sensor s33 = getSensorById("sensor_3_3");
                    if (s33 != null) { s33.activate(); updateGrid(root); }
                    mockButton.setEnabled(true);
                    resetButton.setEnabled(true);
                }, delay2);
            }, delay1);
        });

        // Återställ-knapp
        resetButton.setOnClickListener(v -> {
            Sensor.resetAll();
            updateGrid(root);
        });

        // 4) Observer för Dashboard-rubriken
        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText()
                .observe(getViewLifecycleOwner(), textView::setText);

        // 5) Returnera rot-vyn
        return root;
    }

    /**
     * Uppdaterar alla celler utifrån sensorList
     */
    private void updateGrid(View root) {
        for (Sensor sensor : sensorList) {
            String cellId = sensor.getId().replace("sensor_", "cell_");
            int frameId = getResources().getIdentifier(
                    cellId, "id", requireContext().getPackageName());
            View cellFrame = root.findViewById(frameId);  // ändrad till View
            if (cellFrame == null) continue;

            ImageView sensorIv = cellFrame.findViewById(R.id.sensorIcon);
            ImageView eyeIv    = cellFrame.findViewById(R.id.eyeIcon);

            sensorIv.setImageResource(sensor.getSensorIconResId());
            sensorIv.setAlpha(sensor.getSensorAlpha());

            if (sensor.getEyeAlpha() > 0f) {
                eyeIv.setImageResource(sensor.getEyeIconResId());
                int tint = ContextCompat.getColor(requireContext(), sensor.getEyeTint());
                eyeIv.setColorFilter(tint);
                eyeIv.setAlpha(sensor.getEyeAlpha());
                eyeIv.setVisibility(View.VISIBLE);
            } else {
                eyeIv.setVisibility(View.GONE);
            }

            cellFrame.setOnClickListener(v -> {
                StringBuilder msg = new StringBuilder();
                msg.append("ID: ").append(sensor.getId()).append("\n");
                boolean trig = sensor.isTriggered();
                msg.append("Triggered: ").append(trig ? "Yes" : "No").append("\n");
                if (trig) {
                    String time = DateFormat.getDateTimeInstance()
                            .format(new Date(sensor.getLastActivationTime()));
                    msg.append("Tid: ").append(time);
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle("Sensor-information")
                        .setMessage(msg.toString())
                        .setPositiveButton("OK", null)
                        .show();
            });
        }
    }

    /**
     * Returnerar sensorn med detta id, eller null om den inte finns
     */
    private Sensor getSensorById(String id) {
        for (Sensor s : sensorList) {
            if (s.getId().equals(id)) return s;
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
