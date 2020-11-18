package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.SubmittedSurveyAdapter;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.relations.ProjectSurveyRelation;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.tasks.EntityUploadTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.viewmodelfactories.ProjectSurveyRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.ProjectSurveyRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SettingsViewModel;

import java.util.List;

public class SurveyScorePagerFragment extends Fragment {
    private static final String TAG = SurveyScorePagerFragment.class.getName();

    private SubmittedSurveyAdapter mSurveyAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_instrument, container, false);
        mSurveyAdapter = new SubmittedSurveyAdapter(getContext(), new SurveyRepository(getActivity().getApplication()));
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(mSurveyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        setProject();

        return view;
    }

    private void setProject() {
        if (AppUtil.getProjectId() == null) {
            SettingsViewModel settingsViewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);
            settingsViewModel.getSettings().observe(this, new Observer<Settings>() {
                @Override
                public void onChanged(@Nullable Settings settings) {
                    if (settings == null || settings.getProjectId() == null) return;
                    setViewModels(Long.valueOf(settings.getProjectId()));
                }
            });
        } else {
            setViewModels(AppUtil.getProjectId());
        }
    }

    private void setViewModels(long projectId) {
        ProjectSurveyRelationViewModelFactory factory = new ProjectSurveyRelationViewModelFactory(getActivity().getApplication(), projectId);
        ProjectSurveyRelationViewModel viewModel = ViewModelProviders.of(getActivity(), factory).get(ProjectSurveyRelationViewModel.class);
        viewModel.getSubmittedProjectSurveyRelations().observe(this, new Observer<List<ProjectSurveyRelation>>() {
            @Override
            public void onChanged(@Nullable List<ProjectSurveyRelation> projectSurveyRelations) {
                mSurveyAdapter.setSurveys(projectSurveyRelations);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_instrument, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_item_submit_all).setEnabled(true).setVisible(true);
        menu.findItem(R.id.menu_item_settings).setEnabled(false).setVisible(false);
        menu.findItem(R.id.menu_item_refresh).setEnabled(false).setVisible(false);
        menu.findItem(R.id.menu_item_progress_action).setEnabled(false).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_submit_all) {
            submitAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitAll() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.submit_survey)
                .setMessage(R.string.submit_survey_message)
                .setPositiveButton(R.string.submit,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                for (ProjectSurveyRelation projectSurveyRelation : mSurveyAdapter.getSurveyRelations()) {
                                    if (projectSurveyRelation.survey.isComplete()) {
                                        mSurveyAdapter.prepareForSubmission(projectSurveyRelation);
                                    }
                                }
                                new EntityUploadTask().execute();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                .show();
    }

}
