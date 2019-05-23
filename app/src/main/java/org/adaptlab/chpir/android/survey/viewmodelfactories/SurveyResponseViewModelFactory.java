package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.viewmodels.SurveyResponseViewModel;

public class SurveyResponseViewModelFactory implements ViewModelProvider.Factory {
    private String mUUID;
    private Application mApplication;

    public SurveyResponseViewModelFactory(@NonNull Application application, String uuid) {
        this.mApplication = application;
        this.mUUID = uuid;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SurveyResponseViewModel.class)) {
            return (T) new SurveyResponseViewModel(mApplication, mUUID);
        } else {
            return null;
        }
    }

}
