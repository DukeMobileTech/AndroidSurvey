package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.adaptlab.chpir.android.survey.InstrumentActivity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyActivity;
import org.adaptlab.chpir.android.survey.SurveyFragment;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.tasks.SetInstrumentLabelTask;
import org.adaptlab.chpir.android.survey.tasks.SubmitSurveyTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.InstrumentListLabel;
import org.adaptlab.chpir.android.survey.utils.looper.ItemTouchHelperExtension;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class SurveyViewPagerFragment extends Fragment {
    private static final String TAG = "SurveyViewPagerFragment";

    private RecyclerView mInProgressRecyclerView;
    private RecyclerView mCompletedRecyclerView;
    private RecyclerView mSubmittedRecyclerView;

    private SurveyAdapter inProgressSurveysAdapter;
    private SurveyAdapter completedSurveysAdapter;
    private SurveyAdapter submittedSurveysAdapter;

    private List<Survey> inProgressSurveys;
    private List<Survey> completedSurveys;
    private List<Survey> submittedSurveys;

    private Button mSubmitAll;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        setSurveyLists();
        View view = inflater.inflate(R.layout.survey_recycler_view, container, false);

        mInProgressRecyclerView = view.findViewById(R.id.onGoingSurveys);
        mCompletedRecyclerView = view.findViewById(R.id.completedSurveys);
        mSubmittedRecyclerView = view.findViewById(R.id.submittedSurveys);
        mSubmitAll = view.findViewById(R.id.submitAll);

        toggleHeadersVisibility(view);
        submitAll();

        inProgressSurveysAdapter = new SurveyAdapter(inProgressSurveys);
        completedSurveysAdapter = new SurveyAdapter(completedSurveys);
        submittedSurveysAdapter = new SurveyAdapter(submittedSurveys);

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
                                        for (Survey survey : completedSurveys) {
                                            prepareForSubmission(survey);
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
        for (Survey survey : Survey.getAllProjectSurveys(AppUtil.getProjectId())) {
            if (survey.isSent() || survey.isQueued()) {
                submittedSurveys.add(survey);
            } else if (survey.readyToSend()) {
                completedSurveys.add(survey);
            } else {
                inProgressSurveys.add(survey);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setSurveyLists();
        inProgressSurveysAdapter.updateSurveys(inProgressSurveys);
        completedSurveysAdapter.updateSurveys(completedSurveys);
        submittedSurveysAdapter.updateSurveys(submittedSurveys);
    }

    private void setSurveyLeftSwipe() {
        ItemTouchHelperExtension mItemTouchHelper = new ItemTouchHelperExtension(new ItemTouchHelperCallback());
        ItemTouchHelperExtension mItemTouchHelper1 = new ItemTouchHelperExtension(new ItemTouchHelperCallback());
        ItemTouchHelperExtension mItemTouchHelper2 = new ItemTouchHelperExtension(new ItemTouchHelperCallback());
        mItemTouchHelper.attachToRecyclerView(mInProgressRecyclerView);
        mItemTouchHelper1.attachToRecyclerView(mCompletedRecyclerView);
        mItemTouchHelper2.attachToRecyclerView(mSubmittedRecyclerView);
    }

    private void prepareForSubmission(Survey survey) {
        if (survey.getCompletedResponseCount() == 0 && survey.responses().size() > 0) {
            survey.setCompletedResponseCount(survey.responses().size());
        }
        survey.setQueued(true);
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
            SurveyViewHolder holder = (SurveyViewHolder) viewHolder;
            if (dX < -holder.mActionContainer.getWidth()) {
                dX = -holder.mActionContainer.getWidth();
            }
            holder.mViewContent.setTranslationX(dX);
        }
    }

    public class SurveyAdapter extends RecyclerView.Adapter<SurveyViewHolder> {
        public List<Survey> mSurveys;

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
                    return oldSurveys.get(oldSurveyPosition).equals(
                            mSurveys.get(newSurveyPosition));
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
            View surveyView = getLayoutInflater().inflate(R.layout.list_item_survey_background,
                    parent, false);
            return new SurveyViewHolder(surveyView);
        }

        @Override
        public void onBindViewHolder(@NonNull final SurveyViewHolder viewHolder, int position) {
            viewHolder.setSurvey(mSurveys.get(position));
            setSurveyDeleteAction(viewHolder);
            setSurveyLaunchAction(viewHolder);
            setSurveySubmitAction(viewHolder);
        }

        private void setSurveyDeleteAction(@NonNull final SurveyViewHolder viewHolder) {
            viewHolder.mDeleteAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.delete_survey_title)
                            .setMessage(R.string.delete_survey_message)
                            .setPositiveButton(R.string.delete, new DialogInterface
                                    .OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    remove(viewHolder.getAdapterPosition());
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface
                                    .OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    notifyItemChanged(viewHolder.getAdapterPosition());
                                }
                            })
                            .show();
                }
            });
        }

        private void setSurveyLaunchAction(@NonNull final SurveyViewHolder viewHolder) {
            viewHolder.mViewContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Survey survey = mSurveys.get(viewHolder.getAdapterPosition());
                    if (survey == null) return;
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
                }
            });
        }

        private void setSurveySubmitAction(final SurveyViewHolder viewHolder) {
            viewHolder.mSubmitAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.submit_survey)
                            .setMessage(R.string.submit_survey_message)
                            .setPositiveButton(R.string.submit,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Survey survey = mSurveys.get(viewHolder.getAdapterPosition());
                                            prepareForSubmission(survey);
                                            new SubmitSurveyTask(getActivity()).execute();
                                            notifyItemChanged(viewHolder.getAdapterPosition());
                                        }
                                    })
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            notifyItemChanged(viewHolder.getAdapterPosition());
                                        }
                                    })
                            .show();
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

    }

    public class SurveyViewHolder extends RecyclerView.ViewHolder {
        View mViewContent;
        View mActionContainer;
        TextView mDeleteAction;
        TextView mSubmitAction;
        TextView surveyTextView;
        TextView progressTextView;
        Survey mSurvey;

        SurveyViewHolder(final View itemView) {
            super(itemView);
            surveyTextView = itemView.findViewById(R.id.surveyProperties);
            progressTextView = itemView.findViewById(R.id.surveyProgress);
            mViewContent = itemView.findViewById(R.id.list_item_survey_main_content);
            mActionContainer = itemView.findViewById(R.id.list_item_survey_action_container);
            mDeleteAction = itemView.findViewById(R.id.list_item_survey_action_delete);
            mSubmitAction = itemView.findViewById(R.id.list_item_survey_action_submit);
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
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length() +
                    instrumentTitle.length(), surveyTitle.length() + instrumentTitle.length() +
                    lastUpdated.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                            .secondary_text)), surveyTitle.length() + instrumentTitle.length(),
                    surveyTitle.length() + instrumentTitle.length() + lastUpdated.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // progress
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length() +
                            instrumentTitle.length() + lastUpdated.length(),
                    surveyTitle.length() + instrumentTitle.length() + lastUpdated.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .secondary_text)), surveyTitle.length() + instrumentTitle.length() +
                    lastUpdated.length(), surveyTitle.length() + instrumentTitle.length() +
                    lastUpdated.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            surveyTextView.setText(spannableString);
            if (!survey.isQueued()) {
                new SetInstrumentLabelTask(SurveyViewPagerFragment.this).execute(
                        new InstrumentListLabel(survey.getInstrument(), surveyTextView));
            }

            String progress;
            if (survey.isSent()) {
                progress = getString(R.string.submitted) + " " + (survey.getCompletedResponseCount() - survey.responses().size())
                        + " " + getString(R.string.of) + " " + survey.getCompletedResponseCount();
            } else if (survey.isQueued()) {
                progress = getString(R.string.submitted) + " " +
                        (survey.getCompletedResponseCount() - survey.responses().size()) + " "
                        + getString(R.string.of) + " " + survey.getCompletedResponseCount();
            } else {
                progress = getString(R.string.progress) + " " + survey.responses().size() + " "
                        + getString(R.string.of) + " " + survey.getInstrument().questions().size();
            }
            SpannableString progressString = new SpannableString(progress);
            if (survey.isSent() || survey.isQueued()) {
                if (survey.responses().size() > 0) {
                    mSubmitAction.setVisibility(View.VISIBLE);
                } else {
                    mSubmitAction.setVisibility(View.GONE);
                }
                progressString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                        .blue)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (survey.readyToSend()) {
                progressString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                        .green)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mSubmitAction.setVisibility(View.VISIBLE);
            } else {
                progressString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                        .red)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mSubmitAction.setVisibility(View.GONE);
            }

            progressTextView.setText(progressString);
        }

    }

}
