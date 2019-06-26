package org.adaptlab.chpir.android.survey.entities;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Settings")
public class Settings {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id", index = true)
    private Long mId;
    @ColumnInfo(name = "DeviceIdentifier")
    private String mDeviceIdentifier;
    @ColumnInfo(name = "DeviceLabel")
    private String mDeviceLabel;
    @ColumnInfo(name = "ApiUrl")
    private String mApiUrl;
    @ColumnInfo(name = "CustomLocaleCode")
    private String mCustomLocaleCode;
    @ColumnInfo(name = "ShowSurveys")
    private boolean mShowSurveys;
    @ColumnInfo(name = "ShowSkip")
    private boolean mShowSkip;
    @ColumnInfo(name = "ShowNA")
    private boolean mShowNA;
    @ColumnInfo(name = "ShowRF")
    private boolean mShowRF;
    @ColumnInfo(name = "ShowDK")
    private boolean mShowDK;
    @ColumnInfo(name = "ApiVersion")
    private String mApiVersion;
    @ColumnInfo(name = "ProjectId")
    private String mProjectId;
    @ColumnInfo(name = "ApiKey")
    private String mApiKey;
    @ColumnInfo(name = "RequirePassword")
    private boolean mRequirePassword;
    @ColumnInfo(name = "RecordSurveyLocation")
    private boolean mRecordSurveyLocation;
    @ColumnInfo(name = "LastSyncTimes")
    private String mLastSyncTime;
    @ColumnInfo(name = "SecondEndpoint")
    private boolean mSecondEndpoint;
    @ColumnInfo(name = "Api2Url")
    private String mApi2Url;
    @ColumnInfo(name = "Api2Version")
    private String mApi2Version;
    @ColumnInfo(name = "Api2Key")
    private String mApi2Key;
    @ColumnInfo(name = "ShowRosters")
    private boolean mShowRosters;
    @ColumnInfo(name = "ShowScores")
    private boolean mShowScores;
    @ColumnInfo(name = "Language")
    private String mLanguage;
    @ColumnInfo(name = "DatabaseVersion")
    private int mDatabaseVersion;
    @ColumnInfo(name = "DeviceUserName")
    private String mDeviceUserName;

    @NonNull
    public Long getId() {
        return mId;
    }

    public void setId(@NonNull Long mId) {
        this.mId = mId;
    }

    public String getDeviceIdentifier() {
        return mDeviceIdentifier;
    }

    public void setDeviceIdentifier(String mDeviceIdentifier) {
        this.mDeviceIdentifier = mDeviceIdentifier;
    }

    public String getDeviceLabel() {
        return mDeviceLabel;
    }

    public void setDeviceLabel(String mDeviceLabel) {
        this.mDeviceLabel = mDeviceLabel;
    }

    public String getCustomLocaleCode() {
        return mCustomLocaleCode;
    }

    public void setCustomLocaleCode(String mCustomLocaleCode) {
        this.mCustomLocaleCode = mCustomLocaleCode;
    }

    public boolean isShowSurveys() {
        return mShowSurveys;
    }

    public void setShowSurveys(boolean mShowSurveys) {
        this.mShowSurveys = mShowSurveys;
    }

    public boolean isShowSkip() {
        return mShowSkip;
    }

    public void setShowSkip(boolean mShowSkip) {
        this.mShowSkip = mShowSkip;
    }

    public boolean isShowNA() {
        return mShowNA;
    }

    public void setShowNA(boolean mShowNA) {
        this.mShowNA = mShowNA;
    }

    public boolean isShowRF() {
        return mShowRF;
    }

    public void setShowRF(boolean mShowRF) {
        this.mShowRF = mShowRF;
    }

    public boolean isShowDK() {
        return mShowDK;
    }

    public void setShowDK(boolean mShowDK) {
        this.mShowDK = mShowDK;
    }

    public String getApiVersion() {
        return mApiVersion;
    }

    public void setApiVersion(String mApiVersion) {
        this.mApiVersion = mApiVersion;
    }

    public String getProjectId() {
        return mProjectId;
    }

    public void setProjectId(String mProjectId) {
        this.mProjectId = mProjectId;
    }

    public String getApiKey() {
        return mApiKey;
    }

    public void setApiKey(String mApiKey) {
        this.mApiKey = mApiKey;
    }

    public boolean isRequirePassword() {
        return mRequirePassword;
    }

    public void setRequirePassword(boolean mRequirePassword) {
        this.mRequirePassword = mRequirePassword;
    }

    public boolean isRecordSurveyLocation() {
        return mRecordSurveyLocation;
    }

    public void setRecordSurveyLocation(boolean mRecordSurveyLocation) {
        this.mRecordSurveyLocation = mRecordSurveyLocation;
    }

    public String getLastSyncTime() {
        return mLastSyncTime;
    }

    public void setLastSyncTime(String mLastSyncTime) {
        this.mLastSyncTime = mLastSyncTime;
    }

    public boolean isSecondEndpoint() {
        return mSecondEndpoint;
    }

    public void setSecondEndpoint(boolean mSecondEndpoint) {
        this.mSecondEndpoint = mSecondEndpoint;
    }

    public String getApi2Url() {
        return mApi2Url;
    }

    public void setApi2Url(String mApi2Url) {
        this.mApi2Url = mApi2Url;
    }

    public String getApi2Version() {
        return mApi2Version;
    }

    public void setApi2Version(String mApi2Version) {
        this.mApi2Version = mApi2Version;
    }

    public int getDatabaseVersion() {
        return mDatabaseVersion;
    }

    public void setDatabaseVersion(int mDatabaseVersion) {
        this.mDatabaseVersion = mDatabaseVersion;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String mLanguage) {
        this.mLanguage = mLanguage;
    }

    public boolean isShowScores() {
        return mShowScores;
    }

    public void setShowScores(boolean mShowScores) {
        this.mShowScores = mShowScores;
    }

    public boolean isShowRosters() {
        return mShowRosters;
    }

    public void setShowRosters(boolean mShowRosters) {
        this.mShowRosters = mShowRosters;
    }

    public String getApi2Key() {
        return mApi2Key;
    }

    public void setApi2Key(String mApi2Key) {
        this.mApi2Key = mApi2Key;
    }

    public String getApiUrl() {
        return mApiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.mApiUrl = apiUrl;
    }

    public String getDeviceUserName() {
        return mDeviceUserName;
    }

    public void setDeviceUserName(String name) {
        mDeviceUserName = name;
    }

    public String getFullApiUrl() {
        String apiUrl = mApiUrl;
        if (!TextUtils.isEmpty(apiUrl)) {
            char lastChar = apiUrl.charAt(apiUrl.length() - 1);
            if (lastChar != '/') apiUrl = apiUrl + "/";
        }
        return apiUrl + "api/" + getApiVersion() + "/" + "projects/" + getProjectId() + "/";
    }

    public void resetLastSyncTime() {
        mLastSyncTime = null;
    }

}
