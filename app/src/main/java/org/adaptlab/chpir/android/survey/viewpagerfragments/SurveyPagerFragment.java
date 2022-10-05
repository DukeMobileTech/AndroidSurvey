package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.SurveyAdapter;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.relations.ProjectSurveyRelation;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.tasks.EntityUploadTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.viewmodelfactories.ProjectSurveyRelationViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.ProjectSurveyRelationViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SettingsViewModel;

import java.util.List;

public class SurveyPagerFragment extends Fragment {
    private static final String TAG = SurveyPagerFragment.class.getName();

    private SurveyAdapter mSurveyAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_instrument, container, false);
        mSurveyAdapter = new SurveyAdapter(getContext(), new SurveyRepository(getActivity().getApplication()));
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(mSurveyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeCallback(mSurveyAdapter, getContext()));
        itemTouchHelper.attachToRecyclerView(recyclerView);

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
        viewModel.getOngoingProjectSurveyRelations().observe(this, new Observer<List<ProjectSurveyRelation>>() {
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

    private class SwipeCallback extends ItemTouchHelper.SimpleCallback {

        private final ColorDrawable mDeleteBackground;
        private final ColorDrawable mUploadBackground;
        private final SurveyAdapter mAdapter;
        private final Drawable mDeleteIcon;
        private final Drawable mUploadIcon;

        SwipeCallback(SurveyAdapter adapter, Context context) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mAdapter = adapter;
            mDeleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_forever_black_24dp);
            mUploadIcon = ContextCompat.getDrawable(context, R.drawable.ic_cloud_upload_black_24dp);
            mDeleteBackground = new ColorDrawable(getResources().getColor(R.color.red));
            mUploadBackground = new ColorDrawable(getResources().getColor(R.color.green));
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (direction == ItemTouchHelper.LEFT) {
                mAdapter.deleteItem(position);
            } else if (direction == ItemTouchHelper.RIGHT) {
                mAdapter.uploadItem(position);
            }
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;

            if (dX > 0) { // Swiping to the right
                int iconMargin = (itemView.getHeight() - mUploadIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - mUploadIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + mUploadIcon.getIntrinsicHeight();
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + mUploadIcon.getIntrinsicWidth();
                mUploadIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                mUploadBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                mUploadBackground.draw(c);
                mUploadIcon.draw(c);
            } else if (dX < 0) { // Swiping to the left
                int iconMargin = (itemView.getHeight() - mDeleteIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - mDeleteIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + mDeleteIcon.getIntrinsicHeight();
                int iconLeft = itemView.getRight() - iconMargin - mDeleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                mDeleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                mDeleteBackground.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                mDeleteBackground.draw(c);
                mDeleteIcon.draw(c);
            } else { // view is unSwiped
                mDeleteBackground.setBounds(0, 0, 0, 0);
            }
        }

    }

}
