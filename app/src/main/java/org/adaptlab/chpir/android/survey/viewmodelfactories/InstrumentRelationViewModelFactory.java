package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.viewmodels.InstrumentRelationViewModel;

public class InstrumentRelationViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private Long mInstrumentId;

    public InstrumentRelationViewModelFactory(@NonNull Application application, Long id) {
        this.mApplication = application;
        this.mInstrumentId = id;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InstrumentRelationViewModel.class)) {
            return (T) new InstrumentRelationViewModel(mApplication, mInstrumentId);
        } else {
            return null;
        }
    }

}
