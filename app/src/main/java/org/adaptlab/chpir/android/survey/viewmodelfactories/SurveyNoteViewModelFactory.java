package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.adaptlab.chpir.android.survey.viewmodels.SurveyNoteViewModel;

public class SurveyNoteViewModelFactory implements ViewModelProvider.Factory {
    private String mUUID;
    private Application mApplication;

    public SurveyNoteViewModelFactory(@NonNull Application application, String uuid) {
        this.mApplication = application;
        this.mUUID = uuid;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SurveyNoteViewModel(mApplication, mUUID);
    }

}
