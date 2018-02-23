package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "FollowUpQuestions")
public class FollowUpQuestion extends ReceiveModel {
    private static final String TAG = "FollowUpQuestion";

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "QuestionIdentifier")
    private String mQuestionIdentifier;
    @Column(name = "FollowingUpQuestionIdentifier")
    private String mFollowingUpQuestionIdentifier;
    @Column(name = "RemoteQuestionId")
    private Long mRemoteQuestionId;
    @Column(name = "RemoteInstrumentId")
    private Long mRemoteInstrumentId;
    @Column(name = "Position")
    private Long mPosition;

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            FollowUpQuestion followUpQuestion = FollowUpQuestion.findByRemoteId(remoteId);
            if (followUpQuestion == null) {
                followUpQuestion = this;
            }
            followUpQuestion.setRemoteId(remoteId);
            followUpQuestion.setQuestionIdentifier(jsonObject.getString("question_identifier"));
            followUpQuestion.setPosition(jsonObject.optLong("position"));
            followUpQuestion.setFollowingUpQuestionIdentifier(jsonObject.getString("following_up_question_identifier"));
            followUpQuestion.setRemoteQuestionId(jsonObject.getLong("question_id"));
            followUpQuestion.setRemoteInstrumentId(jsonObject.getLong("instrument_id"));
            followUpQuestion.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static FollowUpQuestion findByRemoteId(Long id) {
        return new Select().from(FollowUpQuestion.class).where("RemoteId = ?", id).executeSingle();
    }

    public Question getFollowingUpOnQuestion() {
        return new Select().from(Question.class).where("QuestionIdentifier = ? AND InstrumentRemoteId = ? AND Deleted != 1", mFollowingUpQuestionIdentifier, mRemoteInstrumentId).executeSingle();
    }

    public Question getFollowUpQuestion() {
        return new Select().from(Question.class).where("QuestionIdentifier = ? AND InstrumentRemoteId = ? AND Deleted != 1", mQuestionIdentifier, mRemoteInstrumentId).executeSingle();
    }

    private String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }

    private String getFollowingUpQuestionIdentifier() {
        return mFollowingUpQuestionIdentifier;
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    private void setQuestionIdentifier(String id) {
        mQuestionIdentifier = id;
    }

    private void setFollowingUpQuestionIdentifier(String id) {
        mFollowingUpQuestionIdentifier = id;
    }

    private void setRemoteQuestionId(Long id) {
        mRemoteQuestionId = id;
    }

    private void setRemoteInstrumentId(Long id) {
        mRemoteInstrumentId = id;
    }

    private void setPosition(long position) {
        mPosition = position;
    }

}
