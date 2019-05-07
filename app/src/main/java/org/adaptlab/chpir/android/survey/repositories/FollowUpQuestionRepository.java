package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.FollowUpQuestionDao;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.entities.FollowUpQuestion;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class FollowUpQuestionRepository extends Repository {
    private FollowUpQuestionDao dao;

    public FollowUpQuestionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        dao = db.followUpQuestionDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "follow_up_questions";
    }

    @Override
    public BaseDao getDao() {
        return dao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new FollowUpQuestion();
    }
}
