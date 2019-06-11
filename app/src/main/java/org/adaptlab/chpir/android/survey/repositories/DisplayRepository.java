package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.DisplayDao;
import org.adaptlab.chpir.android.survey.daos.DisplayTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.DisplayTranslation;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class DisplayRepository extends Repository {
    private DisplayDao mDisplayDao;
    private DisplayTranslationDao mDisplayTranslationDao;

    public DisplayRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mDisplayDao = db.displayDao();
        mDisplayTranslationDao = db.displayTranslationDao();
    }

    public DisplayDao getDisplayDao() {
        return mDisplayDao;
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "displays";
    }

    @Override
    public BaseDao getDao() {
        return mDisplayDao;
    }

    @Override
    public BaseDao getTranslationDao() {
        return mDisplayTranslationDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Display();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return new DisplayTranslation();
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Display.class, new SurveyEntityDeserializer<>(Display.class));
        return gsonBuilder.create();
    }
}
