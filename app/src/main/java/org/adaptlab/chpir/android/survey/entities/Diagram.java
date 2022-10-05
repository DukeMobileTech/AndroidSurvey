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

@Entity(tableName = "Diagrams")
public class Diagram implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("option_id")
    @ColumnInfo(name = "OptionId")
    private Long mOptionId;
    @SerializedName("collage_id")
    @ColumnInfo(name = "CollageId")
    private Long mCollageId;
    @SerializedName("position")
    @ColumnInfo(name = "Position")
    private Integer mPosition;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long mRemoteId) {
        this.mRemoteId = mRemoteId;
    }

    public Long getOptionId() {
        return mOptionId;
    }

    public void setOptionId(Long id) {
        this.mOptionId = id;
    }

    public Long getCollageId() {
        return mCollageId;
    }

    public void setCollageId(Long id) {
        this.mCollageId = id;
    }

    public Integer getPosition() {
        return mPosition;
    }

    public void setPosition(Integer id) {
        this.mPosition = id;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<Diagram>>() {
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
        mDeleted = deleted;
    }

}
