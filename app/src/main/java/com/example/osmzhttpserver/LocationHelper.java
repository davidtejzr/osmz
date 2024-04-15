package com.example.osmzhttpserver;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    public LocationHelper(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void requestLocation(final OnSuccessListener<Location> onSuccessListener) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener((com.google.android.gms.tasks.OnSuccessListener<? super Location>) location -> {
                    if (location != null) {
                        onSuccessListener.onSuccess(location);
                    }
                });
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }
}
