package org.adaptlab.chpir.android.survey.tasks;

import android.os.AsyncTask;

import org.adaptlab.chpir.android.survey.models.ScoreUnit;

public class SetScoreUnitOrderingQuestionTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
        for (ScoreUnit scoreUnit : ScoreUnit.getAll()) {
            scoreUnit.setQuestionNumberInInstrument(
                    scoreUnit.getOrderingQuestion().getNumberInInstrument());
            scoreUnit.save();
        }
        return null;
    }
}