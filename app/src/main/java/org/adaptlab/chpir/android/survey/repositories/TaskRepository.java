package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.TaskDao;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.entities.Task;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class TaskRepository extends Repository {
    private final TaskDao mTaskDao;

    public TaskRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mTaskDao = db.taskDao();
    }

    @Override
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
    }

    @Override
    public String getRemoteTableName() {
        return "tasks";
    }

    @Override
    public BaseDao getDao() {
        return mTaskDao;
    }

    @Override
    public BaseDao getTranslationDao() {
        return null;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Task();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return null;
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Task.class, new SurveyEntityDeserializer<>(Task.class));
        return gsonBuilder.create();
    }
}
