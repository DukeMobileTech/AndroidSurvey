package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.CollageDao;
import org.adaptlab.chpir.android.survey.entities.Collage;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class CollageRepository extends Repository {
    private final CollageDao mCollageDao;

    public CollageRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mCollageDao = db.collageDao();
    }

    @Override
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
    }

    @Override
    public String getRemoteTableName() {
        return "collages";
    }

    @Override
    public BaseDao getDao() {
        return mCollageDao;
    }

    @Override
    public BaseDao getTranslationDao() {
        return null;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Collage();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return null;
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Collage.class, new SurveyEntityDeserializer<>(Collage.class));
        return gsonBuilder.create();
    }
}
