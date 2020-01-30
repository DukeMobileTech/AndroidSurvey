package org.adaptlab.chpir.android.survey.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "Displays")
public class Display implements SurveyEntity, Translatable {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("title")
    @ColumnInfo(name = "Title", index = true)
    private String mTitle;
    @SerializedName("position")
    @ColumnInfo(name = "Position", index = true)
    private int mPosition;
    @SerializedName("question_count")
    @ColumnInfo(name = "QuestionCount")
    private int mQuestionCount;
    @SerializedName("instrument_id")
    @ColumnInfo(name = "InstrumentRemoteId", index = true)
    private Long mInstrumentRemoteId;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("section_id")
    @ColumnInfo(name = "SectionId")
    private Long mSectionId;
    @SerializedName("instrument_position")
    @ColumnInfo(name = "InstrumentPosition", index = true)
    private int mInstrumentPosition;
    @Ignore
    @SerializedName("display_translations")
    private List<DisplayTranslation> mDisplayTranslations;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    public void setInstrumentRemoteId(Long mInstrumentRemoteId) {
        this.mInstrumentRemoteId = mInstrumentRemoteId;
    }

    public Long getSectionId() {
        return mSectionId;
    }

    public void setSectionId(Long mSectionId) {
        this.mSectionId = mSectionId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long remoteId) {
        this.mRemoteId = remoteId;
    }

    public int getQuestionCount() {
        return mQuestionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.mQuestionCount = questionCount;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    public List<DisplayTranslation> getDisplayTranslations() {
        return mDisplayTranslations;
    }

    public void setDisplayTranslations(List<DisplayTranslation> mDisplayTranslations) {
        this.mDisplayTranslations = mDisplayTranslations;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<Display>>() {
        }.getType();
    }

    @Override
    public List<DisplayTranslation> getTranslations() {
        return mDisplayTranslations;
    }

    @Override
    public void save(BaseDao dao, List list) {
        dao.updateAll(list);
        dao.insertAll(list);
    }

    @NonNull
    public String toString() {
        return new ToStringBuilder(this).
                append("RemoteId", mRemoteId).
                append("Title", mTitle).
                append("Position", mPosition).
                append("QuestionCount", mQuestionCount).
                append("InstrumentPosition", mInstrumentPosition).
                toString();
    }

    public int getInstrumentPosition() {
        return mInstrumentPosition;
    }

    public void setInstrumentPosition(int mInstrumentPosition) {
        this.mInstrumentPosition = mInstrumentPosition;
    }

    @Override
    public String getText() {
        return getTitle();
    }

}
