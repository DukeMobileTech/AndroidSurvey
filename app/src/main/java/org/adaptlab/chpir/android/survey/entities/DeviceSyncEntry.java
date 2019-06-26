package org.adaptlab.chpir.android.survey.entities;

import com.google.gson.JsonObject;

import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.location.LocationManager;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRepository;
import org.adaptlab.chpir.android.survey.repositories.SettingsRepository;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.util.Locale;
import java.util.TimeZone;

public class DeviceSyncEntry implements Uploadable {
    private final static String TAG = DeviceSyncEntry.class.getName();

    private LocationManager mLocationManager;
    private Settings mSettings;
    private InstrumentRepository mInstrumentRepository;
    private SurveyRepository mSurveyRepository;

    public DeviceSyncEntry() {
//        mLocationManager = new LocationManager(SurveyApp.getInstance());
//        mLocationManager.startLocationUpdates();
        mSettings = new SettingsRepository(SurveyApp.getInstance()).getSettingsDao().getInstanceSync();
        mInstrumentRepository = new InstrumentRepository(SurveyApp.getInstance());
        mSurveyRepository = new SurveyRepository(SurveyApp.getInstance());
    }

    @Override
    public String toJSON() {
        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("latitude", mLocationManager.getLatitude());
//        jsonObject.addProperty("longitude", mLocationManager.getLongitude());
        jsonObject.addProperty("current_version_code", AppUtil.getVersionCode());
        jsonObject.addProperty("current_version_name", AppUtil.getVersionName());
        jsonObject.addProperty("num_complete_surveys", mSurveyRepository.getCompleted().size());
        jsonObject.addProperty("num_incomplete_surveys", mSurveyRepository.getIncomplete().size());
        jsonObject.addProperty("current_language", new Locale(AppUtil.getDeviceLanguage()).getDisplayLanguage());
        jsonObject.addProperty("instrument_versions", mInstrumentRepository.instrumentVersions());
        jsonObject.addProperty("device_uuid", mSettings.getDeviceIdentifier());
        jsonObject.addProperty("api_key", mSettings.getApiKey());
        jsonObject.addProperty("timezone", TimeZone.getDefault().getDisplayName() + " " + TimeZone.getDefault().getID());
        jsonObject.addProperty("project_id", mSettings.getProjectId());
        jsonObject.addProperty("device_label", mSettings.getDeviceLabel());
        jsonObject.addProperty("os_build_number", AppUtil.getOsBuildNumber());

        JsonObject json = new JsonObject();
        json.add("device_sync_entry", jsonObject);

        return json.toString();
    }

    @Override
    public void setSent(boolean status) {
    }
}
