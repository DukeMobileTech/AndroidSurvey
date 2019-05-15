package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.relations.DisplayQuestion;
import org.adaptlab.chpir.android.survey.entities.relations.SurveyResponse;
import org.adaptlab.chpir.android.survey.repositories.DisplayRepository;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.util.List;

public class DisplayQuestionViewModel extends AndroidViewModel {
    private LiveData<DisplayQuestion> mDisplayQuestion;

    public DisplayQuestionViewModel(@NonNull Application application, Long instrumentId, int position) {
        super(application);
        DisplayRepository displayRepository = new DisplayRepository(application);
        mDisplayQuestion = displayRepository.getDisplayQuestionDao().findByPosition(instrumentId, position);
    }

    public LiveData<DisplayQuestion> getDisplayQuestion() {
        return mDisplayQuestion;
    }

}
