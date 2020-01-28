package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.entities.SurveyNote;
import org.adaptlab.chpir.android.survey.repositories.SurveyNoteRepository;

import java.util.List;

public class SurveyNoteViewModel extends AndroidViewModel {
    private LiveData<List<SurveyNote>> mSurveyNotes;
    private SurveyNoteRepository mSurveyNoteRepository;

    public SurveyNoteViewModel(@NonNull Application application, String uuid) {
        super(application);
        mSurveyNoteRepository = new SurveyNoteRepository(application, uuid);
        mSurveyNotes = mSurveyNoteRepository.getSurveyNotes();
    }

    public LiveData<List<SurveyNote>> getSurveyNotes() {
        return mSurveyNotes;
    }

    public void insert(SurveyNote surveyNote) {
        mSurveyNoteRepository.insert(surveyNote);
    }

    public void update(SurveyNote surveyNote) {
        mSurveyNoteRepository.update(surveyNote);
    }

}
