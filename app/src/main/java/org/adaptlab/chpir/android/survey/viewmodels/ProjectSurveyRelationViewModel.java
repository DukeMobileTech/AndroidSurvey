package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.relations.ProjectSurveyRelation;
import org.adaptlab.chpir.android.survey.relations.SurveyRelation;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;

import java.util.List;

public class ProjectSurveyRelationViewModel extends AndroidViewModel {
    private LiveData<List<ProjectSurveyRelation>> mProjectSurveyRelations;

    public ProjectSurveyRelationViewModel(@NonNull Application application, long projectId) {
        super(application);
        SurveyRepository surveyRepository = new SurveyRepository(application);
        mProjectSurveyRelations = surveyRepository.getSurveyDao().projectSurveys(projectId);
    }

    public LiveData<List<ProjectSurveyRelation>> getProjectSurveyRelations() {
        return mProjectSurveyRelations;
    }

}
