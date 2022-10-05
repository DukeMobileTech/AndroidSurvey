package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.os.AsyncTask;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.SurveyScoreDao;
import org.adaptlab.chpir.android.survey.entities.SurveyScore;

public class SurveyScoreRepository {
    private final SurveyScoreDao mSurveyScoreDao;

    public SurveyScoreRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSurveyScoreDao = db.surveyScoreDao();
    }

    public SurveyScore initializeSurveyScore(String uuid, Long scoreSchemeId) {
        SurveyScore surveyScore = new SurveyScore(uuid);
        surveyScore.setScoreSchemeRemoteId(scoreSchemeId);

        new InsertSurveyScoreTask(mSurveyScoreDao).execute(surveyScore);

        return surveyScore;
    }

    private static class InsertSurveyScoreTask extends AsyncTask<SurveyScore, Void, Void> {
        private final SurveyScoreDao surveyScoreDao;

        InsertSurveyScoreTask(SurveyScoreDao surveyDao) {
            surveyScoreDao = surveyDao;
        }

        @Override
        protected Void doInBackground(SurveyScore... params) {
            if (params[0] == null) return null;
            surveyScoreDao.insert(params[0]);
            return null;
        }
    }

}
