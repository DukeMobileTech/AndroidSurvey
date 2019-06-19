package org.adaptlab.chpir.android.survey.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.daos.BaseDao;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@android.arch.persistence.room.Entity(tableName = "OptionSetTranslations")
public class OptionSetTranslation implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId")
    private Long mRemoteId;
    @SerializedName("language")
    @ColumnInfo(name = "Language")
    private String mLanguage;
    @SerializedName("option_set_id")
    @ColumnInfo(name = "OptionSetRemoteId", index = true)
    private Long mOptionSetRemoteId;
    @SerializedName("option_id")
    @ColumnInfo(name = "OptionRemoteId", index = true)
    private Long mOptionRemoteId;
    @SerializedName("option_translation_id")
    @ColumnInfo(name = "OptionTranslationRemoteId", index = true)
    private Long mOptionTranslationRemoteId;

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long id) {
        this.mRemoteId = id;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        this.mLanguage = language;
    }

    public Long getOptionSetRemoteId() {
        return mOptionSetRemoteId;
    }

    public void setOptionSetRemoteId(Long id) {
        this.mOptionSetRemoteId = id;
    }

    public Long getOptionRemoteId() {
        return mOptionRemoteId;
    }

    public void setOptionRemoteId(Long mOptionRemoteId) {
        this.mOptionRemoteId = mOptionRemoteId;
    }

    public Long getOptionTranslationRemoteId() {
        return mOptionTranslationRemoteId;
    }

    public void setOptionTranslationRemoteId(Long mOptionTranslationRemoteId) {
        this.mOptionTranslationRemoteId = mOptionTranslationRemoteId;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<OptionSetTranslation>>() {
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

    @Override
    public void setDeleted(boolean deleted) {

    }
}
