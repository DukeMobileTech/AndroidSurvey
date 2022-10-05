package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.adaptlab.chpir.android.survey.viewmodels.ProjectInstrumentViewModel;

public class ProjectInstrumentViewModelFactory implements ViewModelProvider.Factory {
    private final long mProjectId;
    private final Application mApplication;

    public ProjectInstrumentViewModelFactory(@NonNull Application application, long id) {
        this.mApplication = application;
        this.mProjectId = id;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ProjectInstrumentViewModel(mApplication, mProjectId);
    }

}
