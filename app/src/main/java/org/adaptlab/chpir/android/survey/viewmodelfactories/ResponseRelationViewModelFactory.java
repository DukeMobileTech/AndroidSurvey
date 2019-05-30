package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.viewmodels.ResponseRelationViewModel;

public class ResponseRelationViewModelFactory implements ViewModelProvider.Factory {
    private String mSurveyUUID;
    private long mInstrumentId;
    private long mDisplayId;
    private Application mApplication;

    public ResponseRelationViewModelFactory(@NonNull Application application, Long instrumentId, Long displayId, String uuid) {
        this.mApplication = application;
        this.mSurveyUUID = uuid;
        this.mInstrumentId = instrumentId;
        this.mDisplayId = displayId;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ResponseRelationViewModel.class)) {
            return (T) new ResponseRelationViewModel(mApplication, mInstrumentId, mDisplayId, mSurveyUUID);
        } else {
            return null;
        }
    }

}
