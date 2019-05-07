package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.ConditionSkipDao;
import org.adaptlab.chpir.android.survey.entities.ConditionSkip;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class ConditionSkipRepository extends Repository {
    private ConditionSkipDao mConditionSkipDao;

    public ConditionSkipRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mConditionSkipDao = db.conditionSkipDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "condition_skips";
    }

    @Override
    public BaseDao getDao() {
        return mConditionSkipDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new ConditionSkip();
    }

}
