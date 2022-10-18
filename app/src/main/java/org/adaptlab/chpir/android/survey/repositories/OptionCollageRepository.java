package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.OptionCollageDao;
import org.adaptlab.chpir.android.survey.entities.OptionCollage;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class OptionCollageRepository extends Repository {
    private final OptionCollageDao mOptionCollageDao;

    public OptionCollageRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mOptionCollageDao = db.optionCollageDao();
    }

    @Override
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
    }

    @Override
    public String getRemoteTableName() {
        return "option_collages";
    }

    @Override
    public BaseDao getDao() {
        return mOptionCollageDao;
    }

    @Override
    public BaseDao getTranslationDao() {
        return null;
    }

    @Override
    public SurveyEntity getEntity() {
        return new OptionCollage();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return null;
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(OptionCollage.class, new SurveyEntityDeserializer<>(OptionCollage.class));
        return gsonBuilder.create();
    }
}
