package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.InstrumentTranslation;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRepository;

import java.util.List;

public class InstrumentTranslationViewModel extends AndroidViewModel {
    private InstrumentRepository mRepository;
    private LiveData<List<InstrumentTranslation>> mAllInstrumentTranslations;

    public InstrumentTranslationViewModel(@NonNull Application application) {
        super(application);
        mRepository = new InstrumentRepository(application);
        mAllInstrumentTranslations = mRepository.getAllInstrumentTranslations();
    }

    public LiveData<List<InstrumentTranslation>> getAllInstrumentTranslations() {
        return mAllInstrumentTranslations;
    }

}
