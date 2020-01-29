package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.relations.ProjectSurveyRelation;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;

import java.util.List;

public class ProjectSurveyRelationViewModel extends AndroidViewModel {
    private LiveData<List<ProjectSurveyRelation>> mOngoingSurveys;
    private LiveData<List<ProjectSurveyRelation>> mSubmittedSurveys;

    public ProjectSurveyRelationViewModel(@NonNull Application application, long projectId) {
        super(application);
        SurveyRepository surveyRepository = new SurveyRepository(application);
        mOngoingSurveys = surveyRepository.getSurveyDao().onGoingProjectSurveys(projectId);
        mSubmittedSurveys = surveyRepository.getSurveyDao().submittedProjectSurveys(projectId);
    }

    public LiveData<List<ProjectSurveyRelation>> getOngoingProjectSurveyRelations() {
        return mOngoingSurveys;
    }

    public LiveData<List<ProjectSurveyRelation>> getSubmittedProjectSurveyRelations() {
        return mSubmittedSurveys;
    }

}
