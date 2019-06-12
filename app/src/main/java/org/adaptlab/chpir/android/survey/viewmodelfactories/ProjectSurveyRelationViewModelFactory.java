package org.adaptlab.chpir.android.survey.viewmodelfactories;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

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
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProjectSurveyRelationViewModel.class)) {
            return (T) new ProjectSurveyRelationViewModel(mApplication, mProjectId);
        } else {
            return null;
        }
    }

}
