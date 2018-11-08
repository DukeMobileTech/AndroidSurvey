package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "MultipleSkips")
public class MultipleSkip extends ReceiveModel {
    private static final String TAG = "MultipleSkip";

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "QuestionIdentifier")
    private String mQuestionIdentifier;
    @Column(name = "OptionIdentifier")
    private String mOptionIdentifier;
    @Column(name = "SkipQuestionIdentifier")
    private String mSkipQuestionIdentifier;
    @Column(name = "RemoteQuestionId")
    private Long mRemoteQuestionId;
    @Column(name = "RemoteInstrumentId")
    private Long mRemoteInstrumentId;
    @Column(name = "Deleted")
    private boolean mDeleted;

    public MultipleSkip() {
        super();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            MultipleSkip skipQuestion = MultipleSkip.findByRemoteId(remoteId);
            if (skipQuestion == null) {
                skipQuestion = this;
            }
            skipQuestion.setRemoteId(remoteId);
            skipQuestion.setQuestionIdentifier(jsonObject.getString("question_identifier"));
            skipQuestion.setOptionIdentifier(jsonObject.getString("option_identifier"));
            skipQuestion.setSkipQuestionIdentifier(jsonObject.getString("skip_question_identifier"));
            skipQuestion.setRemoteQuestionId(jsonObject.getLong("question_id"));
            skipQuestion.setRemoteInstrumentId(jsonObject.getLong("instrument_id"));
            if (jsonObject.isNull("deleted_at")) {
                skipQuestion.setDeleted(false);
            } else {
                skipQuestion.setDeleted(true);
            }
            skipQuestion.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static MultipleSkip findByRemoteId(Long id) {
        return new Select().from(MultipleSkip.class).where("RemoteId = ?", id).executeSingle();
    }

    public Question getSkipQuestion() {
        return Question.findByQuestionIdentifier(mSkipQuestionIdentifier);
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    void setQuestionIdentifier(String id) {
        mQuestionIdentifier = id;
    }

    void setOptionIdentifier(String id) {
        mOptionIdentifier = id;
    }

    String getOptionIdentifier() {
        return mOptionIdentifier;
    }

    void setSkipQuestionIdentifier(String id) {
        mSkipQuestionIdentifier = id;
    }

    String getSkipQuestionIdentifier() {
        return mSkipQuestionIdentifier;
    }

    private void setRemoteQuestionId(Long id) {
        mRemoteQuestionId = id;
    }

    void setRemoteInstrumentId(Long id) {
        mRemoteInstrumentId = id;
    }

    Long getRemoteInstrumentId() {
        return mRemoteInstrumentId;
    }

    void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    boolean getDeleted() {
        return mDeleted;
    }
}
