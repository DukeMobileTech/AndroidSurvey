package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.viewmodels.QuestionRelationViewModel;

public class QuestionRelationViewModelFactory implements ViewModelProvider.Factory {
    private Long mInstrumentId;
    private Long mDisplayId;
    private String mSurveyUUID;
    private Application mApplication;

    public QuestionRelationViewModelFactory(@NonNull Application application, Long instrumentId, Long displayId, String uuid) {
        this.mApplication = application;
        this.mInstrumentId = instrumentId;
        this.mDisplayId = displayId;
        this.mSurveyUUID = uuid;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(QuestionRelationViewModel.class)) {
            return (T) new QuestionRelationViewModel(mApplication, mInstrumentId, mDisplayId, mSurveyUUID);
        } else {
            return null;
        }
    }

}
