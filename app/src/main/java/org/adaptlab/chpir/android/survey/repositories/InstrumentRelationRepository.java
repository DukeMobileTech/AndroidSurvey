package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.InstrumentDao;
import org.adaptlab.chpir.android.survey.relations.InstrumentRelation;

public class InstrumentRelationRepository {
    public final String TAG = this.getClass().getName();
    private LiveData<InstrumentRelation> mInstrumentRelation;

    public InstrumentRelationRepository(Application application, Long instrumentId) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        InstrumentDao instrumentDao = db.instrumentDao();
        mInstrumentRelation = instrumentDao.findInstrumentRelationById(instrumentId);
    }

    public LiveData<InstrumentRelation> getInstrumentRelation() {
        return mInstrumentRelation;
    }

}
