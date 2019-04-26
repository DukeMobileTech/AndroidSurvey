package org.adaptlab.chpir.android.survey.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Entity(tableName = "Displays", indices = {@Index(name = "displays_id_index", value = {"RemoteId"}, unique = true)})
public class Display {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId")
    private Long mRemoteId;
    @SerializedName("title")
    @ColumnInfo(name = "Title")
    private String mTitle;
    @SerializedName("position")
    @ColumnInfo(name = "Position")
    private int mPosition;
    @SerializedName("question_count")
    @ColumnInfo(name = "QuestionCount")
    private int mQuestionCount;
    @SerializedName("instrument_id")
    @ColumnInfo(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("section_id")
    @ColumnInfo(name = "SectionId")
    private Long mSectionId;
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
}
