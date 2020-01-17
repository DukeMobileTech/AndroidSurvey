package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
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
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
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

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ConditionSkip.class, new SurveyEntityDeserializer<>(ConditionSkip.class));
        return gsonBuilder.create();
    }
}
