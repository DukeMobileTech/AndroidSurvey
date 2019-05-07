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

@android.arch.persistence.room.Entity(tableName = "NextQuestions",
        foreignKeys = @ForeignKey(entity = Question.class,
                parentColumns = "QuestionIdentifier", childColumns = "QuestionIdentifier", onDelete = CASCADE))
public class NextQuestion implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId")
    private Long mRemoteId;
    @SerializedName("question_identifier")
    @ColumnInfo(name = "QuestionIdentifier", index = true)
    private String mQuestionIdentifier;
    @SerializedName("option_identifier")
    @ColumnInfo(name = "OptionIdentifier")
    private String mOptionIdentifier;
    @SerializedName("next_question_identifier")
    @ColumnInfo(name = "NextQuestionIdentifier")
    private String mNextQuestionIdentifier;
    @SerializedName("question_id")
    @ColumnInfo(name = "QuestionId")
    private Long mQuestionId;
    @SerializedName("instrument_id")
    @ColumnInfo(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("complete_survey")
    @ColumnInfo(name = "CompleteSurvey")
    private boolean mCompleteSurvey;
    @SerializedName("value")
    @ColumnInfo(name = "Value")
    private String mValue;

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

    public String getOptionIdentifier() {
        return mOptionIdentifier;
    }

    public void setOptionIdentifier(String mOptionIdentifier) {
        this.mOptionIdentifier = mOptionIdentifier;
    }

    public String getNextQuestionIdentifier() {
        return mNextQuestionIdentifier;
    }

    public void setNextQuestionIdentifier(String mNextQuestionIdentifier) {
        this.mNextQuestionIdentifier = mNextQuestionIdentifier;
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

    public boolean isCompleteSurvey() {
        return mCompleteSurvey;
    }

    public void setCompleteSurvey(boolean mCompleteSurvey) {
        this.mCompleteSurvey = mCompleteSurvey;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String mValue) {
        this.mValue = mValue;
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
