package com.smartattend.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    public static final String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;
    public static final String LOCATION_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String STORAGE_PERMISSION = android.Manifest.permission.READ_MEDIA_IMAGES;

    public static boolean hasCamera(Context context) {
        return ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasLocation(Context context) {
        return ContextCompat.checkSelfPermission(context, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasStorage(Context context) {
        return ContextCompat.checkSelfPermission(context, STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }
}
