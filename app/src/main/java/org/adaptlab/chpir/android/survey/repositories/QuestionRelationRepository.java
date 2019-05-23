package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;
import android.util.Log;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.SurveyDao;
import org.adaptlab.chpir.android.survey.daos.relations.DisplayQuestionDao;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.entities.relations.SurveyRelation;

import java.util.List;

public class QuestionRelationRepository {
    public final String TAG = this.getClass().getName();
    private LiveData<List<QuestionRelation>> questions;

    public QuestionRelationRepository(Application application, Long instrumentId, Long displayId, final String surveyUUID) {
        if (BuildConfig.DEBUG) Log.i(TAG, "initialize: " + instrumentId + " " + displayId + " " + surveyUUID);
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        DisplayQuestionDao displayQuestionDao = db.displayQuestionDao();
        questions = displayQuestionDao.displayQuestions(instrumentId, displayId);

        final SurveyRepository surveyRepository = new SurveyRepository(application);
        questions = Transformations.switchMap(questions, new Function<List<QuestionRelation>, LiveData<List<QuestionRelation>>>() {
            @Override
            public LiveData<List<QuestionRelation>> apply(final List<QuestionRelation> input) {
                final MediatorLiveData<List<QuestionRelation>> questionsMediatorLiveData = new MediatorLiveData<>();
                for (final QuestionRelation questionRelation : input) {
                    final Question question = questionRelation.question;
                    questionsMediatorLiveData.addSource(((SurveyDao) surveyRepository.getDao()).findByUUID(surveyUUID), new Observer<SurveyRelation>() {

                        @Override
                        public void onChanged(@Nullable SurveyRelation surveyRelation) {
//                            Log.i(TAG, "onChanged...");
                            question.setSurvey(surveyRelation.survey);
                            for (Response response : surveyRelation.responses) {
                                if (response.getQuestionIdentifier().equals(question.getQuestionIdentifier())) {
                                    question.setResponse(response);
                                }
                            }
                            questionsMediatorLiveData.postValue(input);
                        }
                    });
                }
                return questionsMediatorLiveData;
            }
        });

//        final ResponseRepository responseRepository = new ResponseRepository(application);
//        questions = Transformations.switchMap(questions, new Function<List<QuestionRelation>, LiveData<List<QuestionRelation>>>() {
//            @Override
//            public LiveData<List<QuestionRelation>> apply(final List<QuestionRelation> input) {
//                final MediatorLiveData<List<QuestionRelation>> questionsMediatorLiveData = new MediatorLiveData<>();
//                for (final QuestionRelation questionRelation : input) {
//                    final Question question = questionRelation.question;
//                    questionsMediatorLiveData.addSource(responseRepository.getResponseDao().find(surveyUUID, question.getQuestionIdentifier()), new Observer<Response>() {
//
//                        @Override
//                        public void onChanged(@Nullable Response response) {
//                            question.setResponse(response);
//                            questionsMediatorLiveData.postValue(input);
//                        }
//                    });
//                }
//                return questionsMediatorLiveData;
//            }
//        });
    }

    public LiveData<List<QuestionRelation>> getQuestions() {
        return questions;
    }
}
