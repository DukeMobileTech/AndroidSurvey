package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.SurveyResponse;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRepository;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.util.List;

public class SurveyResponseViewModel extends AndroidViewModel {
    private LiveData<List<SurveyResponse>> mSurveys;

    public SurveyResponseViewModel(@NonNull Application application) {
        super(application);
        SurveyRepository surveyRepository = new SurveyRepository(application);
        mSurveys = surveyRepository.getSurveyResponseDao().projectSurveys(AppUtil.getProjectId());
    }

    public LiveData<List<SurveyResponse>> getSurveys() {
        return mSurveys;
    }

}
