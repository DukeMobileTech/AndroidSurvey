package org.adaptlab.chpir.android.survey.models;

import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Table(name = "AdminSettings")
public class AdminSettings extends Model {
    private static final String TAG = "AdminSettings";
    @Column(name = "DeviceIdentifier")
    private String mDeviceIdentifier;
    @Column(name = "DeviceLabel")
    private String mDeviceLabel;
    @Column(name = "ApiUrl")
    private String mApiUrl;
    @Column(name = "CustomLocaleCode")
    private String mCustomLocaleCode;
    @Column(name = "ShowSurveys")
    private boolean mShowSurveys;
    @Column(name = "ShowSkip")
    private boolean mShowSkip;
    @Column(name = "ShowNA")
    private boolean mShowNA;
    @Column(name = "ShowRF")
    private boolean mShowRF;
    @Column(name = "ShowDK")
    private boolean mShowDK;
    @Column(name = "ApiVersion")
    private String mApiVersion;
    @Column(name = "ProjectId")
    private String mProjectId;
    @Column(name = "ApiKey")
    private String mApiKey;
    @Column(name = "RequirePassword")
    private boolean mRequirePassword;
    @Column(name = "RecordSurveyLocation")
    private boolean mRecordSurveyLocation;
    @Column(name = "LastSyncTimes")
    private String mLastSyncTime;
    @Column(name = "SecondEndpoint")
    private boolean mSecondEndpoint;
    @Column(name = "Api2Url")
    private String mApi2Url;
    @Column(name = "Api2Version")
    private String mApi2Version;
    @Column(name = "Api2Key")
    private String mApi2Key;
    @Column(name = "ShowRosters")
    private boolean mShowRosters;
    @Column(name = "ShowScores")
    private boolean mShowScores;

    /**
     * Typically a Singleton constructor is private, but in this case the constructor
     * must be public for ActiveAndroid to function properly.  Do not use this
     * constructor, use getInstance() instead.
     */
    public AdminSettings() {
        super();
        mShowSurveys = false;
    }

    /**
     * This maintains a single row in the database for the admin settings, and
     * effectively is a Singleton.  This is done to piggy-back on the
     * ReceiveModel functionality.
     */
    public static AdminSettings getInstance() {
        AdminSettings adminSettings = new Select().from(AdminSettings.class).orderBy("Id asc")
                .executeSingle();
        if (adminSettings == null) {
            if (BuildConfig.DEBUG) Log.i(TAG, "Creating new admin settings instance");
            adminSettings = new AdminSettings();
            adminSettings.save();
        }
        return adminSettings;
    }

    public void setUseEndpoint2(boolean status) {
        mSecondEndpoint = status;
        save();
    }

    public boolean useEndpoint2() {
        return mSecondEndpoint;
    }

    public String getApi2DomainName() {
        return mApi2Url;
    }

    public void setApi2DomainName(String url) {
        if (!TextUtils.isEmpty(url)) {
            char lastChar = url.charAt(url.length() - 1);
            if (lastChar != '/') url = url + "/";
            mApi2Url = url;
            save();
        }
    }

    public String getApi2Version() {
        return mApi2Version;
    }

    public void setApi2Version(String version) {
        mApi2Version = version;
        save();
    }

    public String getApi2Key() {
        return mApi2Key;
    }

    public void setApi2Key(String key) {
        mApi2Key = key;
        save();
    }

    public String getDeviceIdentifier() {
        return mDeviceIdentifier;
    }

    public void setDeviceIdentifier(String id) {
        mDeviceIdentifier = id;
        save();
    }

    public String getDeviceLabel() {
        return mDeviceLabel;
    }

    public void setDeviceLabel(String label) {
        mDeviceLabel = label;
        save();
    }

    public String getCustomLocaleCode() {
        return mCustomLocaleCode;
    }

    public void setCustomLocaleCode(String code) {
        mCustomLocaleCode = code;
        save();
    }

    public boolean getShowSurveys() {
        return mShowSurveys;
    }

    public void setShowSurveys(boolean showSurveys) {
        mShowSurveys = showSurveys;
        save();
    }

    public String getApiKey() {
        return mApiKey;
    }

    public void setApiKey(String apiKey) {
        mApiKey = apiKey;
        save();
    }

    public boolean getRequirePassword() {
        return mRequirePassword;
    }

    public void setRequirePassword(boolean requirePassword) {
        mRequirePassword = requirePassword;
        save();
    }

    public boolean getRecordSurveyLocation() {
        return mRecordSurveyLocation;
    }

    public void setRecordSurveyLocation(boolean recordSurveyLocation) {
        mRecordSurveyLocation = recordSurveyLocation;
        save();
    }

    public String getApiUrl() {
        return getApiDomainName() + "api/" + getApiVersion() + "/" + "projects/" + getProjectId()
                + "/";
    }

    public String getApi2url() {
        return getApi2DomainName() + "api/" + getApi2Version() + "/";
    }

    public String getApiDomainName() {
        return mApiUrl;
    }

    public void setApiDomainName(String apiUrl) {
        if (!TextUtils.isEmpty(apiUrl)) {
            char lastChar = apiUrl.charAt(apiUrl.length() - 1);
            if (lastChar != '/') apiUrl = apiUrl + "/";
            mApiUrl = apiUrl;
            save();
        }
    }

    public String getApiVersion() {
        return mApiVersion;
    }

    public void setApiVersion(String apiVersion) {
        mApiVersion = apiVersion;
        save();
    }

    public String getProjectId() {
        return mProjectId;
    }

    public void setProjectId(String projectId) {
        mProjectId = projectId;
        save();
    }

    public String getLastSyncTime() {
        String lastSyncTime = "";
        if (mLastSyncTime == null) {
            return lastSyncTime;
        }
        try {
            JSONObject projectLastSyncTime = new JSONObject(mLastSyncTime);
            if (!projectLastSyncTime.isNull(getProjectId())) {
                lastSyncTime = projectLastSyncTime.getString(getProjectId());
            }
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", je);
        }
        return lastSyncTime;
    }

    public void setLastSyncTime(String syncTime) {
        JSONObject projectTime = new JSONObject();
        try {
            if (mLastSyncTime == null) {
                projectTime = new JSONObject();
            } else {
                projectTime = new JSONObject(mLastSyncTime);
            }
            projectTime.put(getProjectId(), syncTime);
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", je);
        }
        mLastSyncTime = projectTime.toString();
        save();
    }

    public void resetLastSyncTime() {
        mLastSyncTime = null;
        save();
    }

    public List<String> getSpecialOptions() {
        List<String> options = new ArrayList<String>();
        if (getShowDK()) options.add(Response.DK);
        if (getShowNA()) options.add(Response.NA);
        if (getShowRF()) options.add(Response.RF);
        if (getShowSkip()) options.add(Response.SKIP);
        return options;
    }

    /*
     * Show DONT KNOW special response for each question if true
     */
    public boolean getShowDK() {
        return mShowDK;
    }

    /*
     * Show NOT APPLICABLE special response for each question if true
     */
    public boolean getShowNA() {
        return mShowNA;
    }

    public void setShowNA(boolean showNA) {
        mShowNA = showNA;
        save();
    }

    /*
     * Show REFUSED special response for each question if true
     */
    public boolean getShowRF() {
        return mShowRF;
    }

    /*
     * Show SKIP special response for each question if true
     */
    public boolean getShowSkip() {
        return mShowSkip;
    }

    public void setShowSkip(boolean showSkip) {
        mShowSkip = showSkip;
        save();
    }

    public void setShowRF(boolean showRF) {
        mShowRF = showRF;
        save();
    }

    public void setShowDK(boolean showDK) {
        mShowDK = showDK;
        save();
    }

    public boolean getShowRosters() {
        return mShowRosters;
    }

    public void setShowRosters(boolean showRosters) {
        mShowRosters = showRosters;
        save();
    }

    public boolean getShowScores() {
        return mShowScores;
    }

    public void setShowScores(boolean showScores) {
        mShowScores = showScores;
        save();
    }

}