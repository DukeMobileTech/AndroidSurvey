package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRepository;

public class InstrumentViewModel extends AndroidViewModel {
    private InstrumentRepository mRepository;
    private LiveData<Instrument> mInstrument;

    public InstrumentViewModel(@NonNull Application application, Long instrumentId) {
        super(application);
        mRepository = new InstrumentRepository(application);
        mInstrument = mRepository.getInstrumentDao().findById(instrumentId);
    }

    public LiveData<Instrument> getInstrument() {
        return mInstrument;
    }

}
