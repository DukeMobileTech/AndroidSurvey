package org.adaptlab.chpir.android.survey.scorers;

import android.util.Log;

import org.adaptlab.chpir.android.survey.models.ScoreUnit;
import org.adaptlab.chpir.android.survey.models.ScoreUnit.ScoreType;
import org.adaptlab.chpir.android.survey.models.Survey;

public class ScorerFactory {
    private static final String TAG = "ScorerFactory";

    public static Scorer createScorer(ScoreUnit scoreUnit) {
        Scorer scorer;
        String type = scoreUnit.getScoreType().toString();

        if (type.equals(ScoreType.single_select.toString())) {
            scorer = new MatchScorer();
        } else if (type.equals(ScoreType.multiple_select.toString())) {
            scorer = new BankScorer();
        } else if (type.equals(ScoreType.multiple_select_sum.toString())) {
            scorer = new SumScorer();
        } else if (type.equals(ScoreType.range.toString())) {
            scorer = new RangeScorer();
        } else if (type.equals(ScoreType.simple_search.toString())) {
            scorer = new SearchScorer();
        } else {
            Log.wtf(TAG, "Received unknown score type: " + type);
            scorer = new Scorer() {
                @Override
                public double score(ScoreUnit unit, Survey survey) {
                    return 0;
                }
            };
        }
        return scorer;
    }
}