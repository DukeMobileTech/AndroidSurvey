package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.SettingsDao;
import org.adaptlab.chpir.android.survey.entities.Settings;

import java.util.List;

public class SettingsRepository {
    private SettingsDao mSettingsDao;
    private LiveData<Settings> mSettings;
    private LiveData<List<String>> mLanguages;

    public SettingsRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSettingsDao = db.settingsDao();
        mSettings = mSettingsDao.getInstance();
        mLanguages = mSettingsDao.languages();
    }

    public SettingsDao getSettingsDao() {
        return mSettingsDao;
    }

    public LiveData<Settings> getSettings() {
        return mSettings;
    }

    public LiveData<List<String>> languages() {
        return mLanguages;
    }

    public void update(Settings settings) {
        new UpdateAsyncTask(mSettingsDao).execute(settings);
    }

    private static class UpdateAsyncTask extends AsyncTask<Settings, Void, Void> {

        private SettingsDao mAsyncTaskDao;

        UpdateAsyncTask(SettingsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Settings... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }


}
