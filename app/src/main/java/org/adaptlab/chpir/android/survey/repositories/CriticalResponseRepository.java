package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.CriticalResponseDao;
import org.adaptlab.chpir.android.survey.entities.CriticalResponse;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class CriticalResponseRepository implements Downloadable {
    private CriticalResponseDao mCriticalResponseDao;

    public CriticalResponseRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mCriticalResponseDao = db.criticalResponseDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(mCriticalResponseDao, getRemoteTableName(), CriticalResponse.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "critical_responses";
    }
}
