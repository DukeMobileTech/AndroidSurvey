package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.viewmodels.InstrumentViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.ProjectInstrumentViewModel;

public class InstrumentViewModelFactory implements ViewModelProvider.Factory {
    private long mInstrumentId;
    private Application mApplication;

    public InstrumentViewModelFactory(@NonNull Application application, long id) {
        this.mApplication = application;
        this.mInstrumentId = id;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InstrumentViewModel.class)) {
            return (T) new InstrumentViewModel(mApplication, mInstrumentId);
        } else {
            return null;
        }
    }

}
