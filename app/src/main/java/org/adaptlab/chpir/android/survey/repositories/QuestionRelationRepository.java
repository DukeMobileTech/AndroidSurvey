package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;

import java.util.List;

public class QuestionRelationRepository {
    public final String TAG = this.getClass().getName();
    private LiveData<List<QuestionRelation>> questions;

    public QuestionRelationRepository(Application application, Long instrumentId, Long displayId) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        QuestionDao questionDao = db.questionDao();
        questions = questionDao.displayQuestions(instrumentId, displayId);
    }

    public LiveData<List<QuestionRelation>> getQuestions() {
        return questions;
    }
}
