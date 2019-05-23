package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.relations.SurveyRelation;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;

public class SurveyResponseViewModel extends AndroidViewModel {
    private LiveData<SurveyRelation> mSurveyResponse;

    public SurveyResponseViewModel(@NonNull Application application, String uuid) {
        super(application);
        SurveyRepository surveyRepository = new SurveyRepository(application);
        if (uuid == null) return;
        mSurveyResponse = surveyRepository.getSurveyResponseDao().findByUUID(uuid);
    }

    public LiveData<SurveyRelation> getSurveyResponse() {
        return mSurveyResponse;
    }

}
