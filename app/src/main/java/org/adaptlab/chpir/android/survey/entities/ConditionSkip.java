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

@Entity(tableName = "ConditionSkips")
public class ConditionSkip implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("question_identifier")
    @ColumnInfo(name = "QuestionIdentifier", index = true)
    private String mQuestionIdentifier;
    @SerializedName("next_question_identifier")
    @ColumnInfo(name = "NextQuestionIdentifier", index = true)
    private String mNextQuestionIdentifier;
    @SerializedName("question_id")
    @ColumnInfo(name = "QuestionId")
    private Long mQuestionId;
    @SerializedName("instrument_id")
    @ColumnInfo(name = "InstrumentRemoteId", index = true)
    private Long mInstrumentRemoteId;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("question_identifiers")
    @ColumnInfo(name = "QuestionIdentifiers")
    private String mQuestionIdentifiers;
    @SerializedName("option_ids")
    @ColumnInfo(name = "OptionIds")
    private String mOptionIds;
    @SerializedName("values")
    @ColumnInfo(name = "Entries")
    private String mValues;
    @SerializedName("value_operators")
    @ColumnInfo(name = "ValueOperators")
    private String mValueOperators;
    //TODO Remove columns below that are not in use
    @ColumnInfo(name = "Condition")
    private String mCondition;
    @ColumnInfo(name = "ConditionOptionIdentifier", index = true)
    private String mConditionOptionIdentifier;
    @ColumnInfo(name = "OptionIdentifier", index = true)
    private String mOptionIdentifier;
    @ColumnInfo(name = "ConditionQuestionIdentifier", index = true)
    private String mConditionQuestionIdentifier;

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

    public String getConditionQuestionIdentifier() {
        return mConditionQuestionIdentifier;
    }

    public void setConditionQuestionIdentifier(String mConditionQuestionIdentifier) {
        this.mConditionQuestionIdentifier = mConditionQuestionIdentifier;
    }

    public String getConditionOptionIdentifier() {
        return mConditionOptionIdentifier;
    }

    public void setConditionOptionIdentifier(String mConditionOptionIdentifier) {
        this.mConditionOptionIdentifier = mConditionOptionIdentifier;
    }

    public String getCondition() {
        return mCondition;
    }

    public void setCondition(String mCondition) {
        this.mCondition = mCondition;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<ConditionSkip>>() {
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

    public String getQuestionIdentifiers() {
        return mQuestionIdentifiers;
    }

    public void setQuestionIdentifiers(String mQuestionIdentifiers) {
        this.mQuestionIdentifiers = mQuestionIdentifiers;
    }

    public String getOptionIds() {
        return mOptionIds;
    }

    public void setOptionIds(String mOptionIds) {
        this.mOptionIds = mOptionIds;
    }

    public String getValues() {
        return mValues;
    }

    public void setValues(String mValues) {
        this.mValues = mValues;
    }

    public String getValueOperators() {
        return mValueOperators;
    }

    public void setValueOperators(String mValueOperators) {
        this.mValueOperators = mValueOperators;
    }
}
