package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.adaptlab.chpir.android.survey.viewmodels.ProjectSurveyRelationViewModel;

public class ProjectSurveyRelationViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private long mProjectId;

    public ProjectSurveyRelationViewModelFactory(@NonNull Application application, long projectId) {
        this.mApplication = application;
        this.mProjectId = projectId;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ProjectSurveyRelationViewModel(mApplication, mProjectId);
    }

}
