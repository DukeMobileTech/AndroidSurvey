package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.LoopQuestionDao;
import org.adaptlab.chpir.android.survey.entities.LoopQuestion;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class LoopQuestionRepository extends Repository {
    private LoopQuestionDao mLoopQuestionDao;

    public LoopQuestionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mLoopQuestionDao = db.loopQuestionDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "loop_questions";
    }

    @Override
    public BaseDao getDao() {
        return mLoopQuestionDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new LoopQuestion();
    }
}
