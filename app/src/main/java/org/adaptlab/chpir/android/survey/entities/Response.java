package org.adaptlab.chpir.android.survey.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.JsonObject;

import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.util.Date;
import java.util.UUID;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;

@Entity(tableName = "Responses", indices = {@Index(value = {"SurveyUUID", "QuestionIdentifier", "UUID"}, unique = true)})
public class Response implements Uploadable {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "UUID", index = true)
    private String mUUID;
    @NonNull
    @ColumnInfo(name = "SurveyUUID", index = true)
    private String mSurveyUUID;
    @NonNull
    @ColumnInfo(name = "QuestionRemoteId", index = true)
    private Long mQuestionRemoteId;
    @NonNull
    @ColumnInfo(name = "QuestionIdentifier", index = true)
    private String mQuestionIdentifier;
    @ColumnInfo(name = "Text")
    private String mText;
    @ColumnInfo(name = "OtherResponse")
    private String mOtherResponse;
    @ColumnInfo(name = "SpecialResponse")
    private String mSpecialResponse;
    @ColumnInfo(name = "SentToRemote")
    private boolean mSent;
    @ColumnInfo(name = "TimeStarted")
    private Date mTimeStarted;
    @ColumnInfo(name = "TimeEnded")
    private Date mTimeEnded;
    @ColumnInfo(name = "DeviceUserId")
    private Long mDeviceUserId;
    @ColumnInfo(name = "QuestionVersion")
    private int mQuestionVersion;
    @ColumnInfo(name = "RandomizedData")
    private String mRandomizedData;
    @ColumnInfo(name = "RankOrder")
    private String mRankOrder;
    @ColumnInfo(name = "IdentifiesSurvey")
    private boolean mIdentifiesSurvey;
    @ColumnInfo(name = "OtherText")
    private String mOtherText;

    public Response() {
        mSent = false;
        mText = BLANK;
        mOtherResponse = BLANK;
        mSpecialResponse = BLANK;
        mOtherText = BLANK;
        mUUID = UUID.randomUUID().toString();
        mDeviceUserId = AppUtil.getDeviceUserId();
    }

    @NonNull
    public String getUUID() {
        return mUUID;
    }

    public void setUUID(@NonNull String mUUID) {
        this.mUUID = mUUID;
    }

    public String getSurveyUUID() {
        return mSurveyUUID;
    }

    public void setSurveyUUID(String mSurveyUUID) {
        this.mSurveyUUID = mSurveyUUID;
    }

    public Long getQuestionRemoteId() {
        return mQuestionRemoteId;
    }

    public void setQuestionRemoteId(Long mQuestionRemoteId) {
        this.mQuestionRemoteId = mQuestionRemoteId;
    }

    public String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }

    public void setQuestionIdentifier(String mQuestionIdentifier) {
        this.mQuestionIdentifier = mQuestionIdentifier;
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public String getOtherResponse() {
        return mOtherResponse;
    }

    public void setOtherResponse(String mOtherResponse) {
        this.mOtherResponse = mOtherResponse;
    }

    public String getSpecialResponse() {
        return mSpecialResponse;
    }

    public void setSpecialResponse(String mSpecialResponse) {
        this.mSpecialResponse = mSpecialResponse;
    }

    public boolean isSent() {
        return mSent;
    }

    public void setSent(boolean mSent) {
        this.mSent = mSent;
    }

    public Date getTimeStarted() {
        return mTimeStarted;
    }

    public void setTimeStarted(Date mTimeStarted) {
        this.mTimeStarted = mTimeStarted;
    }

    public Date getTimeEnded() {
        return mTimeEnded;
    }

    public void setTimeEnded(Date mTimeEnded) {
        this.mTimeEnded = mTimeEnded;
    }

    public Long getDeviceUserId() {
        return mDeviceUserId;
    }

    public void setDeviceUserId(Long mDeviceUserId) {
        this.mDeviceUserId = mDeviceUserId;
    }

    public int getQuestionVersion() {
        return mQuestionVersion;
    }

    public void setQuestionVersion(int mQuestionVersion) {
        this.mQuestionVersion = mQuestionVersion;
    }

    public String getRandomizedData() {
        return mRandomizedData;
    }

    public void setRandomizedData(String mRandomizedData) {
        this.mRandomizedData = mRandomizedData;
    }

    public String getRankOrder() {
        return mRankOrder;
    }

    public void setRankOrder(String mRankOrder) {
        this.mRankOrder = mRankOrder;
    }

    public boolean isIdentifiesSurvey() {
        return mIdentifiesSurvey;
    }

    public void setIdentifiesSurvey(boolean identifiesSurvey) {
        mIdentifiesSurvey = identifiesSurvey;
    }

    public boolean isEmptyResponse() {
        return mText.isEmpty() && mOtherResponse.isEmpty() && mSpecialResponse.isEmpty() && mOtherText.isEmpty();
    }

    @Override
    public String toJSON() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("survey_uuid", mSurveyUUID);
        jsonObject.addProperty("question_id", mQuestionRemoteId);
        jsonObject.addProperty("text", mText);
        jsonObject.addProperty("other_response", mOtherResponse);
        jsonObject.addProperty("special_response", mSpecialResponse);
        jsonObject.addProperty("time_started", mTimeStarted.getTime());
        jsonObject.addProperty("time_ended", mTimeEnded == null ? null : mTimeEnded.getTime());
        jsonObject.addProperty("question_identifier", mQuestionIdentifier);
        jsonObject.addProperty("uuid", mUUID);
        jsonObject.addProperty("question_version", mQuestionVersion);
        jsonObject.addProperty("randomized_data", mRandomizedData);
        jsonObject.addProperty("rank_order", mRankOrder);
        jsonObject.addProperty("device_user_id", mDeviceUserId);
        jsonObject.addProperty("other_text", mOtherText);

        JsonObject json = new JsonObject();
        json.add("response", jsonObject);

        return json.toString();
    }

    public String getOtherText() {
        return mOtherText;
    }

    public void setOtherText(String mOtherText) {
        this.mOtherText = mOtherText;
    }
}
