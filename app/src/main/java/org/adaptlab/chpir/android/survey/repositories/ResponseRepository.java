package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.os.AsyncTask;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.ResponseDao;
import org.adaptlab.chpir.android.survey.entities.Response;

import java.util.List;

public class ResponseRepository {
    private ResponseDao mResponseDao;

    public ResponseRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mResponseDao = db.responseDao();
    }

    public void insert(Response response) {
        new InsertResponseTask(mResponseDao).execute(response);
    }

    public void update(Response response) {
        new UpdateResponseTask(mResponseDao).execute(response);
    }

    public void insertAll(List<Response> responses) {
        new InsertAllResponsesTask(mResponseDao, responses).execute();
    }

    private static class InsertResponseTask extends AsyncTask<Response, Void, Void> {
        private ResponseDao mResponseDao;

        InsertResponseTask(ResponseDao dao) {
            mResponseDao = dao;
        }

        @Override
        protected Void doInBackground(Response... params) {
            mResponseDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateResponseTask extends AsyncTask<Response, Void, Void> {
        private ResponseDao mResponseDao;

        UpdateResponseTask(ResponseDao dao) {
            mResponseDao = dao;
        }

        @Override
        protected Void doInBackground(Response... params) {
            mResponseDao.update(params[0]);
            return null;
        }
    }

    private static class InsertAllResponsesTask extends AsyncTask<Void, Void, Void> {
        private ResponseDao mResponseDao;
        private List<Response> mResponses;

        InsertAllResponsesTask(ResponseDao dao, List<Response> responses) {
            mResponseDao = dao;
            mResponses = responses;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mResponseDao.insertAll(mResponses);
            return null;
        }
    }
}
