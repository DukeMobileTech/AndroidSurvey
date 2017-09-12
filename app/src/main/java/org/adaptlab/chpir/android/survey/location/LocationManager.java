package org.adaptlab.chpir.android.survey.location;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationManager {
    private static final String TAG = "LocationManager";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 600000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Context mContext;

    public LocationManager(Context context) {
        mContext = context;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mSettingsClient = LocationServices.getSettingsClient(mContext);
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                Log.i(TAG, "Latitude: " + mCurrentLocation.getLatitude());
                Log.i(TAG, "Longitude: " + mCurrentLocation.getLongitude());
                Log.i(TAG, "Altitude: " + mCurrentLocation.getAltitude());
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    public void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener((Activity) mContext, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");
                        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission
                                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) mContext,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        } else {
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        }
                    }
                })
                .addOnFailureListener((Activity) mContext, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade location settings ");
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult((Activity) mContext, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public String getLatitude() {
        if (getCurrentLocation() == null) return null;
        return Double.toString(mCurrentLocation.getLatitude());
    }

    public String getLongitude() {
        if (getCurrentLocation() == null) return null;
        return Double.toString(mCurrentLocation.getLongitude());
    }

    public String getAltitude() {
        if (getCurrentLocation() == null) return null;
        return Double.toString(mCurrentLocation.getAltitude());
    }

    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback).addOnCompleteListener((Activity) mContext, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {}
        });
    }

}
