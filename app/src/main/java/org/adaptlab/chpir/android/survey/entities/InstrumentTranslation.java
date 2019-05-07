package org.adaptlab.chpir.android.survey.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.daos.BaseDao;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@android.arch.persistence.room.Entity(tableName = "InstrumentTranslations",
        foreignKeys = @ForeignKey(entity = Instrument.class,
                parentColumns = "RemoteId", childColumns = "InstrumentRemoteId", onDelete = CASCADE))
public class InstrumentTranslation implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId")
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
    @SerializedName("instrument_id")
    @ColumnInfo(name = "InstrumentRemoteId", index = true)
    private Long mInstrumentRemoteId;

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long id) {
        this.mRemoteId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
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

    public Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    public void setInstrumentRemoteId(Long instrumentRemoteId) {
        this.mInstrumentRemoteId = instrumentRemoteId;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<InstrumentTranslation>>() {
        }.getType();
    }

    @Override
    public List<? extends SurveyEntity> getTranslations() {
        return null;
    }

    @Override
    public void save(BaseDao dao, List list) {
        dao.updateAll(list);
        dao.insertAll(list);
    }
}
