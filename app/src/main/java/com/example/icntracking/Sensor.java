package com.example.icntracking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a sensor with a base icon and an "eye" icon
 * that changes appearance based on activation ordering,
 * and records timestamps.
 */
public class Sensor {
    private String id;

    // Bas-ikon och dess transparens (aldrig förändrad)
    private int sensorIconResId;
    private float sensorAlpha;

    // "Öga"-ikon och dess utseende
    private int eyeIconResId;
    private int eyeTint;    // färg för "eye"
    private float eyeAlpha; // transparens för "eye"

    // Aktiveringsstatus
    private boolean triggered;
    private long lastActivationTime;

    // Statisk lista för att hålla ordning på aktiverade sensorer
    private static final List<Sensor> triggeredSensors = new ArrayList<>();

    public Sensor(String id,
                  int sensorIconResId,
                  float sensorAlpha,
                  int eyeIconResId) {
        this.id = id;
        this.sensorIconResId = sensorIconResId;
        this.sensorAlpha = sensorAlpha;
        this.eyeIconResId = eyeIconResId;
        this.triggered = false;
        this.lastActivationTime = 0;
        this.eyeTint = 0;   // inga värden tills aktivering
        this.eyeAlpha = 0f; // fullt transparent
    }

    /**
     * Aktiverar sensorn: sätter timestamp och uppdaterar
     * ordering och utseende för "eye"-ikonen på alla sensorer.
     */
    public void activate() {
        long now = System.currentTimeMillis();
        this.triggered = true;
        this.lastActivationTime = now;

        // Lägg till om inte redan i listan
        if (!triggeredSensors.contains(this)) {
            triggeredSensors.add(this);
        }
        // Sortera efter senaste först
        Collections.sort(triggeredSensors, new Comparator<Sensor>() {
            @Override
            public int compare(Sensor s1, Sensor s2) {
                // högre timestamp först
                return Long.compare(s2.lastActivationTime, s1.lastActivationTime);
            }
        });

        // Uppdatera utseende för ögon-ikonen baserat på position
        for (int i = 0; i < triggeredSensors.size(); i++) {
            Sensor s = triggeredSensors.get(i);
            if (i == 0) {
                // senaste
                s.eyeTint = android.R.color.holo_red_dark;
                s.eyeAlpha = 1.0f;
            } else if (i == 1) {
                // näst senaste
                s.eyeTint = android.R.color.holo_orange_light;
                s.eyeAlpha = 1.0f;
            } else if (i == 2) {
                // tredje senaste
                s.eyeTint = android.R.color.holo_orange_light;
                s.eyeAlpha = 0.5f;
            } else {
                // övriga; göm dem
                s.eyeAlpha = 0f;
            }
        }
    }

    // Getters för binding i UI
    public String getId() {
        return id;
    }

    public int getSensorIconResId() {
        return sensorIconResId;
    }

    public float getSensorAlpha() {
        return sensorAlpha;
    }

    public int getEyeIconResId() {
        return eyeIconResId;
    }

    public int getEyeTint() {
        return eyeTint;
    }

    public float getEyeAlpha() {
        return eyeAlpha;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public long getLastActivationTime() {
        return lastActivationTime;
    }

    /**
     * För test eller reset: återställ all aktivering
     */
    public static void resetAll() {
        for (Sensor s : new ArrayList<>(triggeredSensors)) {
            s.triggered = false;
            s.lastActivationTime = 0;
            s.eyeAlpha = 0f;
            triggeredSensors.clear();
        }
    }
}
