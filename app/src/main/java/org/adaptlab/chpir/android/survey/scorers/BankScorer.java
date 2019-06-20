package org.adaptlab.chpir.android.survey.scorers;


import android.util.Log;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.OptionScore;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ScoreUnit;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.HashMap;
import java.util.Map;

/*
score_type: multiple_select
question_type: SELECT_MULTIPLE, SELECT_MULTIPLE_WRITE_OTHER
Number of questions in unit: 1
Takes the response text and forms a string value by concatenating the RemoteIds of the selected
options (sorted by their RemoteId attributes). The key of each entry set in the HashMap is the
score for a selection of the options whose RemoteIds form the value of the map entry set.
 */
public class BankScorer extends Scorer {
//    private final static String TAG = "BankScorer";

    @Override
    public double score(ScoreUnit unit, Survey survey) {
        if (BuildConfig.DEBUG) Log.i(TAG, "BankScorer");
        double scoreVal = 0;
        for (Question question : unit.questions()) {
            Response response = survey.getResponseByQuestion(question);
            String value = "";
            for (Option option : getSortedSelectedOptions(response)) {
                value = value + option.getRemoteId().toString();
            }
            for (Map.Entry<Double, String> entry : scoreOptionsMap(unit).entrySet()) {
                if (entry.getValue().equals(value) && entry.getKey() > scoreVal) {
                    scoreVal = entry.getKey();
                }
            }
        }
        return scoreVal;
    }

    /*
        Maps a score value to a concatenation of the ids of the options that give that score value
     */
    private HashMap<Double, String> scoreOptionsMap(ScoreUnit unit) {
        HashMap<Double, String> map = new HashMap<>();
        for (OptionScore os : unit.optionScoresGroupedByValue()) {
            String idsString = "";
            for (OptionScore optionScore : unit.optionScoresByValue(os.getValue())) {
                idsString = idsString + optionScore.getOption().getRemoteId().toString();
            }
            map.put(os.getValue(), idsString);
        }
        return map;
    }

}