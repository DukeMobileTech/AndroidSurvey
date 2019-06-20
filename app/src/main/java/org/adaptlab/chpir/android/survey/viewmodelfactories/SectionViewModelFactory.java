package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.adaptlab.chpir.android.survey.viewmodels.SectionViewModel;

public class SectionViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private Long mInstrumentId;

    public SectionViewModelFactory(@NonNull Application application, Long id) {
        this.mApplication = application;
        this.mInstrumentId = id;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SectionViewModel.class)) {
            return (T) new SectionViewModel(mApplication, mInstrumentId);
        } else {
            return null;
        }
    }

}
