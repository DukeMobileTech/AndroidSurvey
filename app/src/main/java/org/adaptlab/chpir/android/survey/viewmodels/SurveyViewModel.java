package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.relations.SurveyResponse;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

public class SurveyViewModel extends AndroidViewModel {
    private LiveData<SurveyResponse> mSurveyResponse;

    public SurveyViewModel(@NonNull Application application, Long instrumentId, String uuid) {
        super(application);
        SurveyRepository surveyRepository = new SurveyRepository(application);
        if (uuid == null) {
            uuid = surveyRepository.initializeSurvey(AppUtil.getProjectId(), instrumentId);
        }
        mSurveyResponse = surveyRepository.getSurveyResponseDao().findByUUID(uuid);
    }

    public LiveData<SurveyResponse> getSurveyResponse() {
        return mSurveyResponse;
    }
}
