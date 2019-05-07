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

@android.arch.persistence.room.Entity(tableName = "CriticalResponses",
        foreignKeys = @ForeignKey(entity = Question.class,
                parentColumns = "QuestionIdentifier", childColumns = "QuestionIdentifier", onDelete = CASCADE))
public class CriticalResponse implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId")
    private Long mRemoteId;
    @SerializedName("instruction_id")
    @ColumnInfo(name = "InstructionId")
    private Long mInstructionId;
    @SerializedName("question_identifier")
    @ColumnInfo(name = "QuestionIdentifier", index = true)
    private String mQuestionIdentifier;
    @SerializedName("option_identifier")
    @ColumnInfo(name = "OptionIdentifier")
    private String mOptionIdentifier;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean mDeleted) {
        this.mDeleted = mDeleted;
    }

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long mRemoteId) {
        this.mRemoteId = mRemoteId;
    }

    public Long getInstructionId() {
        return mInstructionId;
    }

    public void setInstructionId(Long mInstructionId) {
        this.mInstructionId = mInstructionId;
    }

    public String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }

    public void setQuestionIdentifier(String mQuestionIdentifier) {
        this.mQuestionIdentifier = mQuestionIdentifier;
    }

    public String getOptionIdentifier() {
        return mOptionIdentifier;
    }

    public void setOptionIdentifier(String mOptionIdentifier) {
        this.mOptionIdentifier = mOptionIdentifier;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<CriticalResponse>>() {
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
