package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.MultipleSkipDao;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.entities.MultipleSkip;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class MultipleSkipRepository extends Repository {
    private MultipleSkipDao dao;

    public MultipleSkipRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        dao = db.multipleSkipDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "multiple_skips";
    }

    @Override
    public BaseDao getDao() {
        return dao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new MultipleSkip();
    }
}
