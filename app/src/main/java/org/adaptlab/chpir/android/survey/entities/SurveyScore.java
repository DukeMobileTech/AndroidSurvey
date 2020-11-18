package org.adaptlab.chpir.android.survey.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.JsonObject;

import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.util.UUID;

@Entity(tableName = "SurveyScores")
public class SurveyScore implements Uploadable {
    private static final String TAG = "SurveyScore";
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "UUID", index = true)
    private String mUUID;
    @ColumnInfo(name = "SentToRemote")
    private boolean mSent;
    @NonNull
    @ColumnInfo(name = "SurveyUUID", index = true)
    private String mSurveyUUID;
    @ColumnInfo(name = "Identifier")
    private String mIdentifier;
    @ColumnInfo(name = "ScoreSum")
    private Double mScoreSum;
    @ColumnInfo(name = "ScoreSchemeRemoteId", index = true)
    private Long mScoreSchemeRemoteId;

    public SurveyScore() {
        mSent = false;
        mUUID = UUID.randomUUID().toString();
    }

    public SurveyScore(@NonNull String uuid) {
        mSent = false;
        mUUID = uuid;
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

    public String getIdentifier() {
        return mIdentifier;
    }

    public void setIdentifier(String mIdentifier) {
        this.mIdentifier = mIdentifier;
    }

    @Override
    public String toJSON() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("score_scheme_id", mScoreSchemeRemoteId);
        jsonObject.addProperty("device_uuid", AppUtil.getSettings().getDeviceIdentifier());
        jsonObject.addProperty("device_label", AppUtil.getSettings().getDeviceLabel());
        jsonObject.addProperty("uuid", mUUID);
        jsonObject.addProperty("survey_uuid", mSurveyUUID);
        jsonObject.addProperty("identifier", mIdentifier);
        jsonObject.addProperty("score_sum", mScoreSum);

        JsonObject json = new JsonObject();
        json.add("survey", jsonObject);
        return json.toString();
    }

    @NonNull
    public String getSurveyUUID() {
        return mSurveyUUID;
    }

    public void setSurveyUUID(@NonNull String mSurveyUUID) {
        this.mSurveyUUID = mSurveyUUID;
    }

    public Double getScoreSum() {
        return mScoreSum;
    }

    public void setScoreSum(Double mScoreSum) {
        this.mScoreSum = mScoreSum;
    }

    public Long getScoreSchemeRemoteId() {
        return mScoreSchemeRemoteId;
    }

    public void setScoreSchemeRemoteId(Long mScoreSchemeRemoteId) {
        this.mScoreSchemeRemoteId = mScoreSchemeRemoteId;
    }
}
