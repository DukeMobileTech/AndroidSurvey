package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.adaptlab.chpir.android.survey.InstrumentActivity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.SurveyResponseAdapter;
import org.adaptlab.chpir.android.survey.entities.relations.SurveyResponse;
import org.adaptlab.chpir.android.survey.tasks.SubmitSurveyTask;
import org.adaptlab.chpir.android.survey.utils.looper.ItemTouchHelperExtension;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyResponseViewModel;

import java.util.ArrayList;
import java.util.List;

public class SurveyViewPagerFragment extends Fragment {
    private static final String TAG = "SurveyViewPagerFragment";

    private RecyclerView mInProgressRecyclerView;
    private RecyclerView mCompletedRecyclerView;
    private RecyclerView mSubmittedRecyclerView;

    private SurveyResponseAdapter inProgressSurveysAdapter;
    private SurveyResponseAdapter completedSurveysAdapter;
    private SurveyResponseAdapter submittedSurveysAdapter;

    private List<SurveyResponse> inProgressSurveys;
    private List<SurveyResponse> completedSurveys;
    private List<SurveyResponse> submittedSurveys;

    private Button mSubmitAll;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setSurveyLists();
        View view = inflater.inflate(R.layout.survey_recycler_view, container, false);

        mInProgressRecyclerView = view.findViewById(R.id.onGoingSurveys);
        mCompletedRecyclerView = view.findViewById(R.id.completedSurveys);
        mSubmittedRecyclerView = view.findViewById(R.id.submittedSurveys);
        mSubmitAll = view.findViewById(R.id.submitAll);

        toggleHeadersVisibility(view);
        submitAll();

        inProgressSurveysAdapter = new SurveyResponseAdapter(getContext());
        completedSurveysAdapter = new SurveyResponseAdapter(getContext());
        submittedSurveysAdapter = new SurveyResponseAdapter(getContext());

        mInProgressRecyclerView.setAdapter(inProgressSurveysAdapter);
        mCompletedRecyclerView.setAdapter(completedSurveysAdapter);
        mSubmittedRecyclerView.setAdapter(submittedSurveysAdapter);

        mInProgressRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCompletedRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSubmittedRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mInProgressRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        mInProgressRecyclerView.addItemDecoration(dividerItemDecoration);
        DividerItemDecoration dividerItemDecoration1 = new DividerItemDecoration(
                mCompletedRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration1.setDrawable(getResources().getDrawable(R.drawable.border));
        mCompletedRecyclerView.addItemDecoration(dividerItemDecoration1);
        DividerItemDecoration dividerItemDecoration2 = new DividerItemDecoration(
                mSubmittedRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration2.setDrawable(getResources().getDrawable(R.drawable.border));
        mSubmittedRecyclerView.addItemDecoration(dividerItemDecoration2);

        setSurveyLeftSwipe();
        return view;
    }

    private void toggleHeadersVisibility(View view) {
        if (inProgressSurveys.size() == 0)
            view.findViewById(R.id.progressHeader).setVisibility(View.GONE);
        if (completedSurveys.size() == 0) {
            view.findViewById(R.id.completedHeader).setVisibility(View.GONE);
            view.findViewById(R.id.submitAll).setVisibility(View.GONE);
        }
        if (submittedSurveys.size() == 0)
            view.findViewById(R.id.submittedHeader).setVisibility(View.GONE);
    }

    private void submitAll() {
        mSubmitAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.submit_survey)
                        .setMessage(R.string.submit_survey_message)
                        .setPositiveButton(R.string.submit,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        for (SurveyResponse survey : completedSurveys) {
                                            completedSurveysAdapter.prepareForSubmission(survey);
                                        }
                                        new SubmitSurveyTask(getActivity()).execute();
                                        startActivity(new Intent(getActivity(), InstrumentActivity.class));
                                        getActivity().finish();
                                    }
                                })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                })
                        .show();
            }
        });
    }

    private void setSurveyLists() {
        inProgressSurveys = new ArrayList<>();
        completedSurveys = new ArrayList<>();
        submittedSurveys = new ArrayList<>();

        SurveyResponseViewModel viewModel = ViewModelProviders.of(getActivity()).get(SurveyResponseViewModel.class);
        viewModel.getSurveys().observe(this, new Observer<List<SurveyResponse>>() {
            @Override
            public void onChanged(@Nullable List<SurveyResponse> surveys) {
                Log.i(TAG, "SurveyResponse Count: " + surveys.size());
                for (SurveyResponse surveyResponse : surveys) {
                    if (surveyResponse.survey.isSent() || surveyResponse.survey.isQueued()) {
                        submittedSurveys.add(surveyResponse);
                        submittedSurveysAdapter.setSurveys(submittedSurveys);
                    } else if (surveyResponse.survey.isComplete()) {
                        completedSurveys.add(surveyResponse);
                        completedSurveysAdapter.setSurveys(completedSurveys);
                    } else {
                        inProgressSurveys.add(surveyResponse);
                        inProgressSurveysAdapter.setSurveys(inProgressSurveys);
                    }
                }
            }
        });
    }

    private void setSurveyLeftSwipe() {
        ItemTouchHelperExtension mItemTouchHelper = new ItemTouchHelperExtension(new ItemTouchHelperCallback());
        ItemTouchHelperExtension mItemTouchHelper1 = new ItemTouchHelperExtension(new ItemTouchHelperCallback());
        ItemTouchHelperExtension mItemTouchHelper2 = new ItemTouchHelperExtension(new ItemTouchHelperCallback());
        mItemTouchHelper.attachToRecyclerView(mInProgressRecyclerView);
        mItemTouchHelper1.attachToRecyclerView(mCompletedRecyclerView);
        mItemTouchHelper2.attachToRecyclerView(mSubmittedRecyclerView);
    }

    private class ItemTouchHelperCallback extends ItemTouchHelperExtension.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                    ItemTouchHelper.START);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder
                viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (dY != 0 && dX == 0)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState,
                        isCurrentlyActive);
            SurveyResponseAdapter.SurveyViewHolder holder = (SurveyResponseAdapter.SurveyViewHolder) viewHolder;
            if (dX < -holder.mActionContainer.getWidth()) {
                dX = -holder.mActionContainer.getWidth();
            }
            holder.mViewContent.setTranslationX(dX);
        }

    }

}
