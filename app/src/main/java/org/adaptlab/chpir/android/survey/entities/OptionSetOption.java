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

@android.arch.persistence.room.Entity(tableName = "OptionSetOptions")
public class OptionSetOption implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("number_in_question")
    @ColumnInfo(name = "Position")
    private int mPosition;
    @SerializedName("option_set_id")
    @ColumnInfo(name = "OptionSetRemoteId", index = true)
    private Long mOptionSetRemoteId;
    @SerializedName("option_id")
    @ColumnInfo(name = "OptionRemoteId", index = true)
    private Long mOptionRemoteId;
    @SerializedName("special")
    @ColumnInfo(name = "Special")
    private boolean mSpecial;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("is_exclusive")
    @ColumnInfo(name = "Exclusive")
    private boolean mExclusive;

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long mRemoteId) {
        this.mRemoteId = mRemoteId;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public Long getOptionSetRemoteId() {
        return mOptionSetRemoteId;
    }

    public void setOptionSetRemoteId(Long mOptionSetRemoteId) {
        this.mOptionSetRemoteId = mOptionSetRemoteId;
    }

    public Long getOptionRemoteId() {
        return mOptionRemoteId;
    }

    public void setOptionRemoteId(Long mOptionRemoteId) {
        this.mOptionRemoteId = mOptionRemoteId;
    }

    public boolean isSpecial() {
        return mSpecial;
    }

    public void setSpecial(boolean mSpecial) {
        this.mSpecial = mSpecial;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean mDeleted) {
        this.mDeleted = mDeleted;
    }

    public boolean isExclusive() {
        return mExclusive;
    }

    public void setExclusive(boolean mExclusive) {
        this.mExclusive = mExclusive;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<OptionSetOption>>() {
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
