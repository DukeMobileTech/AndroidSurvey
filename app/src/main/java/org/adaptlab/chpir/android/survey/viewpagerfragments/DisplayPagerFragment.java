package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.DisplayAdapter;
import org.adaptlab.chpir.android.survey.adapters.ResponseRelationAdapter;
import org.adaptlab.chpir.android.survey.adapters.ResponseRelationTableAdapter;
import org.adaptlab.chpir.android.survey.entities.LoopQuestion;
import org.adaptlab.chpir.android.survey.entities.MultipleSkip;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolder;
import org.adaptlab.chpir.android.survey.viewmodelfactories.QuestionRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.QuestionRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.adaptlab.chpir.android.survey.models.Instrument.LOOP_MAX;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMPLETE_SURVEY;

public class DisplayPagerFragment extends Fragment {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    public final static String EXTRA_DISPLAY_ID = "org.adaptlab.chpir.android.survey.EXTRA_DISPLAY_ID";
    public final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";

    private static final String TAG = DisplayPagerFragment.class.getName();

    private SurveyViewModel mSurveyViewModel;
    private Long mInstrumentId;
    private Long mDisplayId;
    private String mSurveyUUID;

    private QuestionViewHolder.OnResponseSelectedListener mListener;
    private DisplayAdapter mDisplayAdapter;
    private List<List<QuestionRelation>> mQuestionRelationGroups;
    private List<ResponseRelationAdapter> mResponseRelationAdapters;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) return;
        mInstrumentId = getArguments().getLong(EXTRA_INSTRUMENT_ID);
        mDisplayId = getArguments().getLong(EXTRA_DISPLAY_ID);
        mSurveyUUID = getArguments().getString(EXTRA_SURVEY_UUID);
        setOnResponseSelectedListener();
    }

    private void setOnResponseSelectedListener() {
        mListener = new QuestionViewHolder.OnResponseSelectedListener() {
            @Override
            public void onResponseSelected(QuestionRelation qr, Option selectedOption, List<Option> selectedOptions, String enteredValue, String nextQuestion, String text) {
                setNextQuestions(qr, nextQuestion);
                setMultipleSkips(qr, selectedOption, selectedOptions, enteredValue);
                setQuestionLoops(qr, text);
                mSurveyViewModel.updateQuestionsToSkipSet();
            }

            private void setQuestionLoops(QuestionRelation qr, String text) {
                if (qr.loopQuestions != null && !qr.loopQuestions.isEmpty()) {
                    if (qr.question.getQuestionType().equals(Question.INTEGER)) {
                        List<String> questionsToHide = new ArrayList<>();
                        int start = 0;
                        if (!TextUtils.isEmpty(text)) {
                            start = Integer.parseInt(text);
                        }
                        for (int k = start + 1; k <= LOOP_MAX; k++) {
                            for (LoopQuestion lq : qr.loopQuestions) {
                                String id = qr.question.getQuestionIdentifier() + "_" + lq.getLooped() + "_" + k;
                                questionsToHide.add(id);
                            }
                        }
                        mSurveyViewModel.updateQuestionsToSkipMap(qr.question.getQuestionIdentifier() + "/intLoop", questionsToHide);
                    } else if (qr.question.isMultipleResponseLoop()) {
                        List<String> responses;
                        if (qr.question.isListResponse()) {
                            responses = Arrays.asList(text.split(COMMA, -1)); // Keep empty values
                        } else {
                            responses = Arrays.asList(text.split(COMMA)); // Ignore empty values
                        }
                        List<String> questionsToHide = new ArrayList<>();
                        int optionsSize = qr.optionSets.get(0).optionSetOptions.size() - 1;
                        if (qr.question.isOtherQuestionType()) {
                            optionsSize += 1;
                        }
                        for (int k = 0; k <= optionsSize; k++) {
                            for (LoopQuestion lq : qr.loopQuestions) {
                                if (!TextUtils.isEmpty(lq.getLooped())) {
                                    String id = qr.question.getQuestionIdentifier() + "_" + lq.getLooped() + "_" + k;
                                    if (qr.question.isMultipleResponse()) {
                                        if (!responses.contains(String.valueOf(k))) {
                                            questionsToHide.add(id);
                                        }
                                    } else if (qr.question.isListResponse()) {
                                        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(responses.get(k))) {
                                            questionsToHide.add(id);
                                        }
                                    }
                                }
                            }
                        }
                        mSurveyViewModel.updateQuestionsToSkipMap(qr.question.getQuestionIdentifier() + "/multiLoop", questionsToHide);
                    }
                }
            }

            private void setMultipleSkips(QuestionRelation qr, Option selectedOption, List<Option> selectedOptions, String enteredValue) {
                if (qr.multipleSkips != null && !qr.multipleSkips.isEmpty()) {
                    List<String> skipList = new ArrayList<>();
                    if (selectedOption != null && selectedOption.getIdentifier() != null) {
                        for (MultipleSkip multipleSkip : qr.multipleSkips) {
                            if (multipleSkip.getOptionIdentifier() != null &&
                                    multipleSkip.getOptionIdentifier().equals(selectedOption.getIdentifier())) {
                                skipList.add(multipleSkip.getSkipQuestionIdentifier());
                            }
                        }
                    }
                    if (enteredValue != null) {
                        for (MultipleSkip multipleSkip : qr.multipleSkips) {
                            if (multipleSkip.getValue().equals(enteredValue)) {
                                skipList.add(multipleSkip.getSkipQuestionIdentifier());
                            }
                        }
                    }
                    mSurveyViewModel.updateQuestionsToSkipMap(qr.question.getQuestionIdentifier() + "/multi", skipList);
                    if (!selectedOptions.isEmpty()) {
                        HashSet<String> skipSet = new HashSet<>();
                        for (Option option : selectedOptions) {
                            for (MultipleSkip multipleSkip : qr.multipleSkips) {
                                if (multipleSkip.getOptionIdentifier().equals(option.getIdentifier())) {
                                    skipSet.add(multipleSkip.getSkipQuestionIdentifier());
                                }
                            }
                        }
                        mSurveyViewModel.updateQuestionsToSkipMap(qr.question.getQuestionIdentifier() + "/multi", new ArrayList<>(skipSet));
                    }
                }
            }

            private void setNextQuestions(QuestionRelation qr, String nextQuestion) {
                if (qr.nextQuestions != null && !qr.nextQuestions.isEmpty()) {
                    List<String> skipList = new ArrayList<>();
                    if (nextQuestion != null) {
                        if (nextQuestion.equals(COMPLETE_SURVEY)) {
                            List<String> questions = new ArrayList<>();
                            for (Question q : mSurveyViewModel.getQuestions()) {
                                questions.add(q.getQuestionIdentifier());
                            }
                            skipList = new ArrayList<>(questions.subList(questions.indexOf(qr.question.getQuestionIdentifier()) + 1, questions.size()));
                        } else {
                            boolean toBeSkipped = false;
                            for (Question curQuestion : mSurveyViewModel.getQuestions()) {
                                if (curQuestion.getQuestionIdentifier().equals(nextQuestion)) {
                                    break;
                                }
                                if (toBeSkipped) {
                                    skipList.add(curQuestion.getQuestionIdentifier());
                                }
                                if (curQuestion.getQuestionIdentifier().equals(qr.question.getQuestionIdentifier()))
                                    toBeSkipped = true;
                            }
                        }
                    }
                    mSurveyViewModel.updateQuestionsToSkipMap(qr.question.getQuestionIdentifier() + "/skipTo", skipList);
                }
            }
        };
    }

    private void hideQuestions() {
        if (mQuestionRelationGroups == null) return;
        HashSet<String> hideSet = new HashSet<>();
        List<String> displayQuestionIds = new ArrayList<>();
        for (List<QuestionRelation> relationList : mQuestionRelationGroups) {
            for (QuestionRelation questionRelation : relationList) {
                displayQuestionIds.add(questionRelation.question.getQuestionIdentifier());
            }
        }
        for (String questionToSkip : mSurveyViewModel.getQuestionsToSkipSet()) {
            if (displayQuestionIds.contains(questionToSkip)) {
                hideSet.add(questionToSkip);
            }
        }
        List<List<QuestionRelation>> visibleRelations = new ArrayList<>();
        for (List<QuestionRelation> relationList : mQuestionRelationGroups) {
            List<QuestionRelation> questionRelations = new ArrayList<>();
            for (QuestionRelation questionRelation : relationList) {
                if (!hideSet.contains(questionRelation.question.getQuestionIdentifier())) {
                    questionRelations.add(questionRelation);
                }
            }
            visibleRelations.add(questionRelations);
        }

        mDisplayAdapter.submitList(visibleRelations);
    }

    private void setQuestions() {
        if (mDisplayId == null || mInstrumentId == null) return;
        QuestionRelationViewModelFactory factory = new QuestionRelationViewModelFactory(getActivity().getApplication(), mInstrumentId, mDisplayId);
        QuestionRelationViewModel questionRelationViewModel = ViewModelProviders.of(this, factory).get(QuestionRelationViewModel.class);
        questionRelationViewModel.getQuestionRelations().observe(getViewLifecycleOwner(), new Observer<List<QuestionRelation>>() {
            @Override
            public void onChanged(@Nullable List<QuestionRelation> questionRelations) {
                for (QuestionRelation questionRelation : questionRelations) {
                    for (Response response : questionRelation.responses) {
                        if (response.getSurveyUUID().equals(mSurveyUUID)) {
                            questionRelation.response = response;
                            break;
                        }
                    }
                    hideLoopedQuestions(questionRelation);
                }

                Collections.sort(questionRelations, new Comparator<QuestionRelation>() {
                    @Override
                    public int compare(QuestionRelation o1, QuestionRelation o2) {
                        if (o1.question.getNumberInInstrument() < o2.question.getNumberInInstrument())
                            return -1;
                        if (o1.question.getNumberInInstrument() > o2.question.getNumberInInstrument())
                            return 1;
                        return 0;
                    }
                });

                if (questionRelations.get(0).response == null) {
                    initializeResponses(questionRelations);
                } else {
                    groupQuestionRelations(questionRelations);
                    setAdapters();
                    hideQuestions();
                }
            }
        });
    }

    private void initializeResponses(List<QuestionRelation> questionRelations) {
        if (mSurveyUUID == null) return;
        ResponseRepository responseRepository = new ResponseRepository(getActivity().getApplication());
        List<Response> responses = new ArrayList<>();
        for (QuestionRelation questionRelation : questionRelations) {
            Response response = questionRelation.response;
            if (response == null) {
                response = new Response();
                response.setSurveyUUID(mSurveyUUID);
                response.setQuestionIdentifier(questionRelation.question.getQuestionIdentifier());
                response.setQuestionRemoteId(questionRelation.question.getRemoteId());
                response.setQuestionVersion(questionRelation.question.getQuestionVersion());
                response.setTimeStarted(new Date());
                responses.add(response);
            }
        }
        if (responses.size() > 0) {
            responseRepository.insertAll(responses);
        }
    }

    private void groupQuestionRelations(List<QuestionRelation> questionRelations) {
        String tableName = questionRelations.get(0).question.getTableIdentifier();
        mQuestionRelationGroups = new ArrayList<>();
        List<QuestionRelation> group = new ArrayList<>();
        for (QuestionRelation qr : questionRelations) {
            if ((tableName == null && qr.question.getTableIdentifier() == null) ||
                    (tableName != null && tableName.equals(qr.question.getTableIdentifier()))) {
                group.add(qr);
            } else {
                tableName = qr.question.getTableIdentifier();
                mQuestionRelationGroups.add(new ArrayList<>(group));
                group.clear();
                group.add(qr);
            }
            if (questionRelations.indexOf(qr) == questionRelations.size() - 1) {
                mQuestionRelationGroups.add(new ArrayList<>(group));
            }
        }
    }

    private void setAdapters() {
        if (mResponseRelationAdapters == null || mResponseRelationAdapters.size() != mQuestionRelationGroups.size()) {
            mResponseRelationAdapters = new ArrayList<>();
            for (List<QuestionRelation> list : mQuestionRelationGroups) {
                if (TextUtils.isEmpty(list.get(0).question.getTableIdentifier())) {
                    mResponseRelationAdapters.add(new ResponseRelationAdapter(mListener));
                } else {
                    mResponseRelationAdapters.add(new ResponseRelationTableAdapter(mListener));
                }
            }
            mDisplayAdapter.setResponseRelationAdapters(mResponseRelationAdapters);
        }
    }

    private void hideLoopedQuestions(QuestionRelation questionRelation) {
        if (questionRelation.loopedQuestions.size() > 0) {
            List<String> questionsToHide = new ArrayList<>();
            for (LoopQuestion lq : questionRelation.loopedQuestions) {
                questionsToHide.add(lq.getLooped());
            }
            mSurveyViewModel.updateQuestionsToSkipMap(questionRelation.question.getQuestionIdentifier() + "/looped", questionsToHide);
        }
    }

    private void setSurvey() {
        if (mSurveyUUID == null) return;
        SurveyViewModelFactory factory = new SurveyViewModelFactory(getActivity().getApplication(), mSurveyUUID);
        mSurveyViewModel = ViewModelProviders.of(getActivity(), factory).get(SurveyViewModel.class);
        mSurveyViewModel.getLiveDataSurvey().observe(getViewLifecycleOwner(), new Observer<Survey>() {
            @Override
            public void onChanged(@Nullable Survey survey) {
                if (survey != null && mSurveyViewModel.getSurvey() == null) {
                    mSurveyViewModel.setSurvey(survey);
                    mSurveyViewModel.setSkipData();
                }
            }
        });
        mDisplayAdapter.setSurveyViewModel(mSurveyViewModel);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_display, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.displayRecyclerView);
        mDisplayAdapter = new DisplayAdapter(getContext());
        recyclerView.setAdapter(mDisplayAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setSurvey();
        setQuestions();
        return view;
    }

}
