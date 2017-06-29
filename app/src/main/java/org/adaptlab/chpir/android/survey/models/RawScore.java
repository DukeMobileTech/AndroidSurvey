package org.adaptlab.chpir.android.survey.models;

import android.content.Context;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
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

    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public boolean isSent() {
        return mSent;
    }

    @Override
    public boolean readyToSend() {
        return false;
    }

    @Override
    public void setAsSent(Context context) {

    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    public static RawScore findByScoreUnitAndScore(ScoreUnit scoreUnit, Score score) {
        return new Select().from(RawScore.class).where("ScoreUnit = ? AND ScoreUUID = ?",
                scoreUnit.getId(), score.getUUID()).executeSingle();
    }

    public void setScore(Score score) {
        mScoreUUID = score.getUUID();
    }

    public void setScoreUnit(ScoreUnit scoreUnit) {
        mScoreUnit = scoreUnit;
    }

    public void setValue(double value) {
        mValue = value;
    }

    public double getValue() {
        return mValue;
    }

    public Score getScore() {
        return new Select().from(Score.class).where("UUID = ?", mScoreUUID).executeSingle();
    }

    public ScoreUnit getScoreUnit() {
        return mScoreUnit;
    }

    public double getWeightedScore() {
        return getValue() * getScoreUnit().getWeight();
    }
}
