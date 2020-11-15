package com.paz.accesstolib;

public interface GrantListener {
    void onGranted(boolean allGranted);

    void onNotGranted(String[] permissions);

    void onNeverAskAgain(String[] permissions);
}
