package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.NextQuestionDao;
import org.adaptlab.chpir.android.survey.entities.NextQuestion;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class NextQuestionRepository implements Downloadable {
    private NextQuestionDao dao;

    public NextQuestionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        dao = db.nextQuestionDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(dao, getRemoteTableName(), NextQuestion.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "next_questions";
    }

}
