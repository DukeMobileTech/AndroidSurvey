package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.OptionSetOptionDao;
import org.adaptlab.chpir.android.survey.entities.OptionSetOption;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class OptionSetOptionRepository extends Repository {
    private OptionSetOptionDao mOptionSetOptionDao;

    public OptionSetOptionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mOptionSetOptionDao = db.optionSetOptionDao();
    }

    @Override
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
    }

    @Override
    public String getRemoteTableName() {
        return "option_in_option_sets";
    }

    @Override
    public BaseDao getDao() {
        return mOptionSetOptionDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new OptionSetOption();
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(OptionSetOption.class, new SurveyEntityDeserializer<>(OptionSetOption.class));
        return gsonBuilder.create();
    }
}
