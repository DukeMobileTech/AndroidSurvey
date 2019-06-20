package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "ScoreUnitQuestions")
public class ScoreUnitQuestion extends ReceiveModel {
    private final static String TAG = "ScoreUnitQuestion";
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "ScoreUnit")
    private ScoreUnit mScoreUnit;
    @Column(name = "Question")
    private Question mQuestion;
    @Column(name = "Deleted")
    private boolean mDeleted;

    private static ScoreUnitQuestion findByRemoteId(Long remoteId) {
        return new Select().from(ScoreUnitQuestion.class).where("RemoteId = ?", remoteId).executeSingle();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            ScoreUnitQuestion unit = ScoreUnitQuestion.findByRemoteId(remoteId);
            if (unit == null) {
                unit = this;
            }
            unit.setRemoteId(remoteId);
            unit.setScoreUnit(ScoreUnit.findByRemoteId(jsonObject.getLong("score_unit_id")));
            unit.setQuestion(Question.findByRemoteId(jsonObject.getLong("question_id")));
            if (jsonObject.isNull("deleted_at")) {
                unit.setDeleted(false);
            } else {
                unit.setDeleted(true);
            }
            unit.save();
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error parsing object json", je);
        }
    }

    private void setScoreUnit(ScoreUnit scoreUnit) {
        mScoreUnit = scoreUnit;
    }

    private void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public Question getQuestion() {
        return mQuestion;
    }

    private void setQuestion(Question question) {
        mQuestion = question;
    }

}