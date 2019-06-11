package org.adaptlab.chpir.android.survey.repositories;


import android.app.Application;
import android.os.AsyncTask;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.SurveyDao;
import org.adaptlab.chpir.android.survey.entities.Survey;

public class SurveyRepository {
    private SurveyDao mSurveyDao;

    public SurveyRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSurveyDao = db.surveyDao();
    }

    public SurveyDao getSurveyDao() {
        return mSurveyDao;
    }

    public Survey initializeSurvey(Long projectId, Long instrumentId) {
        Survey survey = new Survey();
        survey.setProjectId(projectId);
        survey.setInstrumentRemoteId(instrumentId);

        new InsertSurveyTask(mSurveyDao).execute(survey);

        return survey;
    }

    public void update(Survey survey) {
        new UpdateSurveyTask(mSurveyDao).execute(survey);
    }

    private static class UpdateSurveyTask extends AsyncTask<Survey, Void, Void> {

        private SurveyDao mSurveyDao;

        UpdateSurveyTask(SurveyDao dao) {
            mSurveyDao = dao;
        }

        @Override
        protected Void doInBackground(Survey... params) {
            mSurveyDao.update(params[0]);
            return null;
        }
    }

    private static class InsertSurveyTask extends AsyncTask<Survey, Void, Void> {
        private SurveyDao mSurveyDao;

        InsertSurveyTask(SurveyDao surveyDao) {
            mSurveyDao = surveyDao;
        }

        @Override
        protected Void doInBackground(Survey... params) {
            mSurveyDao.insert(params[0]);
            return null;
        }
    }
}
