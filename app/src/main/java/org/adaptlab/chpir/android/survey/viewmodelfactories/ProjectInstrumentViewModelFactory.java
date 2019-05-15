package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.viewmodels.ProjectInstrumentViewModel;

public class ProjectInstrumentViewModelFactory implements ViewModelProvider.Factory {
    private long mProjectId;
    private Application mApplication;

    public ProjectInstrumentViewModelFactory(@NonNull Application application, long id) {
        this.mApplication = application;
        this.mProjectId = id;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProjectInstrumentViewModel.class)) {
            return (T) new ProjectInstrumentViewModel(mApplication, mProjectId);
        } else {
            return null;
        }
    }

}
