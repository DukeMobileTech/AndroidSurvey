package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.QuestionCollageDao;
import org.adaptlab.chpir.android.survey.entities.QuestionCollage;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class QuestionCollageRepository extends Repository {
    private final QuestionCollageDao mQuestionCollageDao;

    public QuestionCollageRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mQuestionCollageDao = db.questionCollageDao();
    }

    @Override
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
    }

    @Override
    public String getRemoteTableName() {
        return "question_collages";
    }

    @Override
    public BaseDao getDao() {
        return mQuestionCollageDao;
    }

    @Override
    public BaseDao getTranslationDao() {
        return null;
    }

    @Override
    public SurveyEntity getEntity() {
        return new QuestionCollage();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return null;
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(QuestionCollage.class, new SurveyEntityDeserializer<>(QuestionCollage.class));
        return gsonBuilder.create();
    }
}
