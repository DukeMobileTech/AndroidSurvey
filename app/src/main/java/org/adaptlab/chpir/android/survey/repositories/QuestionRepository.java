package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.daos.QuestionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.QuestionTranslation;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class QuestionRepository extends Repository {
    private static final String TAG = "QuestionRepository";
    private QuestionDao mQuestionDao;
    private QuestionTranslationDao mQuestionTranslationDao;

    public QuestionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mQuestionDao = db.questionDao();
        mQuestionTranslationDao = db.questionTranslationDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "questions";
    }

    @Override
    public BaseDao getDao() {
        return mQuestionDao;
    }

//    @Override
//    public Gson getGson() {
//        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.registerTypeAdapter(Question.class, new QuestionDeserializer());
//        return gsonBuilder.create();
//    }

    @Override
    public BaseDao getTranslationDao() {
        return mQuestionTranslationDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Question();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return new QuestionTranslation();
    }

}
