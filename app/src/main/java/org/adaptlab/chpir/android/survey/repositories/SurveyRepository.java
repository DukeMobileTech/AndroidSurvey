package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.os.AsyncTask;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.SurveyDao;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.util.List;

public class SurveyRepository {
    private SurveyDao mSurveyDao;

    public SurveyRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSurveyDao = db.surveyDao();
    }

    public SurveyDao getSurveyDao() {
        return mSurveyDao;
    }

    public List<Survey> getCompleted() {
        return mSurveyDao.projectCompletedSurveys(AppUtil.getProjectId());
    }

    public List<Survey> getIncomplete() {
        return mSurveyDao.projectIncompleteSurveys(AppUtil.getProjectId());
    }

    public Survey initializeSurvey(String uuid, Long projectId, Long instrumentId) {
        Survey survey = new Survey(uuid);
        survey.setProjectId(projectId);
        survey.setInstrumentRemoteId(instrumentId);
        survey.setLanguage(AppUtil.getDeviceLanguage());

        new InsertSurveyTask(mSurveyDao).execute(survey);

        return survey;
    }

    public void update(Survey survey) {
        new UpdateSurveyTask(mSurveyDao).execute(survey);
    }

    public void delete(Survey survey) {
        new DeleteSurveyTask(mSurveyDao).execute(survey);
    }

    private static class UpdateSurveyTask extends AsyncTask<Survey, Void, Void> {

        private SurveyDao mSurveyDao;

        UpdateSurveyTask(SurveyDao dao) {
            mSurveyDao = dao;
        }

        @Override
        protected Void doInBackground(Survey... params) {
            if (params[0] == null) return null;
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
            if (params[0] == null) return null;
            mSurveyDao.insert(params[0]);
            return null;
        }
    }

    private static class DeleteSurveyTask extends AsyncTask<Survey, Void, Void> {
        private SurveyDao surveyDao;

        DeleteSurveyTask(SurveyDao dao) {
            surveyDao = dao;
        }

        @Override
        protected Void doInBackground(Survey... params) {
            if (params[0] == null) return null;
            surveyDao.delete(params[0]);
            return null;
        }
    }
}
