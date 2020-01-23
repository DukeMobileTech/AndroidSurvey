package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.adaptlab.chpir.android.survey.viewmodels.QuestionRelationViewModel;

public class QuestionRelationViewModelFactory implements ViewModelProvider.Factory {
    private Long mInstrumentId;
    private Long mDisplayId;
    private Application mApplication;

    public QuestionRelationViewModelFactory(@NonNull Application application, Long instrumentId, Long displayId) {
        this.mApplication = application;
        this.mInstrumentId = instrumentId;
        this.mDisplayId = displayId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new QuestionRelationViewModel(mApplication, mInstrumentId, mDisplayId);
    }

}
