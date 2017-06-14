package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.models.Question.QuestionType;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static org.adaptlab.chpir.android.survey.models.Question.validQuestionType;

@Table(name = "ScoreUnits")
public class ScoreUnit extends ReceiveModel {
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "ScoreScheme")
    private ScoreScheme mScoreScheme;
    @Column(name = "QuestionType")
    private QuestionType mQuestionType;
    @Column(name = "Max")
    private double mMax;
    @Column(name = "Min")
    private double mMin;
    @Column(name = "Weight")
    private double mWeight;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "ScoreType")
    private ScoreType mScoreType;
    @Column(name = "QuestionNumberInInstrument")
    private int mQuestionNumberInInstrument;

    private final static String TAG = "ScoreUnit";

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            ScoreUnit unit = ScoreUnit.findByRemoteId(remoteId);
            if (unit == null) {
                unit = this;
            }
            unit.setRemoteId(remoteId);
            unit.setScoreScheme(ScoreScheme.findByRemoteId(jsonObject.getLong("score_scheme_id")));
            unit.setQuestionType(jsonObject.getString("question_type"));
            unit.setScoreType(jsonObject.getString("score_type"));
            unit.setMin(jsonObject.getDouble("min"));
            unit.setMax(jsonObject.getDouble("max"));
            unit.setWeight(jsonObject.getDouble("weight"));
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

    public static ScoreUnit findByRemoteId(Long remoteId) {
        return new Select().from(ScoreUnit.class).where("RemoteId = ?", remoteId).executeSingle();
    }

    public List<ScoreUnitQuestion> scoreUnitQuestions() {
        return new Select().from(ScoreUnitQuestion.class).where("ScoreUnit = ?", getId()).execute();
    }

    public List<Question> questions() {
        return new Select().from(Question.class).innerJoin(ScoreUnitQuestion.class)
                .on("Questions.Id=ScoreUnitQuestions.Question")
                .where("ScoreUnitQuestions.ScoreUnit = ?", getId())
                .execute();
    }

    public Question getOrderingQuestion() {
        return new Select().from(Question.class).innerJoin(ScoreUnitQuestion.class)
                .on("Questions.Id=ScoreUnitQuestions.Question")
                .where("ScoreUnitQuestions.ScoreUnit = ?", getId())
                .orderBy("Questions.NumberInInstrument ASC")
                .executeSingle();
    }

    public List<OptionScore> optionScores() {
        return new Select().from(OptionScore.class).where("ScoreUnit = ?", getId()).execute();
    }

    public OptionScore getOptionScoreByOption(Option option) {
        return new Select().from(OptionScore.class).where("Option = ? AND ScoreUnit = ?",
                option.getId(), getId()).executeSingle();
    }

    public OptionScore getOptionScoreByLabel(GridLabel label) {
        return new Select().from(OptionScore.class).where("Label = ? AND ScoreUnit = ?",
                label.getLabelText(), getId()).executeSingle();
    }

    public List<OptionScore> optionScoresGroupedByValue() {
        return new Select().from(OptionScore.class).where("ScoreUnit = ?", getId())
                .groupBy("Value").execute();
    }

    public List<OptionScore> optionScoresByValue(Double value) {
        return new Select().from(OptionScore.class).where("ScoreUnit = ? AND Value = ?",
                getId(), value).orderBy("Option").execute();
    }

    public static List<ScoreUnit> getAll() {
        return new Select().from(ScoreUnit.class).execute();
    }

    public String questionIdentifiers() {
        StringBuilder idsBuilder = new StringBuilder();
        List<ScoreUnitQuestion> suq = scoreUnitQuestions();
        for (int k = 0; k < suq.size(); k++) {
            idsBuilder.append(suq.get(k).getQuestion().getQuestionIdentifier());
            if (k != suq.size() - 1) {
                idsBuilder.append(", ");
            }
        }
        return idsBuilder.toString().trim();
    }

    private void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    private void setScoreScheme(ScoreScheme scoreScheme) {
        mScoreScheme = scoreScheme;
    }

    private void setQuestionType(String questionType) {
        if (validQuestionType(questionType)) {
            mQuestionType = QuestionType.valueOf(questionType);
        } else {
            Log.wtf(TAG, "Received invalid question type: " + questionType);
        }
    }

    private void setScoreType(String scoreType) {
        if (validScoreType(scoreType)) {
            mScoreType = ScoreType.valueOf(scoreType);
        } else {
            Log.wtf(TAG, "Received invalid score type: " + scoreType);
        }
    }

    public static boolean validScoreType(String scoreType) {
        for (ScoreType type : ScoreType.values()) {
            if (type.name().equals(scoreType)) {
                return true;
            }
        }
        return false;
    }

    public ScoreType getScoreType() {
        return mScoreType;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    private void setWeight(double weight) {
        mWeight = weight;
    }

    private void setMin(double min) {
        mMin = min;
    }

    private void setMax(double max) {
        mMax = max;
    }

    public double getMin() {
        return mMin;
    }

    public double getMax() {
        return mMax;
    }

    public double getWeight() {
        return mWeight;
    }

    public void setQuestionNumberInInstrument(int pos) {
        mQuestionNumberInInstrument = pos;
    }

    public int getQuestionNumberInInstrument() {
        return mQuestionNumberInInstrument;
    }

    public enum ScoreType {
        single_select, multiple_select, multiple_select_sum, range, simple_search
    }

}