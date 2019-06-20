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

@Entity(tableName = "LoopQuestions")
public class LoopQuestion implements SurveyEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("parent")
    @ColumnInfo(name = "Parent", index = true) // Question identifier that triggers loop
    private String mParent;
    @SerializedName("looped")
    @ColumnInfo(name = "Looped", index = true) // Question identifier that is looped
    private String mLooped;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("option_indices")
    @ColumnInfo(name = "OptionIndices")
    // Determines how many times this question (Looped) is looped
    private String mOptionIndices;
    @SerializedName("same_display")
    @ColumnInfo(name = "SameDisplay")
    private boolean mSameDisplay;
    @SerializedName("replacement_text")
    @ColumnInfo(name = "TextToReplace")
    private String mTextToReplace;
    @SerializedName("instrument_question_id")
    @ColumnInfo(name = "QuestionRemoteId", index = true)
    private Long mQuestionRemoteId;

    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Long remoteId) {
        this.mRemoteId = remoteId;
    }

    public String getParent() {
        return mParent;
    }

    public void setParent(String parent) {
        this.mParent = parent;
    }

    public String getLooped() {
        return mLooped;
    }

    public void setLooped(String looped) {
        this.mLooped = looped;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    public String getOptionIndices() {
        return mOptionIndices;
    }

    public void setOptionIndices(String mOptionIndices) {
        this.mOptionIndices = mOptionIndices;
    }

    public boolean isSameDisplay() {
        return mSameDisplay;
    }

    public void setSameDisplay(boolean mSameDisplay) {
        this.mSameDisplay = mSameDisplay;
    }

    public String getTextToReplace() {
        return mTextToReplace;
    }

    public void setTextToReplace(String mTextToReplace) {
        this.mTextToReplace = mTextToReplace;
    }

    public Long getQuestionRemoteId() {
        return mQuestionRemoteId;
    }

    public void setQuestionRemoteId(Long mQuestionRemoteId) {
        this.mQuestionRemoteId = mQuestionRemoteId;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<LoopQuestion>>() {
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
