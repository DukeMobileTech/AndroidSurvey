package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.ConditionSkipDao;
import org.adaptlab.chpir.android.survey.entities.ConditionSkip;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class ConditionSkipRepository implements Downloadable {
    private ConditionSkipDao mConditionSkipDao;

    public ConditionSkipRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mConditionSkipDao = db.conditionSkipDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(mConditionSkipDao, getRemoteTableName(), ConditionSkip.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "condition_skips";
    }

}
