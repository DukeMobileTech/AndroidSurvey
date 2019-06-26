package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.daos.ProjectDao;
import org.adaptlab.chpir.android.survey.entities.Project;
import org.adaptlab.chpir.android.survey.repositories.ProjectRepository;

public class ProjectViewModel extends AndroidViewModel {
    private LiveData<Project> mProject;

    public ProjectViewModel(@NonNull Application application, Long projectId) {
        super(application);
        ProjectRepository projectRepository = new ProjectRepository(application);
        mProject = ((ProjectDao) projectRepository.getDao()).findById(projectId);
    }

    public LiveData<Project> getProject() {
        return mProject;
    }

}
