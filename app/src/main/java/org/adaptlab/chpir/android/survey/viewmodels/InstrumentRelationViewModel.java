package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.relations.InstrumentRelation;
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
