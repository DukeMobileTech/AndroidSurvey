package org.adaptlab.chpir.android.survey.scorers;

import android.util.Log;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.models.OptionScore;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ScoreUnit;
import org.adaptlab.chpir.android.survey.models.ScoreUnitQuestion;
import org.adaptlab.chpir.android.survey.models.Survey;

/*
score_type: single_select
question_type: SELECT_ONE, SELECT_ONE_WRITE_OTHER
Picks the maximum score possible based on the responses to the questions within the unit.
 */
public class MatchScorer extends Scorer {
    @Override
    public double score(ScoreUnit unit, Survey survey) {
        if (BuildConfig.DEBUG) Log.i(TAG, "MatchScorer");
        double value = 0;
        for (ScoreUnitQuestion suq : unit.scoreUnitQuestions()) {
            Response response = survey.getResponseByQuestion(suq.getQuestion());
            if (response != null && response.getText() != null) {
                OptionScore optionScore = getOptionScore(unit, suq.getQuestion(), response);
                if (optionScore != null) {
                    if (optionScore.getValue() > value) {
                        value = optionScore.getValue();
                    }
                }
            }
        }
        return value;
    }
}