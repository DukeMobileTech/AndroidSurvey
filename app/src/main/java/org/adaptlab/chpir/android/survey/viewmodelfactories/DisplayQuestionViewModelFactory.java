package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.viewmodels.DisplayQuestionViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

public class DisplayQuestionViewModelFactory implements ViewModelProvider.Factory {
    private Long mInstrumentId;
    private int mPosition;
    private Application mApplication;

    public DisplayQuestionViewModelFactory(@NonNull Application application, Long instrumentId, int position) {
        this.mApplication = application;
        this.mInstrumentId = instrumentId;
        this.mPosition = position;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DisplayQuestionViewModel.class)) {
            return (T) new DisplayQuestionViewModel(mApplication, mInstrumentId, mPosition);
        } else {
            return null;
        }
    }

}
