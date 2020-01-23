package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

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
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new InstrumentRelationViewModel(mApplication, mInstrumentId);
    }

}
