package com.smartattend.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class GeoVerifier {

    private final FusedLocationProviderClient fusedClient;

    public GeoVerifier(Context context) {
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public static class GeoResult {
        public final boolean withinRange;
        public final float distanceMetres;
        public final String error;

        public GeoResult(boolean withinRange, float distanceMetres) {
            this(withinRange, distanceMetres, null);
        }

        public GeoResult(boolean withinRange, float distanceMetres, String error) {
            this.withinRange = withinRange;
            this.distanceMetres = distanceMetres;
            this.error = error;
        }
    }

    public interface OnGeoResultListener {
        void onResult(GeoResult result);
    }

    @SuppressLint("MissingPermission")
    public void verify(double sessionLat, double sessionLng, int radiusMetres, OnGeoResultListener listener) {
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        listener.onResult(new GeoResult(false, -1f, "Could not get your location. Enable GPS."));
                        return;
                    }
                    float[] results = new float[1];
                    Location.distanceBetween(
                            location.getLatitude(), location.getLongitude(),
                            sessionLat, sessionLng,
                            results
                    );
                    float distance = results[0];
                    listener.onResult(new GeoResult(distance <= radiusMetres, distance));
                })
                .addOnFailureListener(e -> {
                    String msg = e.getMessage() != null ? e.getMessage() : "Location error";
                    listener.onResult(new GeoResult(false, -1f, msg));
                });
    }

    @SuppressLint("MissingPermission")
    public void verify(double sessionLat, double sessionLng, OnGeoResultListener listener) {
        verify(sessionLat, sessionLng, 20, listener);
    }
}
