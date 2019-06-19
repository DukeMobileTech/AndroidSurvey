package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.InstrumentTranslation;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class InstrumentRepository extends Repository {
    private InstrumentDao mInstrumentDao;
    private InstrumentTranslationDao mInstrumentTranslationDao;

    public InstrumentRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mInstrumentDao = db.instrumentDao();
        mInstrumentTranslationDao = db.instrumentTranslationDao();
    }

    public InstrumentDao getInstrumentDao() {
        return mInstrumentDao;
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "instruments";
    }

    @Override
    public BaseDao getDao() {
        return mInstrumentDao;
    }

    @Override
    public BaseDao getTranslationDao() {
        return mInstrumentTranslationDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Instrument();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return new InstrumentTranslation();
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Instrument.class, new SurveyEntityDeserializer<>(Instrument.class));
        return gsonBuilder.create();
    }
}
