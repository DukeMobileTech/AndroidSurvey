package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;

import java.util.List;

public class QuestionRelationRepository {
    public final String TAG = this.getClass().getName();
    private LiveData<List<QuestionRelation>> questions;

    public QuestionRelationRepository(Application application, Long instrumentId, Long displayId, final String surveyUUID) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "initialize: " + instrumentId + " " + displayId + " " + surveyUUID);
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        QuestionDao questionDao = db.questionDao();
        questions = questionDao.displayQuestions(instrumentId, displayId);
    }

    public LiveData<List<QuestionRelation>> getQuestions() {
        return questions;
    }
}
