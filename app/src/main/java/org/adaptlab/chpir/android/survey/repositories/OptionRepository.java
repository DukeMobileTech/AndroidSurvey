package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.OptionDao;
import org.adaptlab.chpir.android.survey.daos.OptionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.OptionTranslation;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class OptionRepository extends Repository {
    private final OptionDao mOptionDao;
    private final OptionTranslationDao mOptionTranslationDao;

    public OptionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mOptionDao = db.optionDao();
        mOptionTranslationDao = db.optionTranslationDao();
    }

    @Override
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
    }

    @Override
    public String getRemoteTableName() {
        return "options";
    }

    @Override
    public BaseDao getDao() {
        return mOptionDao;
    }

    @Override
    public BaseDao getTranslationDao() {
        return mOptionTranslationDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Option();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return new OptionTranslation();
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Option.class, new SurveyEntityDeserializer<>(Option.class));
        return gsonBuilder.create();
    }
}
