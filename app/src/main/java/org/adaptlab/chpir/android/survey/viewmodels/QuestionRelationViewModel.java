package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.repositories.QuestionRelationRepository;

import java.util.List;

public class QuestionRelationViewModel extends AndroidViewModel {
    public final String TAG = this.getClass().getName();

//    private LiveData<List<Question>> mQuestions;

    private LiveData<List<QuestionRelation>> questionRelations;

    public QuestionRelationViewModel(@NonNull Application application, Long instrumentId, Long displayId, final String surveyUUID) {
        super(application);

        QuestionRelationRepository repository = new QuestionRelationRepository(application, instrumentId, displayId, surveyUUID);
        questionRelations = repository.getQuestions();

//        QuestionRepository questionRepository = new QuestionRepository(application);
//        mQuestions = ((QuestionDao) questionRepository.getDao()).displayQuestions(instrumentId, displayId);
//
//        final SurveyRepository surveyRepository = new SurveyRepository(application);
//        mQuestions = Transformations.switchMap(mQuestions, new Function<List<Question>, LiveData<List<Question>>>() {
//            @Override
//            public LiveData<List<Question>> apply(final List<Question> input) {
//                final MediatorLiveData<List<Question>> questionsMediatorLiveData = new MediatorLiveData<>();
//                for (final Question question : input) {
//                    questionsMediatorLiveData.addSource(((SurveyDao) surveyRepository.getDao()).findByUUID(surveyUUID), new Observer<Survey>() {
//
//                        @Override
//                        public void onChanged(@Nullable Survey survey) {
//                            question.setSurvey(survey);
//                            questionsMediatorLiveData.postValue(input);
//                        }
//                    });
//                }
//                return questionsMediatorLiveData;
//            }
//        });
//
//        final InstructionRepository instructionRepository = new InstructionRepository(application);
//        mQuestions = Transformations.switchMap(mQuestions, new Function<List<Question>, LiveData<List<Question>>>() {
//            @Override
//            public LiveData<List<Question>> apply(final List<Question> input) {
//                final MediatorLiveData<List<Question>> questionsMediatorLiveData = new MediatorLiveData<>();
//                for (final Question question : input) {
//                    questionsMediatorLiveData.addSource(((InstructionDao) instructionRepository.getDao()).findById(question.getInstructionId()), new Observer<Instruction>() {
//
//                        @Override
//                        public void onChanged(@Nullable Instruction instruction) {
//                            question.setInstruction(instruction);
//                            questionsMediatorLiveData.postValue(input);
//                        }
//                    });
//                }
//                return questionsMediatorLiveData;
//            }
//        });
//
//        final OptionSetRepository optionSetRepository = new OptionSetRepository(application);
//        mQuestions = Transformations.switchMap(mQuestions, new Function<List<Question>, LiveData<List<Question>>>() {
//            @Override
//            public LiveData<List<Question>> apply(final List<Question> input) {
//                final MediatorLiveData<List<Question>> questionsMediatorLiveData = new MediatorLiveData<>();
//                for (final Question question : input) {
//                    questionsMediatorLiveData.addSource(((OptionSetDao) optionSetRepository.getDao()).options(question.getRemoteOptionSetId()), new Observer<List<QuestionOption>>() {
//                        @Override
//                        public void onChanged(@Nullable List<QuestionOption> options) {
//                            question.setOptions(options);
//                            questionsMediatorLiveData.postValue(input);
//                        }
//                    });
//                }
//                return questionsMediatorLiveData;
//            }
//        });
//
//        final OptionSetRepository specialOptionSetRepository = new OptionSetRepository(application);
//        mQuestions = Transformations.switchMap(mQuestions, new Function<List<Question>, LiveData<List<Question>>>() {
//            @Override
//            public LiveData<List<Question>> apply(final List<Question> input) {
//                final MediatorLiveData<List<Question>> questionsMediatorLiveData = new MediatorLiveData<>();
//                for (final Question question : input) {
//                    questionsMediatorLiveData.addSource(((OptionSetDao) specialOptionSetRepository.getDao()).options(question.getRemoteSpecialOptionSetId()), new Observer<List<QuestionOption>>() {
//
//                        @Override
//                        public void onChanged(@Nullable List<QuestionOption> options) {
//                            question.setSpecialOptions(options);
//                            questionsMediatorLiveData.postValue(input);
//                        }
//                    });
//                }
//                return questionsMediatorLiveData;
//            }
//        });
//
//        final DisplayInstructionRepository displayInstructionRepository = new DisplayInstructionRepository(application);
//        mQuestions = Transformations.switchMap(mQuestions, new Function<List<Question>, LiveData<List<Question>>>() {
//            @Override
//            public LiveData<List<Question>> apply(final List<Question> input) {
//                final MediatorLiveData<List<Question>> questionsMediatorLiveData = new MediatorLiveData<>();
//                for (final Question question : input) {
//                    questionsMediatorLiveData.addSource(((DisplayInstructionDao) displayInstructionRepository.getDao()).questionDisplayInstructions(question.getDisplayId(),
//                            question.getNumberInInstrument()), new Observer<List<QuestionDisplayInstruction>>() {
//
//                        @Override
//                        public void onChanged(@Nullable List<QuestionDisplayInstruction> displayInstructions) {
//                            question.setDisplayInstructions(displayInstructions);
//                            questionsMediatorLiveData.postValue(input);
//                        }
//                    });
//                }
//                return questionsMediatorLiveData;
//            }
//        });
//
//        final ResponseRepository responseRepository = new ResponseRepository(application);
//        mQuestions = Transformations.switchMap(mQuestions, new Function<List<Question>, LiveData<List<Question>>>() {
//            @Override
//            public LiveData<List<Question>> apply(final List<Question> input) {
//                final MediatorLiveData<List<Question>> questionsMediatorLiveData = new MediatorLiveData<>();
//                for (final Question question : input) {
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
//
    }

//    public LiveData<List<Question>> getQuestions() {
//        return mQuestions;
//    }

    public LiveData<List<QuestionRelation>> getQuestionRelations() {
        return questionRelations;
    }
}
