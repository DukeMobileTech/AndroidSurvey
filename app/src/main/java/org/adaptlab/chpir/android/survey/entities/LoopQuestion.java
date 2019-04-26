package org.adaptlab.chpir.android.survey.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "LoopQuestions",
        foreignKeys = @ForeignKey(entity = Question.class,
                parentColumns = "RemoteId", childColumns = "QuestionRemoteId", onDelete = CASCADE))
public class LoopQuestion {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId")
    private Long mRemoteId;
    @SerializedName("parent")
    @ColumnInfo(name = "Parent")
    private String mParent;
    @SerializedName("looped")
    @ColumnInfo(name = "Looped")
    private String mLooped;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("option_indices")
    @ColumnInfo(name = "OptionIndices")
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

}
