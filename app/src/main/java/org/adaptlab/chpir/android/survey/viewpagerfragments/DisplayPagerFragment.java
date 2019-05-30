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
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.ResponseRelationAdapter;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.entities.relations.ResponseRelation;
import org.adaptlab.chpir.android.survey.entities.relations.SurveyRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.viewmodelfactories.QuestionRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.ResponseRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodelfactories.SurveyViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.QuestionRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.ResponseRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DisplayPagerFragment extends Fragment {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    public final static String EXTRA_DISPLAY_ID = "org.adaptlab.chpir.android.survey.EXTRA_DISPLAY_ID";
    public final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";

    private static final String TAG = "DisplayPagerFragment";

    private ResponseRelationAdapter mResponseRelationAdapter;
    private List<Question> mQuestions;
    private LongSparseArray<Response> mResponses;
    private Survey mSurvey;

    private Long mInstrumentId;
    private Long mDisplayId;
    private String mSurveyUUID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) return;
        mInstrumentId = getArguments().getLong(EXTRA_INSTRUMENT_ID);
        mDisplayId = getArguments().getLong(EXTRA_DISPLAY_ID);
        mSurveyUUID = getArguments().getString(EXTRA_SURVEY_UUID);
    }

    private void setQuestions() {
        if (mSurveyUUID == null || mDisplayId == null || mInstrumentId == null) return;
        QuestionRelationViewModelFactory factory = new QuestionRelationViewModelFactory(getActivity().getApplication(), mInstrumentId, mDisplayId, mSurveyUUID);
        QuestionRelationViewModel questionRelationViewModel = ViewModelProviders.of(this, factory).get(QuestionRelationViewModel.class);
        questionRelationViewModel.getQuestionRelations().observe(this, new Observer<List<QuestionRelation>>() {
            @Override
            public void onChanged(@Nullable List<QuestionRelation> questionRelations) {
                Log.i(TAG, "Questions Count: " + questionRelations.size());
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
        responseRelationViewModel.getResponseRelations().observe(this, new Observer<List<ResponseRelation>>() {
            @Override
            public void onChanged(@Nullable List<ResponseRelation> responseRelations) {
                Log.i(TAG, "Responses Count: " + responseRelations.size());
                mResponseRelationAdapter.submitList(responseRelations);
                mResponses = new LongSparseArray<>();
                for (ResponseRelation responseRelation : responseRelations) {
                    Response response = responseRelation.response;
                    if (response != null) {
                        mResponses.put(responseRelation.response.getQuestionRemoteId(), response);
                    }
                }
                initializeResponses();
            }
        });
    }

    private void setSurvey() {
        if (mSurveyUUID == null) return;
        SurveyViewModelFactory factory = new SurveyViewModelFactory(getActivity().getApplication(), mSurveyUUID);
        SurveyViewModel surveyViewModel = ViewModelProviders.of(getActivity(), factory).get(SurveyViewModel.class);
        surveyViewModel.getSurveyRelation().observe(this, new Observer<SurveyRelation>() {
            @Override
            public void onChanged(@Nullable SurveyRelation surveyRelation) {
                mSurvey = surveyRelation.survey;
            }
        });
    }

    private void initializeResponses() {
        if (mSurvey == null || mResponses == null || mQuestions == null) return;
        ResponseRepository responseRepository = new ResponseRepository(getActivity().getApplication());
        List<Response> responses = new ArrayList<>();
        for (Question question : mQuestions) {
            Response response = mResponses.get(question.getRemoteId());
            if (response == null && mSurvey != null) {
                response = new Response();
                response.setSurveyUUID(mSurvey.getUUID());
                response.setQuestionIdentifier(question.getQuestionIdentifier());
                response.setQuestionRemoteId(question.getRemoteId());
                response.setQuestionVersion(question.getQuestionVersion());
                response.setTimeStarted(new Date());
                responses.add(response);
            }
        }
        if (responses.size() > 0) {
            Log.i(TAG, "INSERT COUNT: " + responses.size());
            responseRepository.insertAll(responses);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.recycler_view_display, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.displayRecyclerView);
        mResponseRelationAdapter = new ResponseRelationAdapter();
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
