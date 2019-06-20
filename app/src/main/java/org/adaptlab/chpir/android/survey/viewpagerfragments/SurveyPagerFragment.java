package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.InstrumentActivity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.SurveyAdapter;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.relations.ProjectSurveyRelation;
import org.adaptlab.chpir.android.survey.tasks.SubmitSurveyTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.looper.ItemTouchHelperExtension;
import org.adaptlab.chpir.android.survey.viewmodelfactories.ProjectSurveyRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.ProjectSurveyRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SettingsViewModel;

import java.util.List;

public class SurveyPagerFragment extends Fragment {
    private static final String TAG = "SurveyPagerFragment";

    private Button mSubmitAll;
    private SurveyAdapter mSurveyAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setViewModels(long projectId) {
        ProjectSurveyRelationViewModelFactory factory = new ProjectSurveyRelationViewModelFactory(getActivity().getApplication(), projectId);
        ProjectSurveyRelationViewModel viewModel = ViewModelProviders.of(getActivity(), factory).get(ProjectSurveyRelationViewModel.class);
        viewModel.getProjectSurveyRelations().observe(this, new Observer<List<ProjectSurveyRelation>>() {
            @Override
            public void onChanged(@Nullable List<ProjectSurveyRelation> projectSurveyRelations) {
                mSurveyAdapter.setSurveys(projectSurveyRelations);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_instrument, container, false);
        mSurveyAdapter = new SurveyAdapter(getContext());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(mSurveyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);
        ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(new ItemTouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);

        if (AppUtil.getProjectId() == 0) {
            SettingsViewModel settingsViewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);
            settingsViewModel.getSettings().observe(this, new Observer<Settings>() {
                @Override
                public void onChanged(@Nullable Settings settings) {
                    setViewModels(Long.valueOf(settings.getProjectId()));
                }
            });
        } else {
            setViewModels(AppUtil.getProjectId());
        }

        return view;
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
//                                        for (SurveyRelation survey : completedSurveys) {
//                                            completedSurveysAdapter.prepareForSubmission(survey);
//                                        }
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

    private class ItemTouchHelperCallback extends ItemTouchHelperExtension.Callback {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
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
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            SurveyAdapter.SurveyViewHolder holder = (SurveyAdapter.SurveyViewHolder) viewHolder;
            if (dX < -holder.mActionContainer.getWidth()) {
                dX = -holder.mActionContainer.getWidth();
            }
            holder.mViewContent.setTranslationX(dX);
        }

    }

}
