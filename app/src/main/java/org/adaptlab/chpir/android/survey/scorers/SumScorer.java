package org.adaptlab.chpir.android.survey.scorers;

import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.OptionScore;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ScoreUnit;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.List;

/*
score_type: multiple_select
question_type: SELECT_MULTIPLE, SELECT_MULTIPLE_WRITE_OTHER
Number of questions in unit: 1
Sums the values of the OptionScores whose Options correspond to the options contained in the
response text
 */
public class SumScorer extends Scorer {

    @Override
    public double score(ScoreUnit unit, Survey survey) {
        if (BuildConfig.DEBUG) Log.i(TAG, "SumScorer");
        double sum = 0;
        if (unit.questions().size() > 1) {
            Log.wtf(TAG, "Wrong Unit Scorer");
        } else {
            Response response = survey.getResponseByQuestion(unit.questions().get(0));
            List<OptionScore> optionScores = optionScoresByOptions(unit,
                    getSortedSelectedOptions(response));
            for (OptionScore os : optionScores) {
                sum = sum + os.getValue();
            }
        }
        return sum;
    }

    private List<OptionScore> optionScoresByOptions(ScoreUnit unit, List<Option> options) {
        Character[] placeholdersArray = new Character[options.size()];
        Long[] optionIds = new Long[options.size()];
        for (int i = 0; i < options.size(); i++) {
            placeholdersArray[i] = '?';
            optionIds[i] = options.get(i).getId();
        }
//        String[] unitId = new String[] { unit.getId().toString() };
//        return SQLiteUtils.rawQuery(OptionScore.class, "SELECT * FROM OptionScores WHERE Option IN " +
//                "(SELECT Option FROM OptionScores WHERE ScoreUnit = ?)", unitId);

        // TODO: 6/8/17 Filter using ScoreUnit
        return new Select().from(OptionScore.class)
                .where("Option IN (" + TextUtils.join(",", placeholdersArray) + ")", optionIds)
                .execute();
    }

}