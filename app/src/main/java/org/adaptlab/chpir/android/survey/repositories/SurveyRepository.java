package org.adaptlab.chpir.android.survey.repositories;


import android.app.Application;
import android.os.AsyncTask;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentDao;
import org.adaptlab.chpir.android.survey.daos.SurveyDao;
import org.adaptlab.chpir.android.survey.daos.relations.SurveyResponseDao;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;

public class SurveyRepository extends Repository {
    private SurveyDao mSurveyDao;
    private SurveyResponseDao mSurveyResponseDao;
    private InstrumentDao mInstrumentDao;

    public SurveyRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSurveyDao = db.surveyDao();
        mSurveyResponseDao = db.surveyResponseDao();
        mInstrumentDao = db.instrumentDao();
    }

    public SurveyDao getSurveyDao() {
        return mSurveyDao;
    }

    public SurveyResponseDao getSurveyResponseDao() {
        return mSurveyResponseDao;
    }

    public String initializeSurvey(Long projectId, Long instrumentId) {
        Survey survey = new Survey();
        survey.setProjectId(projectId);
        survey.setInstrumentRemoteId(instrumentId);

        new InsertSurveyTask(mSurveyDao, mInstrumentDao).execute(survey);

        return survey.getUUID();
    }

    public void update(Survey survey) {
        new UpdateSurveyTask(mSurveyDao).execute(survey);
    }

    @Override
    public void download() {
        // Do not implement
    }

    @Override
    public String getRemoteTableName() {
        return "surveys";
    }

    @Override
    public BaseDao getDao() {
        return mSurveyDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Survey();
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
        private InstrumentDao mInstrumentDao;

        InsertSurveyTask(SurveyDao surveyDao, InstrumentDao instrumentDao) {
            mSurveyDao = surveyDao;
            mInstrumentDao = instrumentDao;
        }

        @Override
        protected Void doInBackground(Survey... params) {
            Survey survey = params[0];
            Instrument instrument = mInstrumentDao.findByIdSync(survey.getInstrumentRemoteId());
            survey.setInstrument(instrument);
            mSurveyDao.insert(survey);
            return null;
        }
    }
}
