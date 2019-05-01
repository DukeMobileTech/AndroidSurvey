package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.MultipleSkipDao;
import org.adaptlab.chpir.android.survey.entities.MultipleSkip;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class MultipleSkipRepository implements Downloadable {
    private MultipleSkipDao dao;

    public MultipleSkipRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        dao = db.multipleSkipDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(dao, getRemoteTableName(), MultipleSkip.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "multiple_skips";
    }
}
