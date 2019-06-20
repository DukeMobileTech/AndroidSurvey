package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRepository;

import java.util.List;

public class ProjectInstrumentViewModel extends AndroidViewModel {
    private LiveData<List<Instrument>> mInstruments;

    public ProjectInstrumentViewModel(@NonNull Application application, long projectId) {
        super(application);
        InstrumentRepository mRepository = new InstrumentRepository(application);
        mInstruments = mRepository.getInstrumentDao().projectInstruments(projectId);
    }

    public LiveData<List<Instrument>> getInstruments() {
        return mInstruments;
    }

}
