package org.adaptlab.chpir.android.survey.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.daos.BaseDao;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@android.arch.persistence.room.Entity(tableName = "Sections", indices = {@Index(name = "sections_index", value = {"RemoteId"}, unique = true)},
        foreignKeys = @ForeignKey(entity = Instrument.class, parentColumns = "RemoteId",
                childColumns = "InstrumentRemoteId", onDelete = CASCADE))
public class Section implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId")
    private Long mRemoteId;
    @SerializedName("title")
    @ColumnInfo(name = "Title")
    private String mTitle;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("instrument_id")
    @ColumnInfo(name = "InstrumentRemoteId", index = true)
    private Long mInstrumentRemoteId;
    @Ignore
    @SerializedName("section_translations")
    private List<SectionTranslation> mSectionTranslations;

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long mRemoteId) {
        this.mRemoteId = mRemoteId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean mDeleted) {
        this.mDeleted = mDeleted;
    }

    public Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    public void setInstrumentRemoteId(Long mInstrumentRemoteId) {
        this.mInstrumentRemoteId = mInstrumentRemoteId;
    }

    public List<SectionTranslation> getSectionTranslations() {
        return mSectionTranslations;
    }

    public void setSectionTranslations(List<SectionTranslation> mSectionTranslations) {
        this.mSectionTranslations = mSectionTranslations;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<Section>>() {
        }.getType();
    }

    @Override
    public List<SectionTranslation> getTranslations() {
        return mSectionTranslations;
    }

    @Override
    public void save(BaseDao dao, List list) {
        dao.updateAll(list);
        dao.insertAll(list);
    }
}
