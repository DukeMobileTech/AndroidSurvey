package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.activeandroid.Cache;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.scorers.Scorer;
import org.adaptlab.chpir.android.survey.scorers.ScorerFactory;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

@Table(name = "Scores", id = BaseColumns._ID)
public class Score extends SendModel {
    private static final String TAG = "Score";

    @Column(name = "UUID")
    private String mUUID;
    @Column(name = "SentToRemote")
    private boolean mSent;
    @Column(name = "SurveyUUID")
    private String mSurveyUUID;
    @Column(name = "ScoreSchemeRemoteId")
    private Long mScoreSchemeRemoteId;
    @Column(name = "ScoreSum")
    private double mScoreSum;
    @Column(name = "SurveyIdentifier")
    private String mSurveyIdentifier;

    public Score() {
        super();
        mSent = false;
        mUUID = UUID.randomUUID().toString();
    }

    public static Score findByUUID(String uuid) {
        return new Select().from(Score.class).where("UUID = ?", uuid).executeSingle();
    }

    public static Cursor getCursor() {
        From query = new Select("Scores.*").from(Score.class);
        return Cache.openDatabase().rawQuery(query.toSql(), query.getArguments());
    }

    public String getUUID() {
        return mUUID;
    }

    public void setSurvey(Survey survey) {
        mSurveyUUID = survey.getUUID();
    }

    public Survey getSurvey() {
        return Survey.findByUUID(getSurveyUUID());
    }

    public String getSurveyUUID() {
        return mSurveyUUID;
    }

    public ScoreScheme getScoreScheme() {
        return new Select().from(ScoreScheme.class).where("RemoteId = ?", mScoreSchemeRemoteId).executeSingle();
    }

    public double getRawScoreSum() {
        return mScoreSum;
    }

    public void setScoreScheme(ScoreScheme scheme) {
        mScoreSchemeRemoteId = scheme.getRemoteId();
    }

    public void setSurveyIdentifier(String identifier) {
        mSurveyIdentifier = identifier;
    }

    public String getSurveyIdentifier() {
        return mSurveyIdentifier;
    }

    public double getWeightedScoreSum() {
        double sum = 0;
        for (RawScore rawScore : rawScores()) {
            sum += rawScore.getWeightedScore();
        }
        return sum;
    }

    private void setRawScoreSum() {
        mScoreSum = 0;
        for (RawScore rawScore : rawScores()) {
            mScoreSum = mScoreSum + rawScore.getValue();
        }
    }

    public List<RawScore> rawScores() {
        return new Select().from(RawScore.class).where("ScoreUUID = ?", mUUID).execute();
    }

    public static Score findBySurveyAndScheme(Survey survey, ScoreScheme scheme) {
        return new Select().from(Score.class).where("SurveyUUID = ? AND ScoreSchemeRemoteId = ?",
                survey.getUUID(), scheme.getRemoteId()).executeSingle();
    }

    public RawScore findRawScoreByScoreUnit(ScoreUnit unit) {
        return new Select().from(RawScore.class).where("ScoreUnit = ? AND ScoreUUID = ?",
                unit.getId(), mUUID).executeSingle();
    }

    public void score() {
        for (ScoreUnit unit : getScoreScheme().scoreUnits()) {
            Scorer scorer = ScorerFactory.createScorer(unit);
            RawScore rawScore = RawScore.findByScoreUnitAndScore(unit, this);
            if (rawScore == null) {
                rawScore = new RawScore();
                rawScore.setScore(this);
                rawScore.setScoreUnit(unit);
            }
            rawScore.setValue(scorer.score(unit, getSurvey()));
            rawScore.save();
        }
        setRawScoreSum();
        save();
    }

    public static List<Score> getAll() {
        return new Select().from(Score.class).execute();
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

    @Override
    public String getPrimaryKey() {
        return BaseColumns._ID;
    }
}