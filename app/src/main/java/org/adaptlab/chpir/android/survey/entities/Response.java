package org.adaptlab.chpir.android.survey.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.utils.AuthUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;

@Entity(tableName = "Responses", indices = {@Index(name = "survey_uuid_question_identifier_uuid_index",
        value = {"SurveyUUID", "QuestionIdentifier", "UUID"}, unique = true)})
public class Response implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "UUID", index = true)
    private String mUUID;
    @ColumnInfo(name = "SurveyUUID", index = true)
    private String mSurveyUUID;
    @ColumnInfo(name = "QuestionRemoteId", index = true)
    private Long mQuestionRemoteId;
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

    public Response() {
        mSent = false;
        mText = BLANK;
        mOtherResponse = BLANK;
        mSpecialResponse = BLANK;
        mUUID = UUID.randomUUID().toString();
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

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<Response>>() {
        }.getType();
    }

    @Override
    public List getTranslations() {
        return null;
    }

    @Override
    public void save(BaseDao dao, List list) {
        dao.updateAll(list);
        dao.insertAll(list);
    }
}
