package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.SurveyNoteDao;
import org.adaptlab.chpir.android.survey.entities.SurveyNote;

import java.util.List;

public class SurveyNoteRepository {
    public final String TAG = this.getClass().getName();
    private final SurveyNoteDao mSurveyNoteDao;

    public SurveyNoteRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSurveyNoteDao = db.surveyNoteDao();
    }

    public LiveData<List<SurveyNote>> getSurveyNotes(String surveyUuid) {
        return mSurveyNoteDao.surveyNotes(surveyUuid);
    }

    public void insert(SurveyNote surveyNote) {
        new InsertSurveyNoteTask(mSurveyNoteDao).execute(surveyNote);
    }

    public void update(SurveyNote surveyNote) {
        new UpdateSurveyNoteTask(mSurveyNoteDao).execute(surveyNote);
    }

    public void delete(SurveyNote surveyNote) {
        new DeleteSurveyNoteTask(mSurveyNoteDao).execute(surveyNote);
    }

    private static class InsertSurveyNoteTask extends AsyncTask<SurveyNote, Void, Void> {
        private final SurveyNoteDao mSurveyNoteDao;

        InsertSurveyNoteTask(SurveyNoteDao dao) {
            mSurveyNoteDao = dao;
        }

        @Override
        protected Void doInBackground(SurveyNote... params) {
            mSurveyNoteDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateSurveyNoteTask extends AsyncTask<SurveyNote, Void, Void> {
        private final SurveyNoteDao mSurveyNoteDao;

        UpdateSurveyNoteTask(SurveyNoteDao dao) {
            mSurveyNoteDao = dao;
        }

        @Override
        protected Void doInBackground(SurveyNote... params) {
            mSurveyNoteDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteSurveyNoteTask extends AsyncTask<SurveyNote, Void, Void> {
        private final SurveyNoteDao mSurveyNoteDao;

        DeleteSurveyNoteTask(SurveyNoteDao dao) {
            mSurveyNoteDao = dao;
        }

        @Override
        protected Void doInBackground(SurveyNote... params) {
            mSurveyNoteDao.delete(params[0]);
            return null;
        }
    }
}
