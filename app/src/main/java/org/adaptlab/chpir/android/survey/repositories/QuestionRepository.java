package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.daos.QuestionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.QuestionTranslation;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class QuestionRepository extends Repository {
    private static final String TAG = "QuestionRepository";
    private final QuestionDao mQuestionDao;
    private final QuestionTranslationDao mQuestionTranslationDao;

    public QuestionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mQuestionDao = db.questionDao();
        mQuestionTranslationDao = db.questionTranslationDao();
    }

    @Override
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
    }

    @Override
    public String getRemoteTableName() {
        return "questions";
    }

    @Override
    public BaseDao getDao() {
        return mQuestionDao;
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Question.class, new SurveyEntityDeserializer<>(Question.class));
        return gsonBuilder.create();
    }

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
