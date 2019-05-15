package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.viewmodels.DisplayQuestionViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayViewModel;

public class DisplayViewModelFactory implements ViewModelProvider.Factory {
    private Long mInstrumentId;
    private Application mApplication;

    public DisplayViewModelFactory(@NonNull Application application, Long instrumentId) {
        this.mApplication = application;
        this.mInstrumentId = instrumentId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DisplayViewModel.class)) {
            return (T) new DisplayViewModel(mApplication, mInstrumentId);
        } else {
            return null;
        }
    }

}
