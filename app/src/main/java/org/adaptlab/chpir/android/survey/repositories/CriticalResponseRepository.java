package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.CriticalResponseDao;
import org.adaptlab.chpir.android.survey.entities.CriticalResponse;
import org.adaptlab.chpir.android.survey.entities.Entity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class CriticalResponseRepository extends Repository {
    private CriticalResponseDao mCriticalResponseDao;

    public CriticalResponseRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mCriticalResponseDao = db.criticalResponseDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "critical_responses";
    }

    @Override
    public BaseDao getDao() {
        return mCriticalResponseDao;
    }

    @Override
    public Entity getEntity() {
        return new CriticalResponse();
    }
}
