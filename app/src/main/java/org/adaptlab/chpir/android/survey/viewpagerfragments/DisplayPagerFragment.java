package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import org.adaptlab.chpir.android.survey.adapters.QuestionAdapter;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.viewmodelfactories.QuestionRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.QuestionRelationViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DisplayPagerFragment extends Fragment {
    public final static String EXTRA_INSTRUMENT_ID = "org.adaptlab.chpir.android.survey.EXTRA_INSTRUMENT_ID";
    public final static String EXTRA_DISPLAY_ID = "org.adaptlab.chpir.android.survey.EXTRA_DISPLAY_ID";
    public final static String EXTRA_SURVEY_UUID = "org.adaptlab.chpir.android.survey.EXTRA_SURVEY_UUID";

    private static final String TAG = "DisplayPagerFragment";

    private QuestionAdapter mQuestionAdapter;
    private List<Question> mQuestions;
    private LongSparseArray<Response> mResponses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) return;
        long instrumentId = getArguments().getLong(EXTRA_INSTRUMENT_ID, -1);
        if (instrumentId == -1) return;
        long displayId = getArguments().getLong(EXTRA_DISPLAY_ID, -1);
        if (displayId == -1) return;
        String surveyUUID = getArguments().getString(EXTRA_SURVEY_UUID, null);
        if (surveyUUID == null) return;

        mQuestionAdapter = new QuestionAdapter();
        setQuestions(instrumentId, displayId, surveyUUID);
    }

    private void setQuestions(long instrumentId, long displayId, String surveyUUID) {
        QuestionRelationViewModelFactory factory = new QuestionRelationViewModelFactory(getActivity().getApplication(), instrumentId, displayId, surveyUUID);
        QuestionRelationViewModel questionRelationViewModel = ViewModelProviders.of(this, factory).get(QuestionRelationViewModel.class);
        questionRelationViewModel.getQuestionRelations().observe(this, new Observer<List<QuestionRelation>>() {
            @Override
            public void onChanged(@Nullable List<QuestionRelation> questionRelations) {
                mQuestionAdapter.submitList(questionRelations);
                Log.i(TAG, "Questions Count: " + questionRelations.size());
                mQuestions = new ArrayList<>();
                mResponses = new LongSparseArray<>();
                for (QuestionRelation questionRelation : questionRelations) {
                    mQuestions.add(questionRelation.question);
                    Response response = questionRelation.question.getResponse();
                    if (response != null) {
                        mResponses.put(questionRelation.question.getResponse().getQuestionRemoteId(), response);
                    }
                }
                if (mQuestions.get(0).getSurvey() != null) {
                    initializeResponses();
                }
            }
        });
    }

    private void initializeResponses() {
        ResponseRepository responseRepository = new ResponseRepository(getActivity().getApplication());
        List<Response> responses = new ArrayList<>();
        for (Question question : mQuestions) {
            Response response = mResponses.get(question.getRemoteId());
            if (response == null && question.getSurvey() != null) {
                response = new Response();
                response.setSurveyUUID(question.getSurvey().getUUID());
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
        if (mQuestionAdapter == null) mQuestionAdapter = new QuestionAdapter();
        recyclerView.setAdapter(mQuestionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    /**
     * Disable predictive animation in layout manager
     * Source: https://stackoverflow.com/questions/46563485/diffresult-dispatching-lead-to-inconsistency-detected-invalid-view-holder-adap
     */
    private class LinearLayoutManagerWrapper extends LinearLayoutManager {

        LinearLayoutManagerWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }

}
