package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.relations.SurveyResponse;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.util.List;

public class ProjectSurveyResponseViewModel extends AndroidViewModel {
    private LiveData<List<SurveyResponse>> mSurveyResponses;

    public ProjectSurveyResponseViewModel(@NonNull Application application, long projectId) {
        super(application);
        SurveyRepository surveyRepository = new SurveyRepository(application);
        mSurveyResponses = surveyRepository.getSurveyResponseDao().projectSurveys(projectId);
    }

    public LiveData<List<SurveyResponse>> getSurveyResponses() {
        return mSurveyResponses;
    }

}
