package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.relations.InstrumentRelation;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRelationRepository;

public class InstrumentRelationViewModel extends AndroidViewModel {
    private LiveData<InstrumentRelation> mInstrumentRelation;

    public InstrumentRelationViewModel(@NonNull Application application, Long instrumentId) {
        super(application);
        InstrumentRelationRepository repository = new InstrumentRelationRepository(application, instrumentId);
        mInstrumentRelation = repository.getInstrumentRelation();
    }

    public LiveData<InstrumentRelation> getInstrumentRelation() {
        return mInstrumentRelation;
    }

}
