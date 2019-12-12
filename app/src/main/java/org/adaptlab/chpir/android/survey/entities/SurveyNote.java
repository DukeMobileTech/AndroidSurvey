package org.adaptlab.chpir.android.survey.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.JsonObject;

import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.util.UUID;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;

@Entity(tableName = "SurveyNotes")
public class SurveyNote implements Uploadable {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "UUID", index = true)
    private String mUUID;
    @NonNull
    @ColumnInfo(name = "SurveyUUID", index = true)
    private String mSurveyUUID;
   @ColumnInfo(name = "Text")
    private String mText;
    @ColumnInfo(name = "Reference")
    private String mReference;
    @ColumnInfo(name = "SentToRemote")
    private boolean mSent;
    @ColumnInfo(name = "DeviceUserId")
    private Long mDeviceUserId;

    public SurveyNote() {
        mSent = false;
        mText = BLANK;
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

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public boolean isSent() {
        return mSent;
    }

    public void setSent(boolean mSent) {
        this.mSent = mSent;
    }

    public Long getDeviceUserId() {
        return mDeviceUserId;
    }

    public void setDeviceUserId(Long mDeviceUserId) {
        this.mDeviceUserId = mDeviceUserId;
    }

    public String getReference() {
        return mReference;
    }

    public void setReference(String mReference) {
        this.mReference = mReference;
    }

    @Override
    public String toJSON() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("survey_uuid", mSurveyUUID);
        jsonObject.addProperty("text", mText);
        jsonObject.addProperty("reference", mReference);
        jsonObject.addProperty("uuid", mUUID);
        jsonObject.addProperty("device_user_id", mDeviceUserId);

        JsonObject json = new JsonObject();
        json.add("survey_note", jsonObject);

        return json.toString();
    }
}
