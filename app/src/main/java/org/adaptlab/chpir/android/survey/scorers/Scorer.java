package org.adaptlab.chpir.android.survey.scorers;

import org.adaptlab.chpir.android.survey.models.GridLabel;
import org.adaptlab.chpir.android.survey.models.Option;
import org.adaptlab.chpir.android.survey.models.OptionScore;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.ScoreUnit;
import org.adaptlab.chpir.android.survey.models.Survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class Scorer {
    public String TAG = this.getClass().getName();
    public abstract double score(ScoreUnit unit, Survey survey);

    List<Option> getSortedSelectedOptions(Response response) {
        List<Option> options = new ArrayList<>();
        String[] optionIds = response.getText().split(",");
        for(String id : optionIds) {
            options.add(response.getQuestion().defaultOptions().get(Integer.valueOf(id)));
        }
        Collections.sort(options, new Comparator<Option>() {
            @Override
            public int compare(Option lhs, Option rhs) {
                return lhs.getRemoteId().compareTo(rhs.getRemoteId());
            }
        });
        return options;
    }

    OptionScore getOptionScore(ScoreUnit unit, Question question, Response response) {
        if (question.getGrid() == null) {
            Option option = question.defaultOptions().get(Integer.valueOf(response.getText()));
            return unit.getOptionScoreByOption(option);
        } else {
            GridLabel label = question.getGrid().labels().get(Integer.valueOf(response.getText()));
            return unit.getOptionScoreByLabel(label);
        }
    }

}