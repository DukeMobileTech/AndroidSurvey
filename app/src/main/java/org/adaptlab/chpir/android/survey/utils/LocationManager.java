package org.adaptlab.chpir.android.survey.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.gson.JsonObject;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.repositories.SettingsRepository;
import org.adaptlab.chpir.android.survey.tasks.GetSettingsTask;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public class LocationManager {
    private static final String TAG = "LocationManager";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1200000;
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
    private SettingsRepository mSettingsRepository;
    private Settings mSettings;
    private SurveyViewModel mSurveyViewModel;

    public LocationManager(Context context) {
        mContext = context;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mSettingsClient = LocationServices.getSettingsClient(mContext);
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        setSettings();
    }

    private void setSettings() {
        mSettingsRepository = new SettingsRepository(SurveyApp.getInstance());
        GetSettingsTask getSettingsTask = new GetSettingsTask();
        getSettingsTask.setListener(new GetSettingsTask.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished(Settings settings) {
                mSettings = settings;
            }
        });
        getSettingsTask.execute(mSettingsRepository.getSettingsDao());
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                mSettings.setLatitude(getLatitude());
                mSettings.setLongitude(getLongitude());
                mSettingsRepository.update(mSettings);
                recordSurveyLocation();
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    public void setSurveyViewModel(SurveyViewModel viewModel) {
        mSurveyViewModel = viewModel;
        List<String> locations = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(mSurveyViewModel.getSurvey().getMetadata());
            String location = jsonObject.getString("location");
            if (!TextUtils.isEmpty(location)) {
               locations = new ArrayList<>(Arrays.asList(location.split(COMMA)));
            }
        } catch (JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "JSONException: " + e);
        }
        mSurveyViewModel.setLocations(locations);
    }

    private void recordSurveyLocation() {
        if (mSurveyViewModel != null && AppUtil.getSettings().isRecordSurveyLocation()) {
            if (TextUtils.isEmpty(mSurveyViewModel.getSurvey().getLatitude())) mSurveyViewModel.getSurvey().setLatitude(getLatitude());
            if (TextUtils.isEmpty(mSurveyViewModel.getSurvey().getLongitude())) mSurveyViewModel.getSurvey().setLongitude(getLongitude());

            JsonObject location = new JsonObject();
            location.addProperty("timestamp", new Date().getTime());
            location.addProperty("latitude", getLatitude());
            location.addProperty("longitude", getLongitude());
            mSurveyViewModel.getLocations().add(location.toString());

            StringBuilder stringBuilder = new StringBuilder();
            int index = 0;
            for (String string : mSurveyViewModel.getLocations()) {
                stringBuilder.append(string);
                if (index < mSurveyViewModel.getLocations().size()) {
                    stringBuilder.append(COMMA);
                }
                index++;
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("location", stringBuilder.toString());
            mSurveyViewModel.getSurvey().setMetadata(jsonObject.toString());

            mSurveyViewModel.update();
        }
    }

    public void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (!((Activity) mContext).isDestroyed()) {
                mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                        .addOnSuccessListener((Activity) mContext, new OnSuccessListener<LocationSettingsResponse>() {

                            @Override
                            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                                if (ContextCompat.checkSelfPermission(mContext,
                                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions((Activity) mContext,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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
                                        try {
                                            ResolvableApiException rae = (ResolvableApiException) e;
                                            rae.startResolutionForResult((Activity) mContext, REQUEST_CHECK_SETTINGS);
                                        } catch (IntentSender.SendIntentException sie) {
                                            if (BuildConfig.DEBUG) Log.e(TAG, "PendingIntent unable to execute request.");
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                        if (BuildConfig.DEBUG) Log.e(TAG, errorMessage);
                                        Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        }
    }

    private Location getCurrentLocation() {
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
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
    }

}