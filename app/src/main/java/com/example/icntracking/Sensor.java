package com.example.icntracking;

public class Sensor {
    private String id;
    private int iconResId;   // Icon resource to display (e.g., R.drawable.baseline_sensors_24)
    private int colorResId;  // Color resource for the icon (e.g., R.color.red)
    private float alpha;     // Icon transparency (0.0 = fully transparent, 1.0 = fully opaque)

    public Sensor(String id, int iconResId, int colorResId, float alpha) {
        this.id = id;
        this.iconResId = iconResId;
        this.colorResId = colorResId;
        this.alpha = alpha;
    }

    // Getters
    public String getId() {
        return id;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getColorResId() {
        return colorResId;
    }

    public float getAlpha() {
        return alpha;
    }

    // Setters
    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public void setColorResId(int colorResId) {
        this.colorResId = colorResId;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
