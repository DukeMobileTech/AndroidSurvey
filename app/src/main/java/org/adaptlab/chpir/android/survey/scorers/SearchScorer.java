package org.adaptlab.chpir.android.survey.scorers;

import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.models.OptionScore;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ScoreUnit;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

/*
score_type: simple_search
question_type: FREE_RESPONSE, LIST_OF_TEXT_BOXES
Expects the OptionScore labels to be similar
*/
public class SearchScorer extends Scorer {
    @Override
    public double score(ScoreUnit unit, Survey survey) {
        if (BuildConfig.DEBUG) Log.i(TAG, "SearchScorer");
        ArrayList<Double> scores = new ArrayList<>();
        scores.add(0.0);
        for (Question question : unit.questions()) {
            Response response = survey.getResponseByQuestion(question);
            if (!TextUtils.isEmpty(response.getText())) {
                if (areLabelsSimilar(unit)) {
                    String label = unit.optionScores().get(0).getLabel();
                    Pattern pattern = Pattern.compile(Pattern.quote(response.getText()),
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                    if (pattern.matcher(label).matches()) {
                        scores.add(getLabelOptionScores(unit, true).getValue());
                    } else {
                        scores.add(getLabelOptionScores(unit, false).getValue());
                    }
                } else {
                    Log.wtf(TAG, "More than one type of label!");
                }
            }
        }
        return Collections.max(scores);
    }

    private boolean areLabelsSimilar(ScoreUnit unit) {
        for (int k = 1; k < unit.optionScores().size(); k++) {
            if (!unit.optionScores().get(k).getLabel().equals(unit.optionScores().get(k - 1).getLabel())) {
                return false;
            }
        }
        return true;
    }

    private OptionScore getLabelOptionScores(ScoreUnit unit, boolean presence) {
        return new Select().from(OptionScore.class)
                .where("ScoreUnit = ? AND Present = ?", unit.getId(), presence)
                .executeSingle();
    }

}