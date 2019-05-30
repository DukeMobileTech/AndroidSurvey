package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRepository;

import java.util.List;

public class ProjectInstrumentViewModel extends AndroidViewModel {
    private InstrumentRepository mRepository;
    private LiveData<List<Instrument>> mInstruments;

    public ProjectInstrumentViewModel(@NonNull Application application, long projectId) {
        super(application);
        mRepository = new InstrumentRepository(application);
        mInstruments = mRepository.getInstrumentDao().projectInstruments(projectId);
    }

    public LiveData<List<Instrument>> getInstruments() {
        return mInstruments;
    }

}
