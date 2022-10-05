package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.adaptlab.chpir.android.survey.viewmodels.ProjectViewModel;

public class ProjectViewModelFactory implements ViewModelProvider.Factory {
    private final Application mApplication;
    private final Long mProjectId;

    public ProjectViewModelFactory(@NonNull Application application, Long id) {
        this.mApplication = application;
        this.mProjectId = id;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ProjectViewModel(mApplication, mProjectId);
    }

}
