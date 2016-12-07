package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.util.Log;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.AppUtil;
import org.adaptlab.chpir.android.survey.location.LocationServiceManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.TimeZone;

public class DeviceSyncEntry extends SendModel {
    private static final String TAG = "DeviceSyncEntry";
    private LocationServiceManager mLocationServiceManager;

    public DeviceSyncEntry() {
        mLocationServiceManager = LocationServiceManager.get(AppUtil.getContext());
//        mLocationServiceManager.startLocationUpdates(); // TODO: 12/6/16 Upgrade to api 23
    }

    @Override
    public JSONObject toJSON() {
        Log.i(TAG, "Creating JSON for " + TAG);
        JSONObject json = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("latitude", mLocationServiceManager.getLatitude());
            jsonObject.put("longitude", mLocationServiceManager.getLongitude());
            jsonObject.put("current_version_code", AppUtil.getVersionCode(AppUtil.getContext()));
            jsonObject.put("current_version_name", AppUtil.getVersionName(AppUtil.getContext()));
            jsonObject.put("num_complete_surveys", Survey.getCompleted().size());
            jsonObject.put("num_incomplete_surveys", Survey.getIncomplete().size());
            jsonObject.put("current_language", Locale.getDefault().getDisplayLanguage());
            jsonObject.put("instrument_versions", instrumentVersions().toString());
            jsonObject.put("device_uuid", AdminSettings.getInstance().getDeviceIdentifier());
            jsonObject.put("api_key", AdminSettings.getInstance().getApiKey());
            jsonObject.put("timezone", TimeZone.getDefault().getDisplayName() + " " + TimeZone.getDefault().getID());
            jsonObject.put("project_id", AdminSettings.getInstance().getProjectId());
            jsonObject.put("device_label", AdminSettings.getInstance().getDeviceLabel());
            jsonObject.put("os_build_number", AppUtil.getOsBuildNumber());

            json.put("device_sync_entry", jsonObject);
        } catch (JSONException je) {
            Log.e(TAG, "JSON exception", je);
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
            Log.e(TAG, "JSON exception", je);
        }

        return json;
    }
}
