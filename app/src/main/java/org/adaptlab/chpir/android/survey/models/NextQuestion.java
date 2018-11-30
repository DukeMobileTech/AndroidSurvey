package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "NextQuestions")
public class NextQuestion extends ReceiveModel {
    public static final String COMPLETE_SURVEY = "COMPLETE_SURVEY";
    private static final String TAG = "NextQuestion";

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "QuestionIdentifier")
    private String mQuestionIdentifier;
    @Column(name = "OptionIdentifier")
    private String mOptionIdentifier;
    @Column(name = "NextQuestionIdentifier")
    private String mNextQuestionIdentifier;
    @Column(name = "RemoteQuestionId")
    private Long mRemoteQuestionId;
    @Column(name = "RemoteInstrumentId")
    private Long mRemoteInstrumentId;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "Value")
    private String mValue;
    @Column(name = "CompleteSurvey")
    private boolean mCompleteSurvey;

    public NextQuestion() {
        super();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            NextQuestion nextQuestion = NextQuestion.findByRemoteId(remoteId);
            if (nextQuestion == null) {
                nextQuestion = this;
            }
            nextQuestion.setRemoteId(remoteId);
            nextQuestion.setQuestionIdentifier(jsonObject.getString("question_identifier"));
            nextQuestion.setOptionIdentifier(jsonObject.getString("option_identifier"));
            nextQuestion.setNextQuestionIdentifier(jsonObject.getString("next_question_identifier"));
            nextQuestion.setRemoteQuestionId(jsonObject.getLong("question_id"));
            nextQuestion.setRemoteInstrumentId(jsonObject.getLong("instrument_id"));
            nextQuestion.setCompleteSurvey(jsonObject.optBoolean("complete_survey", false));
            if (jsonObject.isNull("deleted_at")) {
                nextQuestion.setDeleted(false);
            } else {
                nextQuestion.setDeleted(true);
            }
            nextQuestion.setValue(jsonObject.optString("value", null));
            nextQuestion.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static List<NextQuestion> getAll(Long instrumentId) {
        return new Select().from(NextQuestion.class).where(
                "RemoteInstrumentId = ? AND Deleted != ?", instrumentId, 1).execute();
    }

    public static NextQuestion findByRemoteId(Long id) {
        return new Select().from(NextQuestion.class).where("RemoteId = ?", id).executeSingle();
    }

    public String getNextQuestionIdentifier() {
        return mNextQuestionIdentifier;
    }

    private boolean completeSurvey() {
        return mCompleteSurvey;
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

    public String getOptionIdentifier() {
        return mOptionIdentifier;
    }

    void setNextQuestionIdentifier(String id) {
        mNextQuestionIdentifier = id;
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

    private void setValue(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    private void setCompleteSurvey(boolean completeSurvey) {
        mCompleteSurvey = completeSurvey;
    }

    public String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }

    public String getNextQuestionString() {
        if (completeSurvey()) {
            return COMPLETE_SURVEY;
        } else {
            return getNextQuestionIdentifier();
        }
    }
}
