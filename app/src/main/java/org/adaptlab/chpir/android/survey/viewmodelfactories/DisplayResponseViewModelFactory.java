package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.viewmodels.DisplayQuestionViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayResponseViewModel;

public class DisplayResponseViewModelFactory implements ViewModelProvider.Factory {
    private String mSurveyUUID;
    private long mInstrumentId;
    private long mDisplayId;
    private Application mApplication;

    public DisplayResponseViewModelFactory(@NonNull Application application, String uuid, long instrumentId, long displayId) {
        this.mApplication = application;
        this.mSurveyUUID = uuid;
        this.mInstrumentId = instrumentId;
        this.mDisplayId = displayId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DisplayResponseViewModel.class)) {
            return (T) new DisplayResponseViewModel(mApplication, mSurveyUUID, mInstrumentId, mDisplayId);
        } else {
            return null;
        }
    }

}
