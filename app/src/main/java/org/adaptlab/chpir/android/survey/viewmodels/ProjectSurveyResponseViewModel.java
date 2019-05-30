package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.relations.SurveyRelation;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;

import java.util.List;

public class ProjectSurveyResponseViewModel extends AndroidViewModel {
    private LiveData<List<SurveyRelation>> mSurveyResponses;

    public ProjectSurveyResponseViewModel(@NonNull Application application, long projectId) {
        super(application);
        SurveyRepository surveyRepository = new SurveyRepository(application);
        mSurveyResponses = surveyRepository.getSurveyDao().projectSurveys(projectId);
    }

    public LiveData<List<SurveyRelation>> getSurveyResponses() {
        return mSurveyResponses;
    }

}
