package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;

public class SurveyViewModel extends AndroidViewModel {
    private LiveData<Survey> mSurvey;

    public SurveyViewModel(@NonNull Application application, String uuid) {
        super(application);
        SurveyRepository surveyRepository = new SurveyRepository(application);
        if (uuid == null) return;
        mSurvey = surveyRepository.getSurveyDao().findByUUID(uuid);
    }

    public LiveData<Survey> getSurvey() {
        return mSurvey;
    }

}
