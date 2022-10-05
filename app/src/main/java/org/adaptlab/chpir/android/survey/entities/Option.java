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

@Entity(tableName = "Options")
public class Option implements SurveyEntity, Translatable {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("text")
    @ColumnInfo(name = "Text")
    private String mText;
    @SerializedName("identifier")
    @ColumnInfo(name = "Identifier", index = true)
    private String mIdentifier;
    @SerializedName("text_one")
    @ColumnInfo(name = "TextOne")
    private String mTextOne;
    @SerializedName("text_two")
    @ColumnInfo(name = "TextTwo")
    private String mTextTwo;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @Ignore
    @SerializedName("option_translations")
    private List<OptionTranslation> mOptionTranslations;

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long mRemoteId) {
        this.mRemoteId = mRemoteId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public void setIdentifier(String mIdentifier) {
        this.mIdentifier = mIdentifier;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean mDeleted) {
        this.mDeleted = mDeleted;
    }

    public String getTextOne() {
        return mTextOne;
    }

    public void setTextOne(String mText) {
        this.mTextOne = mText;
    }

    public String getTextTwo() {
        return mTextTwo;
    }

    public void setTextTwo(String mText) {
        this.mTextTwo = mText;
    }

    public List<OptionTranslation> getOptionTranslations() {
        return mOptionTranslations;
    }

    public void setOptionTranslations(List<OptionTranslation> mOptionTranslations) {
        this.mOptionTranslations = mOptionTranslations;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<Option>>() {
        }.getType();
    }

    @Override
    public List<OptionTranslation> getTranslations() {
        return mOptionTranslations;
    }

    @Override
    public void save(BaseDao dao, List list) {
        dao.updateAll(list);
        dao.insertAll(list);
    }
}
