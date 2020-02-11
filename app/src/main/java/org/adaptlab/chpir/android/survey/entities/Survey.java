package org.adaptlab.chpir.android.survey.entities;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.JsonObject;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity(tableName = "Surveys")
public class Survey implements Uploadable {
    private static final String TAG = "Survey";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "UUID", index = true)
    private String mUUID;
    @ColumnInfo(name = "SentToRemote")
    private boolean mSent;
    @ColumnInfo(name = "Complete")
    private boolean mComplete;
    @ColumnInfo(name = "Latitude")
    private String mLatitude;
    @ColumnInfo(name = "Longitude")
    private String mLongitude;
    @ColumnInfo(name = "LastUpdated")
    private Date mLastUpdated;
    @ColumnInfo(name = "Metadata")
    private String mMetadata;
    @ColumnInfo(name = "ProjectId", index = true)
    private Long mProjectId;
    @ColumnInfo(name = "InstrumentRemoteId", index = true)
    private Long mInstrumentRemoteId;
    @ColumnInfo(name = "RosterUUID")
    private String mRosterUUID;
    @ColumnInfo(name = "Language")
    private String mLanguage;
    @ColumnInfo(name = "SkippedQuestions")
    private String mSkippedQuestions;
    @ColumnInfo(name = "SkipMaps")
    private String mSkipMaps;
    @ColumnInfo(name = "Identifier")
    private String mIdentifier;
    @ColumnInfo(name = "CompletedResponseCount")
    private int mCompletedResponseCount;
    @ColumnInfo(name = "Queued")
    private boolean mQueued;
    @ColumnInfo(name = "LastDisplayPosition")
    private int mLastDisplayPosition;
    @ColumnInfo(name = "PreviousDisplays")
    private String mPreviousDisplays;
    @ColumnInfo(name = "InstrumentTitle")
    private String mInstrumentTitle;
    @ColumnInfo(name = "InstrumentVersionNumber")
    private String mInstrumentVersionNumber;

    public Survey() {
        mSent = false;
        mComplete = false;
        mQueued = false;
        mUUID = UUID.randomUUID().toString();
        mLastUpdated = new Date();
    }

    public Survey(@NonNull String uuid) {
        mSent = false;
        mComplete = false;
        mQueued = false;
        mUUID = uuid;
        mLastUpdated = new Date();
    }

    @NonNull
    public String getUUID() {
        return mUUID;
    }

    public void setUUID(@NonNull String mUUID) {
        this.mUUID = mUUID;
    }

    public boolean isSent() {
        return mSent;
    }

    public void setSent(boolean mSent) {
        this.mSent = mSent;
    }

    public boolean isComplete() {
        return mComplete;
    }

    public void setComplete(boolean mComplete) {
        this.mComplete = mComplete;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String mLatitude) {
        this.mLatitude = mLatitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String mLongitude) {
        this.mLongitude = mLongitude;
    }

    public Date getLastUpdated() {
        return mLastUpdated;
    }

    public void setLastUpdated(Date mLastUpdated) {
        this.mLastUpdated = mLastUpdated;
    }

    public String getMetadata() {
        return mMetadata;
    }

    public void setMetadata(String mMetadata) {
        this.mMetadata = mMetadata;
    }

    public Long getProjectId() {
        return mProjectId;
    }

    public void setProjectId(Long mProjectId) {
        this.mProjectId = mProjectId;
    }

    public Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    public void setInstrumentRemoteId(Long mInstrumentRemoteId) {
        this.mInstrumentRemoteId = mInstrumentRemoteId;
    }

    public String getRosterUUID() {
        return mRosterUUID;
    }

    public void setRosterUUID(String mRosterUUID) {
        this.mRosterUUID = mRosterUUID;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String mLanguage) {
        this.mLanguage = mLanguage;
    }

    public String getSkippedQuestions() {
        return mSkippedQuestions;
    }

    public void setSkippedQuestions(String mSkippedQuestions) {
        this.mSkippedQuestions = mSkippedQuestions;
    }

    public String getSkipMaps() {
        return mSkipMaps;
    }

    public void setSkipMaps(String mSkipMaps) {
        this.mSkipMaps = mSkipMaps;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public void setIdentifier(String mIdentifier) {
        this.mIdentifier = mIdentifier;
    }

    public int getCompletedResponseCount() {
        return mCompletedResponseCount;
    }

    public void setCompletedResponseCount(int mCompletedResponseCount) {
        this.mCompletedResponseCount = mCompletedResponseCount;
    }

    public boolean isQueued() {
        return mQueued;
    }

    public void setQueued(boolean mQueued) {
        this.mQueued = mQueued;
    }

    public int getLastDisplayPosition() {
        return mLastDisplayPosition;
    }

    public void setLastDisplayPosition(int position) {
        this.mLastDisplayPosition = position;
    }

    /*
     * The identifier to display to the user to identify a Survey.
     * Return Unidentified Survey string if no response for identifier questions.
     */
    public String identifier(Context context, List<Response> responses) {
        if (isSent() && !TextUtils.isEmpty(getIdentifier())) {
            return getIdentifier();
        }

        String surveyLabel = null;
        StringBuilder identifier = new StringBuilder();

        if (!TextUtils.isEmpty(getMetadata())) {
            surveyLabel = getMetadataLabel();
        }
        if (!TextUtils.isEmpty(surveyLabel)) {
            return surveyLabel;
        }

        for (Response response : responses) {
            if (response.isIdentifiesSurvey()) {
                identifier.append(response.getText()).append(" ");
            }
        }

        if (identifier.toString().trim().isEmpty())
            return context.getResources().getString(R.string.unidentified_survey) + " " + getUUID();
        else
            return identifier.toString();
    }

    private String getMetadataLabel() {
        try {
            JSONObject metadata = new JSONObject(getMetadata());
            if (metadata.has("survey_label")) {
                return metadata.getString("survey_label");
            }
        } catch (JSONException er) {
            if (BuildConfig.DEBUG) Log.e(TAG, er.getMessage());
        }

        return "";
    }

    public String getPreviousDisplays() {
        return mPreviousDisplays;
    }

    public void setPreviousDisplays(String previousDisplays) {
        mPreviousDisplays = previousDisplays;
    }

    public String getInstrumentTitle() {
        return mInstrumentTitle;
    }

    public void setInstrumentTitle(String mInstrumentTitle) {
        this.mInstrumentTitle = mInstrumentTitle;
    }

    public String getInstrumentVersionNumber() {
        return mInstrumentVersionNumber;
    }

    public void setInstrumentVersionNumber(String mInstrumentVersionNumber) {
        this.mInstrumentVersionNumber = mInstrumentVersionNumber;
    }

    @Override
    public String toJSON() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("instrument_id", mInstrumentRemoteId);
        jsonObject.addProperty("instrument_version_number", mInstrumentVersionNumber);
        jsonObject.addProperty("device_uuid", AppUtil.getSettings().getDeviceIdentifier());
        jsonObject.addProperty("device_label", AppUtil.getSettings().getDeviceLabel());
        jsonObject.addProperty("uuid", mUUID);
        jsonObject.addProperty("instrument_title", mInstrumentTitle);
        jsonObject.addProperty("latitude", mLatitude);
        jsonObject.addProperty("longitude", mLongitude);
        jsonObject.addProperty("metadata", mMetadata);
        jsonObject.addProperty("skipped_questions", mSkippedQuestions);
        jsonObject.addProperty("roster_uuid", mRosterUUID);
        jsonObject.addProperty("language", mLanguage);
        jsonObject.addProperty("completed_responses_count", mCompletedResponseCount);

        JsonObject json = new JsonObject();
        json.add("survey", jsonObject);
        return json.toString();
    }
}
