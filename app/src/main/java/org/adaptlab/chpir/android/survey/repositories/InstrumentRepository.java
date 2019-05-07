package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentTranslationDao;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.InstrumentTranslation;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

import java.util.List;

public class InstrumentRepository extends Repository {
    private InstrumentDao mInstrumentDao;
    private InstrumentTranslationDao mInstrumentTranslationDao;
    private LiveData<List<Instrument>> mAllInstruments;
    private LiveData<List<InstrumentTranslation>> mAllInstrumentTranslations;

    public InstrumentRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mInstrumentDao = db.instrumentDao();
        mInstrumentTranslationDao = db.instrumentTranslationDao();
        mAllInstruments = mInstrumentDao.getAllInstruments();
        mAllInstrumentTranslations = mInstrumentTranslationDao.getAllInstrumentTranslations();
    }

    public LiveData<List<Instrument>> getAllInstruments() {
        return mAllInstruments;
    }

    public LiveData<List<InstrumentTranslation>> getAllInstrumentTranslations() {
        return mAllInstrumentTranslations;
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
}
