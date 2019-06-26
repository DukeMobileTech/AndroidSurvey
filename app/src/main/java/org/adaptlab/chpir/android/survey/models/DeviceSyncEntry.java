package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.util.Log;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.location.LocationManager;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.TimeZone;

public class DeviceSyncEntry extends SendModel {
    private static final String TAG = "DeviceSyncEntry";
    private LocationManager mLocationManager;

    public DeviceSyncEntry() {
        mLocationManager = new LocationManager(SurveyApp.getInstance());
        mLocationManager.startLocationUpdates();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("latitude", mLocationManager.getLatitude());
            jsonObject.put("longitude", mLocationManager.getLongitude());
            jsonObject.put("current_version_code", AppUtil.getVersionCode());
            jsonObject.put("current_version_name", AppUtil.getVersionName());
            jsonObject.put("num_complete_surveys", Survey.getCompleted().size());
            jsonObject.put("num_incomplete_surveys", Survey.getIncomplete().size());
            jsonObject.put("current_language", new Locale(AppUtil.getDeviceLanguage()).getDisplayLanguage());
            jsonObject.put("instrument_versions", instrumentVersions().toString());
            jsonObject.put("device_uuid", AdminSettings.getInstance().getDeviceIdentifier());
            jsonObject.put("api_key", AdminSettings.getInstance().getApiKey());
            jsonObject.put("timezone", TimeZone.getDefault().getDisplayName() + " " + TimeZone.getDefault().getID());
            jsonObject.put("project_id", AdminSettings.getInstance().getProjectId());
            jsonObject.put("device_label", AdminSettings.getInstance().getDeviceLabel());
            jsonObject.put("os_build_number", AppUtil.getOsBuildNumber());

            json.put("device_sync_entry", jsonObject);
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", je);
        }

        return json;
    }

    @Override
    public boolean isSent() {
        return false;
    }

    @Override
    public boolean readyToSend() {
        return true;
    }

    @Override
    public void setAsSent(Context context) {
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    private JSONObject instrumentVersions() {
        JSONObject json = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject();

            for (Instrument instrument : Instrument.getAll()) {
                jsonObject.put(instrument.getTitle(), instrument.getVersionNumber());
            }

            json.put("instrument_versions", jsonObject);
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", je);
        }

        return json;
    }

    public void pushRemote() {
        if (BuildConfig.DEBUG) Log.i(TAG, "Pushing sync entry");
        HttpURLConnection connection = null;
        String endPoint = ActiveRecordCloudSync.getEndPoint() + "device_sync_entries" +
                ActiveRecordCloudSync.getParams();
        try {
            connection = (HttpURLConnection) new URL(endPoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);

            JSONObject json = toJSON();
            byte[] outputInBytes = json.toString().getBytes(StandardCharsets.UTF_8);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(outputInBytes);
            outputStream.close();

            if (BuildConfig.DEBUG) {
                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    Log.i(TAG, "Received OK HTTP code for " + json);
                } else {
                    Log.e(TAG, "Received BAD HTTP code " + responseCode + " for " + json);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException " + e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
