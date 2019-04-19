package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRepository;

import java.util.List;

public class InstrumentViewModel extends AndroidViewModel {
    private InstrumentRepository mRepository;
    private LiveData<List<Instrument>> mAllInstruments;

    public InstrumentViewModel(@NonNull Application application) {
        super(application);
        mRepository = new InstrumentRepository(application);
        mAllInstruments = mRepository.getAllInstruments();
    }

    public LiveData<List<Instrument>> getAllInstruments() {
        return mAllInstruments;
    }

    public LiveData<List<Question>> getInstrumentQuestions(Instrument instrument) {
        return mRepository.getInstrumentDao().questions(instrument.getRemoteId());
    }

}
