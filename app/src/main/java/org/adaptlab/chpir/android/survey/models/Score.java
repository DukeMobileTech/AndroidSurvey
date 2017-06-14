package org.adaptlab.chpir.android.survey.models;

import android.content.Context;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.scorers.Scorer;
import org.adaptlab.chpir.android.survey.scorers.ScorerFactory;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

@Table(name = "Scores")
public class Score extends SendModel {
    private static final String TAG = "Score";

    @Column(name = "UUID")
    private String mUUID;
    @Column(name = "SentToRemote")
    private boolean mSent;
    @Column(name = "SurveyUUID")
    private String mSurveyUUID;
    @Column(name = "ScoreScheme")
    private ScoreScheme mScoreScheme;
    @Column(name = "ScoreSum")
    private double mScoreSum;
    @Column(name = "SurveyIdentifier")
    private String mSurveyIdentifier;

    public Score() {
        super();
        mSent = false;
        mUUID = UUID.randomUUID().toString();
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
        return mScoreScheme;
    }

    public double getRawScoreSum() {
        return mScoreSum;
    }

    public void setScoreScheme(ScoreScheme scheme) {
        mScoreScheme = scheme;
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
        return new Select().from(RawScore.class).where("Score = ?", this.getId()).execute();
    }

    public static Score findBySurveyAndScheme(Survey survey, ScoreScheme scheme) {
        return new Select().from(Score.class).where("SurveyUUID = ? AND ScoreScheme = ?",
                survey.getUUID(), scheme.getId()).executeSingle();
    }

    public RawScore findRawScoreByScoreUnit(ScoreUnit unit) {
        return new Select().from(RawScore.class).where("ScoreUnit = ? AND Score = ?",
                unit.getId(), getId()).executeSingle();
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
}