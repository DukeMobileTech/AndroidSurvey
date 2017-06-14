package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "OptionScores")
public class OptionScore extends ReceiveModel {
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "ScoreUnit")
    private ScoreUnit mScoreUnit;
    @Column(name = "Option")
    private Option mOption;
    @Column(name = "Value")
    private double mValue;
    @Column(name = "Label")
    private String mLabel;
    @Column(name = "Present")
    private boolean mPresent;
    @Column(name = "NextQuestion")
    private boolean mNextQuestion;
    @Column(name = "Deleted")
    private boolean mDeleted;

    private final static String TAG = "OptionScore";

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            OptionScore optionScore = OptionScore.findByRemoteId(remoteId);
            if (optionScore == null) {
                optionScore = this;
            }
            optionScore.setRemoteId(remoteId);
            optionScore.setScoreUnit(ScoreUnit.findByRemoteId(jsonObject.getLong("score_unit_id")));
            if (!jsonObject.isNull("option_id")) {
                optionScore.setOption(Option.findByRemoteId(jsonObject.optLong("option_id")));
            }
            optionScore.setValue(jsonObject.optDouble("value"));
            optionScore.setLabel(jsonObject.getString("label"));
            optionScore.setExists(jsonObject.optBoolean("exists"));
            optionScore.setNextQuestion(jsonObject.optBoolean("next_question", false));
            if (jsonObject.isNull("deleted_at")) {
                optionScore.setDeleted(false);
            } else {
                optionScore.setDeleted(true);
            }
            optionScore.save();
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error parsing object json", je);
        }
    }

    public double getValue() {
        return mValue;
    }

    public Option getOption() {
        return mOption;
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    public String getLabel() {
        return mLabel;
    }

    public boolean getExists() {
        return mPresent;
    }

    private static OptionScore findByRemoteId(Long remoteId) {
        return new Select().from(OptionScore.class).where("RemoteId = ?", remoteId).executeSingle();
    }

    private void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    private void setScoreUnit(ScoreUnit scoreUnit) {
        mScoreUnit = scoreUnit;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    private void setValue(double value) {
        mValue = value;
    }

    private void setOption(Option option) {
        mOption = option;
    }

    private void setLabel(String label) {
        mLabel = label;
    }

    private void setNextQuestion(boolean nextQuestion) {
        mNextQuestion = nextQuestion;
    }

    private void setExists(boolean exists) {
        mPresent = exists;
    }

}