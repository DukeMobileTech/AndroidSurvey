package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "CriticalResponses")
public class CriticalResponse extends Model {
    private static final String TAG = "CriticalResponse";

    @Column(name = "QuestionIdentifier")
    private String mQuestionIdentifier;
    @Column(name = "OptionIdentifier")
    private String mOptionIdentifier;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "InstructionId")
    private Long mInstructionId;
    @Column(name = "Deleted")
    private boolean mDeleted;

    public CriticalResponse() {
        super();
    }

    public static CriticalResponse findByRemoteId(Long id) {
        return new Select().from(CriticalResponse.class).where("RemoteId = ?", id).executeSingle();
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public void setInstructionId(Long id) {
        mInstructionId = id;
    }

    public void setQuestionIdentifier(String identifier) {
        mQuestionIdentifier = identifier;
    }

    public void setOptionIdentifier(String identifier) {
        mOptionIdentifier = identifier;
    }

    public boolean getDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public static List<CriticalResponse> getAll() {
        return new Select().from(CriticalResponse.class).where("Deleted = 0").execute();
    }

    public String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }

    public String getOptionIdentifier() {
        return mOptionIdentifier;
    }

    public Long getInstructionId() {
        return mInstructionId;
    }
}