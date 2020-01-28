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
    private LiveData<List<SurveyNote>> mSurveyNotes;
    private SurveyNoteDao mSurveyNoteDao;

    public SurveyNoteRepository(Application application, String surveyUuid) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSurveyNoteDao = db.surveyNoteDao();
        mSurveyNotes = mSurveyNoteDao.surveyNotes(surveyUuid);
    }

    public LiveData<List<SurveyNote>> getSurveyNotes() {
        return mSurveyNotes;
    }

    public void insert(SurveyNote surveyNote) {
        new InsertSurveyNoteTask(mSurveyNoteDao).execute(surveyNote);
    }

    public void update(SurveyNote surveyNote) {
        new UpdateSurveyNoteTask(mSurveyNoteDao).execute(surveyNote);
    }

    private static class InsertSurveyNoteTask extends AsyncTask<SurveyNote, Void, Void> {
        private SurveyNoteDao mSurveyNoteDao;

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
        private SurveyNoteDao mSurveyNoteDao;

        UpdateSurveyNoteTask(SurveyNoteDao dao) {
            mSurveyNoteDao = dao;
        }

        @Override
        protected Void doInBackground(SurveyNote... params) {
            mSurveyNoteDao.update(params[0]);
            return null;
        }
    }
}
