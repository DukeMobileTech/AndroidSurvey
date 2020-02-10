package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.QuestionRelationAdapter;
import org.adaptlab.chpir.android.survey.entities.LoopQuestion;
import org.adaptlab.chpir.android.survey.entities.MultipleSkip;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolder;
import org.adaptlab.chpir.android.survey.viewmodelfactories.DisplayViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.QuestionRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.DisplayViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.QuestionRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMPLETE_SURVEY;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.LOOP_MAX;

public class DisplayPagerFragment extends Fragment {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    public final static String EXTRA_DISPLAY_ID = "org.adaptlab.chpir.android.survey.EXTRA_DISPLAY_ID";
    public final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";

    private static final String TAG = DisplayPagerFragment.class.getName();

    private SurveyViewModel mSurveyViewModel;
    private DisplayViewModel mDisplayViewModel;
    private Long mInstrumentId;
    private Long mDisplayId;
    private String mSurveyUUID;

    private QuestionViewHolder.OnResponseSelectedListener mListener;
    private QuestionRelationAdapter mQuestionRelationsAdapter;
    private List<QuestionRelation> mQuestionRelations;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mInstrumentId = getArguments().getLong(EXTRA_INSTRUMENT_ID);
            mDisplayId = getArguments().getLong(EXTRA_DISPLAY_ID);
            mSurveyUUID = getArguments().getString(EXTRA_SURVEY_UUID);
        }
        setOnResponseSelectedListener();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_DISPLAY_ID, mDisplayId);
        outState.putLong(EXTRA_INSTRUMENT_ID, mInstrumentId);
        outState.putString(EXTRA_SURVEY_UUID, mSurveyUUID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mInstrumentId = savedInstanceState.getLong(EXTRA_INSTRUMENT_ID);
            mDisplayId = savedInstanceState.getLong(EXTRA_DISPLAY_ID);
            mSurveyUUID = savedInstanceState.getString(EXTRA_SURVEY_UUID);
        }
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
        HashSet<String> hideSet = new HashSet<>();
        List<String> displayQuestionIds = new ArrayList<>();
        for (QuestionRelation questionRelation : mQuestionRelations) {
            displayQuestionIds.add(questionRelation.question.getQuestionIdentifier());
        }
        for (String questionToSkip : mSurveyViewModel.getQuestionsToSkipSet()) {
            if (displayQuestionIds.contains(questionToSkip)) {
                hideSet.add(questionToSkip);
            }
        }
        List<QuestionRelation> visibleRelations = new ArrayList<>();
        for (QuestionRelation questionRelation : mQuestionRelations) {
            if (!hideSet.contains(questionRelation.question.getQuestionIdentifier())) {
                visibleRelations.add(questionRelation);
            }
        }
        boolean responsesPresent = true;
        for (QuestionRelation questionRelation : visibleRelations) {
            if (mDisplayViewModel.getResponse(questionRelation.question.getQuestionIdentifier()) == null) {
                responsesPresent = false;
                break;
            }
        }

        if (responsesPresent) {
            mQuestionRelationsAdapter.submitList(visibleRelations);
        }
    }

    private void setQuestions() {
        if (getActivity() == null || mDisplayId == null || mInstrumentId == null) return;
        QuestionRelationViewModelFactory factory = new QuestionRelationViewModelFactory(getActivity().getApplication(), mInstrumentId, mDisplayId);
        QuestionRelationViewModel questionRelationViewModel = ViewModelProviders.of(this, factory).get(QuestionRelationViewModel.class);
        questionRelationViewModel.getQuestionRelations().observe(getViewLifecycleOwner(), new Observer<List<QuestionRelation>>() {
            @Override
            public void onChanged(@Nullable List<QuestionRelation> questionRelations) {
                if (questionRelations == null) return;
                for (QuestionRelation questionRelation : questionRelations) {
                    for (Response response : questionRelation.responses) {
                        if (response.getSurveyUUID().equals(mSurveyUUID)) {
                            if (mDisplayViewModel.getResponse(response.getQuestionIdentifier()) == null) {
                                mDisplayViewModel.setResponse(response.getQuestionIdentifier(), response);
                            }
                            break;
                        }
                    }
                    hideLoopedQuestions(questionRelation);
                }

                Collections.sort(questionRelations, new Comparator<QuestionRelation>() {
                    @Override
                    public int compare(QuestionRelation o1, QuestionRelation o2) {
                        return o1.question.getNumberInInstrument() - o2.question.getNumberInInstrument();
                    }
                });

                if (questionRelations.size() > 0) {
                    if (mDisplayViewModel.getResponse(questionRelations.get(0).question.getQuestionIdentifier()) == null) {
                        initializeResponses(questionRelations);
                    } else {
                        mQuestionRelations = questionRelations;
                        hideQuestions();
                    }
                }
            }
        });
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

    private void initializeResponses(List<QuestionRelation> questionRelations) {
        if (getActivity() == null || mSurveyUUID == null) return;
        ResponseRepository responseRepository = new ResponseRepository(getActivity().getApplication());
        List<Response> responses = new ArrayList<>();
        for (QuestionRelation questionRelation : questionRelations) {
            Response response = mDisplayViewModel.getResponse(questionRelation.question.getQuestionIdentifier());
            if (response == null) {
                response = new Response();
                response.setSurveyUUID(mSurveyUUID);
                response.setQuestionIdentifier(questionRelation.question.getQuestionIdentifier());
                response.setQuestionRemoteId(questionRelation.question.getRemoteId());
                response.setQuestionVersion(questionRelation.question.getQuestionVersion());
                response.setTimeStarted(new Date());
                if (!TextUtils.isEmpty(questionRelation.question.getDefaultResponse())) {
                    response.setText(questionRelation.question.getDefaultResponse());
                }
                responses.add(response);
            }
        }
        if (responses.size() > 0) {
            responseRepository.insertAll(responses);
        }
    }

    private void setSurvey() {
        if (getActivity() == null || mSurveyUUID == null) return;
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
        mQuestionRelationsAdapter = new QuestionRelationAdapter(mListener, mSurveyViewModel);
        if (mDisplayViewModel != null)
            mQuestionRelationsAdapter.setDisplayViewModel(mDisplayViewModel);
        mRecyclerView.setAdapter(mQuestionRelationsAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setDisplayViewModel() {
        if (getActivity() == null || mDisplayId == null) return;
        DisplayViewModelFactory factory = new DisplayViewModelFactory(getActivity().getApplication(), mDisplayId);
        mDisplayViewModel = ViewModelProviders.of(this, factory).get(DisplayViewModel.class);
        if (mQuestionRelationsAdapter != null)
            mQuestionRelationsAdapter.setDisplayViewModel(mDisplayViewModel);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_display, container, false);
        mRecyclerView = view.findViewById(R.id.displayQuestionsRecyclerView);
        mQuestionRelations = new ArrayList<>();
        setDisplayViewModel();
        setSurvey();
        setQuestions();
        return view;
    }

}
