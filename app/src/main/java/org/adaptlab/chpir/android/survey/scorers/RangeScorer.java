package org.adaptlab.chpir.android.survey.scorers;

import android.text.TextUtils;
import android.util.Log;
import android.util.Range;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.models.OptionScore;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ScoreUnit;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.HashMap;
import java.util.Map;

/*
    score_type: range
    question_type: INTEGER, DECIMAL_NUMBER, SLIDER, LABELED_SLIDER, RATING
 */
public class RangeScorer extends Scorer {
    @Override
    public double score(ScoreUnit unit, Survey survey) {
        if (BuildConfig.DEBUG) Log.i(TAG, "RangeScorer");
        double scoreVal = 0;
        for (Question question : unit.questions()) {
            Response response = survey.getResponseByQuestion(question);
            if (!TextUtils.isEmpty(response.getText())) {
                Double responseText = Double.parseDouble(response.getText());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    HashMap<Range<Double>, Double> map = rangeScoreOptionsMap(unit);
                    for (Map.Entry<Range<Double>, Double> entry : map.entrySet()) {
                        if (entry.getKey().contains(responseText) && entry.getValue() > scoreVal) {
                            scoreVal = entry.getValue();
                        }
                    }
                }
            }
        }
        return scoreVal;
    }

    private HashMap<Range<Double>, Double> rangeScoreOptionsMap(ScoreUnit unit) {
        HashMap<Range<Double>, Double> map = new HashMap<>();
        for (OptionScore os : unit.optionScores()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (os.getLabel().contains("..")) {
                    String[] ranges = os.getLabel().split("\\.\\.");
                    Range<Double> range = new Range<>(Double.parseDouble(ranges[0]),
                            Double.parseDouble(ranges[1]));
                    map.put(range, os.getValue());
                }
            }
        }
        return map;
    }
}