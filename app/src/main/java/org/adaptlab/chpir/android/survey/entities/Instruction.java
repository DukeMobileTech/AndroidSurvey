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

@Entity(tableName = "Instructions")
public class Instruction implements SurveyEntity, Translatable {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("text")
    @ColumnInfo(name = "Text")
    private String mText;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @Ignore
    @SerializedName("instruction_translations")
    private List<InstructionTranslation> mInstructionTranslations;

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

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean mDeleted) {
        this.mDeleted = mDeleted;
    }

    public List<InstructionTranslation> getInstructionTranslations() {
        return mInstructionTranslations;
    }

    public void setInstructionTranslations(List<InstructionTranslation> mInstructionTranslations) {
        this.mInstructionTranslations = mInstructionTranslations;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<Instruction>>() {
        }.getType();
    }

    @Override
    public List<InstructionTranslation> getTranslations() {
        return mInstructionTranslations;
    }

    @Override
    public void save(BaseDao dao, List list) {
        dao.updateAll(list);
        dao.insertAll(list);
    }

}
