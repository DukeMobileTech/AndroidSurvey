package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.InstrumentActivity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyActivity;
import org.adaptlab.chpir.android.survey.SurveyFragment;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.tasks.SetInstrumentLabelTask;
import org.adaptlab.chpir.android.survey.tasks.SubmitSurveyTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.InstrumentListLabel;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class SurveyViewPagerFragment extends Fragment {
    private static final String TAG = "SurveyViewPagerFragment";
    private SurveyAdapter surveyAdapter;
    private List<Survey> mSurveys;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setSurveyLists();
        View view = inflater.inflate(R.layout.recycler_view, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        surveyAdapter = new SurveyAdapter(mSurveys);
        recyclerView.setAdapter(surveyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView
                .getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeCallback(surveyAdapter, getContext()));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    private void submitAll() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.submit_survey)
                .setMessage(R.string.submit_survey_message)
                .setPositiveButton(R.string.submit,
                        (dialog, id) -> {
                            for (Survey survey : mSurveys) {
                                prepareForSubmission(survey);
                            }
                            new SubmitSurveyTask(getActivity()).execute();
                            startActivity(new Intent(getActivity(), InstrumentActivity.class));
                            if (getActivity() != null) getActivity().finish();
                        })
                .setNegativeButton(R.string.cancel,
                        (dialog, id) -> {
                        })
                .show();
    }

    private void setSurveyLists() {
        mSurveys = new ArrayList<>();
        for (Survey survey : Survey.getAllProjectSurveys(AppUtil.getProjectId())) {
            if (survey.isSent() || survey.isQueued()) continue;

            if (survey.readyToSend()) {
                mSurveys.add(survey);
            } else {
                mSurveys.add(survey);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setSurveyLists();
        surveyAdapter.updateSurveys(mSurveys);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_instrument, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
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

    private void prepareForSubmission(Survey survey) {
        if (survey.readyToSend()) {
            if (survey.getCompletedResponseCount() == 0 && survey.responses().size() > 0) {
                survey.setCompletedResponseCount(survey.responses().size());
            }
            survey.setQueued(true);
        }
    }

    public class SurveyAdapter extends RecyclerView.Adapter<SurveyViewHolder> {
        List<Survey> mSurveys;

        SurveyAdapter(List<Survey> surveys) {
            mSurveys = surveys;
        }

        public void add(Survey survey) {
            mSurveys.add(0, survey);
            notifyItemInserted(0);
        }

        void updateSurveys(List<Survey> newSurveys) {
            final List<Survey> oldSurveys = new ArrayList<>(this.mSurveys);
            this.mSurveys.clear();
            if (newSurveys != null) {
                this.mSurveys.addAll(newSurveys);
            }

            DiffUtil.calculateDiff(new DiffUtil.Callback() {

                @Override
                public int getOldListSize() {
                    return oldSurveys.size();
                }

                @Override
                public int getNewListSize() {
                    return mSurveys.size();
                }

                @Override
                public boolean areItemsTheSame(int oldSurveyPosition, int newSurveyPosition) {
                    return oldSurveys.get(oldSurveyPosition).equals(mSurveys.get(newSurveyPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldSurveyPosition, int newSurveyPosition) {
                    Survey oldSurvey = oldSurveys.get(oldSurveyPosition);
                    Survey newSurvey = mSurveys.get(newSurveyPosition);
                    return oldSurvey.getLastUpdated().equals(newSurvey.getLastUpdated()) &&
                            oldSurvey.responses().size() == newSurvey.responses().size();
                }
            }).dispatchUpdatesTo(this);
        }

        @NonNull
        @Override
        public SurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View surveyView = getLayoutInflater().inflate(R.layout.list_item_survey,
                    parent, false);
            return new SurveyViewHolder(surveyView);
        }

        @Override
        public void onBindViewHolder(@NonNull final SurveyViewHolder viewHolder, int position) {
            viewHolder.setSurvey(mSurveys.get(position));
            setSurveyLaunchAction(viewHolder);
        }

        private void setSurveyLaunchAction(@NonNull final SurveyViewHolder viewHolder) {
            viewHolder.mViewContent.setOnClickListener(v -> {
                Survey survey = mSurveys.get(viewHolder.getAdapterPosition());
                if (getActivity() == null || survey == null) return;

                if (survey.isQueued() || survey.isSent()) {
                    Toast.makeText(getActivity(), R.string.survey_submitted, Toast.LENGTH_LONG).show();
                } else if (!survey.getInstrument().loaded()) {
                    Toast.makeText(getActivity(), R.string.instrument_not_loaded, Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(getActivity(), SurveyActivity.class);
                    i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID,
                            survey.getInstrument().getRemoteId());
                    i.putExtra(SurveyFragment.EXTRA_SURVEY_ID, survey.getId());
                    i.putExtra(SurveyFragment.EXTRA_QUESTION_NUMBER,
                            survey.getLastQuestion().getNumberInInstrument() - 1);
                    i.putExtra(SurveyFragment.EXTRA_AUTHORIZE_SURVEY,
                            ((InstrumentActivity) getActivity()).isAuthorizeSurvey());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                    } else {
                        startActivity(i);
                    }
                }
            });
        }

        public void remove(int position) {
            if (position > -1 && position < mSurveys.size()) {
                Survey survey = mSurveys.get(position);
                if (survey != null) {
                    mSurveys.remove(position);
                    survey.delete();
                    notifyItemRemoved(position);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mSurveys.size();
        }

        void deleteItem(final int position) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.delete_survey_title)
                    .setMessage(R.string.delete_survey_message)
                    .setPositiveButton(R.string.delete, (dialog, id) -> remove(position))
                    .setNegativeButton(R.string.cancel, (dialog, id) -> notifyItemChanged(position))
                    .show();
        }

        void uploadItem(final int position) {
            final Survey survey = mSurveys.get(position);
            if (getActivity() == null || survey == null) return;

            if (survey.readyToSend()) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.submit_survey)
                        .setMessage(R.string.submit_survey_message)
                        .setPositiveButton(R.string.submit,
                                (dialog, id) -> {
                                    prepareForSubmission(survey);
                                    new SubmitSurveyTask(getActivity()).execute();
                                    mSurveys.remove(position);
                                    notifyItemRemoved(position);
                                })
                        .setNegativeButton(R.string.cancel,
                                (dialog, id) -> notifyItemChanged(position))
                        .show();
            } else {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getString(R.string.incomplete_survey))
                        .setMessage(getActivity().getString(R.string.incomplete_survey_message))
                        .setPositiveButton(R.string.okay,
                                (dialog, id) -> notifyItemChanged(position))
                        .show();
            }
        }
    }

    public class SurveyViewHolder extends RecyclerView.ViewHolder {
        View mViewContent;
        TextView surveyTextView;
        TextView progressTextView;
        Survey mSurvey;

        SurveyViewHolder(final View itemView) {
            super(itemView);
            surveyTextView = itemView.findViewById(R.id.surveyProperties);
            progressTextView = itemView.findViewById(R.id.surveyProgress);
            mViewContent = itemView.findViewById(R.id.list_item_survey_content);
        }

        public void setSurvey(Survey survey) {
            this.mSurvey = survey;
            String surveyTitle = survey.identifier(AppUtil.getContext()) + "\n";
            String instrumentTitle = survey.getInstrument().getTitle() + "\n";
            String lastUpdated = DateFormat.getDateTimeInstance().format(
                    survey.getLastUpdated()) + "  ";
            SpannableString spannableString = new SpannableString(surveyTitle +
                    instrumentTitle + lastUpdated);
            // survey title
            spannableString.setSpan(new RelativeSizeSpan(1.2f), 0, surveyTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(
                    R.color.primary_text)), 0, surveyTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // instrument title
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length(),
                    surveyTitle.length() + instrumentTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(
                    R.color.secondary_text)), surveyTitle.length(), surveyTitle.length() +
                    instrumentTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // last updated at
            int end = surveyTitle.length() + instrumentTitle.length() +
                    lastUpdated.length();
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length() +
                    instrumentTitle.length(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                            .secondary_text)), surveyTitle.length() + instrumentTitle.length(),
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // progress
            spannableString.setSpan(new RelativeSizeSpan(0.8f), end,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .secondary_text)), end, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            surveyTextView.setText(spannableString);

            if (!survey.isQueued()) {
                new SetInstrumentLabelTask(SurveyViewPagerFragment.this).execute(
                        new InstrumentListLabel(survey.getInstrument(), surveyTextView));
            }

            String progress = getString(R.string.progress) + " " + survey.responses().size() + " " + getString(R.string.of) + " " + survey.getInstrument().getQuestionCount();
            SpannableString progressString = new SpannableString(progress);
            if (survey.readyToSend()) {
                progressString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                progressString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            progressTextView.setText(progressString);
        }

    }

    private class SwipeCallback extends ItemTouchHelper.SimpleCallback {

        private final ColorDrawable mDeleteBackground;
        private final ColorDrawable mUploadBackground;
        private SurveyAdapter mAdapter;
        private Drawable mDeleteIcon;
        private Drawable mUploadIcon;

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
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
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
