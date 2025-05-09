package com.example.icntracking.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DashboardViewModel extends ViewModel {
    // 1) En MutableLiveData som håller "grid" eller "geofence"
    private final MutableLiveData<String> viewMode = new MutableLiveData<>("grid");

    // 2) Publik getter
    public LiveData<String> getViewMode() {
        return viewMode;
    }

    // 3) Publik setter
    public void setViewMode(String mode) {
        viewMode.setValue(mode);
    }
}
