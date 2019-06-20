package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

@Table(name = "RawScores")
public class RawScore extends SendModel {
    private static final String TAG = "Score";

    @Column(name = "UUID")
    private String mUUID;
    @Column(name = "SentToRemote")
    private boolean mSent;
    @Column(name = "ScoreUnit")
    private ScoreUnit mScoreUnit;
    @Column(name = "ScoreUUID")
    private String mScoreUUID;
    @Column(name = "Value")
    private double mValue;

    public RawScore() {
        super();
        mSent = false;
        mUUID = UUID.randomUUID().toString();
    }

    public static RawScore findByScoreUnitAndScore(ScoreUnit scoreUnit, Score score) {
        return new Select().from(RawScore.class).where("ScoreUnit = ? AND ScoreUUID = ?",
                scoreUnit.getId(), score.getUUID()).executeSingle();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uuid", mUUID);
            jsonObject.put("score_uuid", mScoreUUID);
            jsonObject.put("score_unit_id", mScoreUnit.getRemoteId());
            jsonObject.put("value", mValue);
            json.put("raw_score", jsonObject);
        } catch (JSONException je) {
            Log.e(TAG, "JSON exception", je);
        }
        return json;
    }

    @Override
    public boolean isSent() {
        return mSent;
    }

    @Override
    public boolean readyToSend() {
        return (getScore() != null) && getScore().readyToSend();
    }

    @Override
    public void setAsSent(Context context) {
        mSent = true;
        this.save();
        // TODO: 6/30/17 Decide how long to keep the scores around
//        this.delete();
//        getScore().deleteIfComplete();
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    public double getValue() {
        return mValue;
    }

    public void setValue(double value) {
        mValue = value;
    }

    public Score getScore() {
        if (mScoreUUID == null) return null;
        return new Select().from(Score.class).where("UUID = ?", mScoreUUID).executeSingle();
    }

    public void setScore(Score score) {
        mScoreUUID = score.getUUID();
    }

    public ScoreUnit getScoreUnit() {
        return mScoreUnit;
    }

    public void setScoreUnit(ScoreUnit scoreUnit) {
        mScoreUnit = scoreUnit;
    }

    public double getWeightedScore() {
        return getValue() * getScoreUnit().getWeight();
    }
}
