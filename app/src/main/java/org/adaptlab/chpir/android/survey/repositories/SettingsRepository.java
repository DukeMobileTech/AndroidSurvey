package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.os.AsyncTask;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.SettingsDao;
import org.adaptlab.chpir.android.survey.entities.Settings;

public class SettingsRepository {
    private SettingsDao mSettingsDao;

    public SettingsRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSettingsDao = db.settingsDao();
    }

    public SettingsDao getSettingsDao() {
        return mSettingsDao;
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
