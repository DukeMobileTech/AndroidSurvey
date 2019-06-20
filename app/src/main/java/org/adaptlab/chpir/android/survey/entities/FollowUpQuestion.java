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

@Entity(tableName = "FollowUpQuestions")
public class FollowUpQuestion implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("question_identifier")
    @ColumnInfo(name = "QuestionIdentifier", index = true)
    private String mQuestionIdentifier;
    @SerializedName("following_up_question_identifier")
    @ColumnInfo(name = "FollowingUpQuestionIdentifier", index = true)
    private String mFollowingUpQuestionIdentifier;
    @SerializedName("question_id")
    @ColumnInfo(name = "QuestionId", index = true)
    private Long mQuestionId;
    @SerializedName("instrument_id")
    @ColumnInfo(name = "InstrumentRemoteId", index = true)
    private Long mInstrumentRemoteId;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("position")
    @ColumnInfo(name = "Position")
    private Long mPosition;

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long mRemoteId) {
        this.mRemoteId = mRemoteId;
    }

    public String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }

    public void setQuestionIdentifier(String mQuestionIdentifier) {
        this.mQuestionIdentifier = mQuestionIdentifier;
    }

    public String getFollowingUpQuestionIdentifier() {
        return mFollowingUpQuestionIdentifier;
    }

    public void setFollowingUpQuestionIdentifier(String identifier) {
        this.mFollowingUpQuestionIdentifier = identifier;
    }

    public Long getQuestionId() {
        return mQuestionId;
    }

    public void setQuestionId(Long mQuestionId) {
        this.mQuestionId = mQuestionId;
    }

    public Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    public void setInstrumentRemoteId(Long mInstrumentRemoteId) {
        this.mInstrumentRemoteId = mInstrumentRemoteId;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean mDeleted) {
        this.mDeleted = mDeleted;
    }

    public Long getPosition() {
        return mPosition;
    }

    public void setPosition(Long position) {
        this.mPosition = position;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<FollowUpQuestion>>() {
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
