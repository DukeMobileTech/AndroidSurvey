package org.adaptlab.chpir.android.survey.models;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.activeandroid.Cache;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.scorers.Scorer;
import org.adaptlab.chpir.android.survey.scorers.ScorerFactory;
import org.json.JSONException;
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
    @Column(name = "Complete")
    private boolean mComplete;

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
        setComplete(true);
        save();
    }

    public static List<Score> getAll() {
        return new Select().from(Score.class).execute();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("device_uuid", AppUtil.getAdminSettingsInstance().getDeviceIdentifier());
            jsonObject.put("device_label", AppUtil.getAdminSettingsInstance().getDeviceLabel());
            jsonObject.put("uuid", mUUID);
            jsonObject.put("survey_uuid", mSurveyUUID);
            jsonObject.put("score_scheme_id", mScoreSchemeRemoteId);
            jsonObject.put("score_sum", mScoreSum);
            json.put("score", jsonObject);
        } catch (JSONException je) {
            Log.e(TAG, "JSON exception", je);
        }
        return json;
    }

    private void setComplete(boolean status) {
        mComplete = status;
    }

    public void deleteIfComplete() {
        if (this.rawScores().size() == 0) {
            this.delete();
        }
    }

    @Override
    public boolean isSent() {
        return mSent;
    }

    @Override
    public boolean readyToSend() {
        return mComplete;
    }

    @Override
    public void setAsSent(Context context) {
        mSent = true;
        this.save();

        EventLog eventLog = new EventLog(EventLog.EventType.SENT_SURVEY, context);
        eventLog.setInstrumentRemoteId(getScoreScheme().getInstrument().getRemoteId());
        eventLog.setSurveyIdentifier(mSurveyIdentifier);
        eventLog.save();

        Resources r = context.getResources();

        Notification notification = new NotificationCompat.Builder(context)
                .setTicker(r.getString(R.string.app_name))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(r.getString(R.string.app_name))
                .setContentText(eventLog.getLogMessage(context))
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(eventLog.getLogMessage(context), 1, notification);
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public String getPrimaryKey() {
        return BaseColumns._ID;
    }
}