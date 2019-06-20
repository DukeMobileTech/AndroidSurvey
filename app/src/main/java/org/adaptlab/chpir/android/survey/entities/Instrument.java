package org.adaptlab.chpir.android.survey.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.daos.BaseDao;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "Instruments")
public class Instrument implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("title")
    @ColumnInfo(name = "Title")
    private String mTitle;
    @SerializedName("language")
    @ColumnInfo(name = "Language")
    private String mLanguage;
    @SerializedName("alignment")
    @ColumnInfo(name = "Alignment")
    private String mAlignment;
    @SerializedName("current_version_number")
    @ColumnInfo(name = "VersionNumber")
    private int mVersionNumber;
    @SerializedName("question_count")
    @ColumnInfo(name = "QuestionCount")
    private int mQuestionCount;
    @SerializedName("project_id")
    @ColumnInfo(name = "ProjectId")
    private Long mProjectId;
    @SerializedName("published")
    @ColumnInfo(name = "Published")
    private boolean mPublished;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @ColumnInfo(name = "Loaded")
    private boolean mLoaded;
    @Ignore
    // This field is not saved into the database, but is required by Gson to correctly deserialize nested translations
    @SerializedName("instrument_translations")
    private List<InstrumentTranslation> mInstrumentTranslations;

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

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        this.mLanguage = language;
    }

    public String getAlignment() {
        return mAlignment;
    }

    public void setAlignment(String alignment) {
        this.mAlignment = alignment;
    }

    public int getVersionNumber() {
        return mVersionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.mVersionNumber = versionNumber;
    }

    public int getQuestionCount() {
        return mQuestionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.mQuestionCount = questionCount;
    }

    public Long getProjectId() {
        return mProjectId;
    }

    public void setProjectId(Long projectId) {
        this.mProjectId = projectId;
    }

    public boolean isPublished() {
        return mPublished;
    }

    public void setPublished(boolean published) {
        this.mPublished = published;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    public boolean isLoaded() {
        return mLoaded;
    }

    public void setLoaded(boolean loaded) {
        this.mLoaded = loaded;
    }

    public List<InstrumentTranslation> getInstrumentTranslations() {
        return mInstrumentTranslations;
    }

    public void setInstrumentTranslations(List<InstrumentTranslation> translations) {
        this.mInstrumentTranslations = translations;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<Instrument>>() {
        }.getType();
    }

    @Override
    public List<InstrumentTranslation> getTranslations() {
        return mInstrumentTranslations;
    }

    @Override
    public void save(BaseDao dao, List list) {
        dao.updateAll(list);
        dao.insertAll(list);
    }
}
