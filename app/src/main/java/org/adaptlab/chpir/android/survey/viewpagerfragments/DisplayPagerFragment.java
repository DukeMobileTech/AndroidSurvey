package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.ResponseRelationAdapter;
import org.adaptlab.chpir.android.survey.entities.ConditionSkip;
import org.adaptlab.chpir.android.survey.entities.MultipleSkip;
import org.adaptlab.chpir.android.survey.entities.NextQuestion;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.entities.relations.ResponseRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.viewholders.QuestionViewHolder;
import org.adaptlab.chpir.android.survey.viewmodelfactories.QuestionRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.ResponseRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.QuestionRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.ResponseRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMPLETE_SURVEY;

public class DisplayPagerFragment extends Fragment {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    public final static String EXTRA_DISPLAY_ID = "org.adaptlab.chpir.android.survey.EXTRA_DISPLAY_ID";
    public final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";

    private static final String TAG = "DisplayPagerFragment";

    private ResponseRelationAdapter mResponseRelationAdapter;
    private List<Question> mQuestions;
    private List<ResponseRelation> mResponseRelations;
    private LongSparseArray<Response> mResponses;

    private SurveyViewModel mSurveyViewModel;
    private Long mInstrumentId;
    private Long mDisplayId;
    private String mSurveyUUID;

    private QuestionViewHolder.OnResponseSelectedListener mListener;

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
            public void onResponseSelected(QuestionRelation qr, Option selectedOption, List<Option> selectedOptions, String enteredValue, String nextQuestion) {
//                Log.i(TAG, "qid: " + qr.question.getQuestionIdentifier());
//                if (selectedOption != null) Log.i(TAG, "Res: " + selectedOption.getText());
//                if (nextQuestion != null) Log.i(TAG, "NQ: " + nextQuestion);

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
                                    Log.i(TAG, "To skip: " + curQuestion.getQuestionIdentifier());
                                    // Skip loop children questions
//                                for (Question question : loopChildren(curQuestion.getQuestionIdentifier())) {
//                                    skipList.add(question.getQuestionIdentifier());
//                                }
                                }
                                if (curQuestion.getQuestionIdentifier().equals(qr.question.getQuestionIdentifier()))
                                    toBeSkipped = true;
                            }
                        }
                    }
                    mSurveyViewModel.updateQuestionsToSkipMap(qr.question.getQuestionIdentifier() + "/skipTo", skipList);
                    mSurveyViewModel.updateQuestionsToSkipSet();
                }

                if (qr.multipleSkips != null && !qr.multipleSkips.isEmpty()) {
                    List<String> skipList = new ArrayList<>();
                    if (selectedOption != null) {
                        for (MultipleSkip multipleSkip : qr.multipleSkips) {
                            if (multipleSkip.getOptionIdentifier().equals(selectedOption.getIdentifier())) {
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
                    mSurveyViewModel.updateQuestionsToSkipSet();
                }
            }
        };
    }

    private void hideQuestions() {
        if (mResponseRelations == null) return;
        HashSet<String> hideSet = new HashSet<>();
        List<String> displayQuestionIds = new ArrayList<>();
        for (ResponseRelation rr : mResponseRelations) {
            displayQuestionIds.add(rr.response.getQuestionIdentifier());
        }
        for (String questionToSkip : mSurveyViewModel.getQuestionsToSkipSet()) {
            if (displayQuestionIds.contains(questionToSkip)) hideSet.add(questionToSkip);
        }
        List<ResponseRelation> visibleQuestions = new ArrayList<>();
        for (ResponseRelation rr : mResponseRelations) {
            if (!hideSet.contains(rr.response.getQuestionIdentifier())) {
                visibleQuestions.add(rr);
            }
        }
        mResponseRelationAdapter.submitList(visibleQuestions);
    }

    private void setQuestions() {
        if (mDisplayId == null || mInstrumentId == null) return;
        QuestionRelationViewModelFactory factory = new QuestionRelationViewModelFactory(getActivity().getApplication(), mInstrumentId, mDisplayId);
        QuestionRelationViewModel questionRelationViewModel = ViewModelProviders.of(this, factory).get(QuestionRelationViewModel.class);
        questionRelationViewModel.getQuestionRelations().observe(getViewLifecycleOwner(), new Observer<List<QuestionRelation>>() {
            @Override
            public void onChanged(@Nullable List<QuestionRelation> questionRelations) {
                mResponseRelationAdapter.setQuestionRelations(questionRelations);
                mQuestions = new ArrayList<>();
                for (QuestionRelation questionRelation : questionRelations) {
                    mQuestions.add(questionRelation.question);
                }
                initializeResponses();
            }
        });
    }

    private void setResponses() {
        if (mSurveyUUID == null || mDisplayId == null || mInstrumentId == null) return;
        ResponseRelationViewModelFactory factory = new ResponseRelationViewModelFactory(getActivity().getApplication(), mInstrumentId, mDisplayId, mSurveyUUID);
        ResponseRelationViewModel responseRelationViewModel = ViewModelProviders.of(this, factory).get(ResponseRelationViewModel.class);
        responseRelationViewModel.getResponseRelations().observe(getViewLifecycleOwner(), new Observer<List<ResponseRelation>>() {
            @Override
            public void onChanged(@Nullable List<ResponseRelation> responseRelations) {
                mResponseRelations = responseRelations;
                mResponses = new LongSparseArray<>();
                for (ResponseRelation responseRelation : responseRelations) {
                    Response response = responseRelation.response;
                    if (response != null) {
                        mResponses.put(responseRelation.response.getQuestionRemoteId(), response);
                    }
                }
                initializeResponses();
                hideQuestions();
            }
        });
    }

    private void setSurvey() {
        if (mSurveyUUID == null) return;
        SurveyViewModelFactory factory = new SurveyViewModelFactory(getActivity().getApplication(), mSurveyUUID);
        mSurveyViewModel = ViewModelProviders.of(getActivity(), factory).get(SurveyViewModel.class);
        mSurveyViewModel.getLiveDataSurvey().observe(getViewLifecycleOwner(), new Observer<Survey>() {
            @Override
            public void onChanged(@Nullable Survey survey) {
                if (mSurveyViewModel.getSurvey() == null) {
                    mSurveyViewModel.setSurvey(survey);
                    mSurveyViewModel.setSkipData();
                }
            }
        });
    }

    private void initializeResponses() {
        if (mSurveyUUID == null || mResponses == null || mQuestions == null) return;
        ResponseRepository responseRepository = new ResponseRepository(getActivity().getApplication());
        List<Response> responses = new ArrayList<>();
        for (Question question : mQuestions) {
            Response response = mResponses.get(question.getRemoteId());
            if (response == null) {
                response = new Response();
                response.setSurveyUUID(mSurveyUUID);
                response.setQuestionIdentifier(question.getQuestionIdentifier());
                response.setQuestionRemoteId(question.getRemoteId());
                response.setQuestionVersion(question.getQuestionVersion());
                response.setTimeStarted(new Date());
                responses.add(response);
            }
        }
        if (responses.size() > 0) {
            responseRepository.insertAll(responses);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_display, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.displayRecyclerView);
        mResponseRelationAdapter = new ResponseRelationAdapter(mListener);
        recyclerView.setAdapter(mResponseRelationAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        setSurvey();
        setQuestions();
        setResponses();

        return view;
    }

}
