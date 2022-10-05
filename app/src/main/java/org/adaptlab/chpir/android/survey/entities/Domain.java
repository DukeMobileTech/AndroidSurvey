package org.adaptlab.chpir.android.survey.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.daos.BaseDao;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "Domains")
public class Domain implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("title")
    @ColumnInfo(name = "Title")
    private String mTitle;
    @SerializedName("name")
    @ColumnInfo(name = "Name")
    private String mName;
    @SerializedName("weight")
    @ColumnInfo(name = "Weight")
    private Double mWeight;
    @SerializedName("score_scheme_id")
    @ColumnInfo(name = "ScoreSchemeRemoteId")
    private Long mScoreSchemeRemoteId;

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<Domain>>() {
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

    public boolean isDeleted() {
        return mDeleted;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

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

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public Double getWeight() {
        return mWeight;
    }

    public void setWeight(Double mWeight) {
        this.mWeight = mWeight;
    }

    public Long getScoreSchemeRemoteId() {
        return mScoreSchemeRemoteId;
    }

    public void setScoreSchemeRemoteId(Long mScoreSchemeRemoteId) {
        this.mScoreSchemeRemoteId = mScoreSchemeRemoteId;
    }
}
