package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.activerecordcloudsync.HttpUtil;
import org.adaptlab.chpir.android.activerecordcloudsync.NetworkNotificationUtils;
import org.adaptlab.chpir.android.survey.Instrument2Activity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyActivity;
import org.adaptlab.chpir.android.survey.SurveyFragment;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.looper.ItemTouchHelperExtension;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class SurveyViewPagerFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SurveyAdapter mSurveyAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mSurveyAdapter = new SurveyAdapter();
        mRecyclerView.setAdapter(mSurveyAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        setSurveyLeftSwipe();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSurveyAdapter.updateSurveys(Survey.getAllProjectSurveys(AppUtil.getProjectId()));
    }

    private void setSurveyLeftSwipe() {
        ItemTouchHelperExtension.Callback mCallback = new ItemTouchHelperCallback();
        ItemTouchHelperExtension mItemTouchHelper = new ItemTouchHelperExtension(mCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private static class SubmitSurveyTask extends AsyncTask<Void, Void, Boolean> {
        int position;
        SurveyAdapter surveyAdapter;
        Survey survey;

        SubmitSurveyTask(SurveyAdapter adapter, int pos) {
            surveyAdapter = adapter;
            position = pos;
            survey = surveyAdapter.mSurveys.get(position);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (survey == null) {
                return false;
            } else {
                if (NetworkNotificationUtils.checkForNetworkErrors(AppUtil.getContext())) {
                    if (survey.isPersistent()) {
                        NetworkNotificationUtils.showNotification(AppUtil.getContext(),
                                android.R.drawable.stat_sys_download,
                                R.string.sync_notification_text);
                        HttpUtil.postData(survey, "surveys");
                        for (Response response : survey.responses()) {
                            HttpUtil.postData(response, "responses");
                            if (response.getResponsePhoto() != null) {
                                HttpUtil.postData(response.getResponsePhoto(),
                                        "response_images");
                            }
                        }
                        NetworkNotificationUtils.showNotification(AppUtil.getContext(),
                                android.R.drawable.stat_sys_download_done,
                                R.string.sync_notification_complete_text);
                    }
                }
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (status) {
                Survey deletedSurvey = Survey.findByUUID(survey.getUUID());
                if (deletedSurvey == null) {
                    surveyAdapter.remove(position);
                }
            }
        }
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

    private class SurveyAdapter extends RecyclerView.Adapter<SurveyViewHolder> {
        private List<Survey> mSurveys;

        SurveyAdapter() {
            mSurveys = Survey.getAllProjectSurveys(AppUtil.getProjectId());
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
                    if (survey != null && survey.getInstrument().loaded()) {
                        Intent i = new Intent(getActivity(), SurveyActivity.class);
                        i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID,
                                survey.getInstrument().getRemoteId());
                        i.putExtra(SurveyFragment.EXTRA_SURVEY_ID, survey.getId());
                        i.putExtra(SurveyFragment.EXTRA_QUESTION_NUMBER,
                                survey.getLastQuestion().getNumberInInstrument() - 1);
                        i.putExtra(SurveyFragment.EXTRA_AUTHORIZE_SURVEY,
                                ((Instrument2Activity) getActivity()).isAuthorizeSurvey());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startActivity(i, ActivityOptions.makeSceneTransitionAnimation
                                    (getActivity()).toBundle());
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
                                    new SubmitSurveyTask(mSurveyAdapter,
                                            viewHolder.getAdapterPosition()).execute();
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
            Survey survey = mSurveys.get(position);
            if (survey != null) {
                mSurveys.remove(position);
                survey.delete();
                notifyItemRemoved(position);
            }
        }

        @Override
        public int getItemCount() {
            return mSurveys.size();
        }

    }

    private class SurveyViewHolder extends RecyclerView.ViewHolder {
        public View mViewContent;
        public View mActionContainer;
        public TextView mDeleteAction;
        public TextView mSubmitAction;
        TextView surveyTextView;
        Survey mSurvey;

        SurveyViewHolder(final View itemView) {
            super(itemView);
            surveyTextView = itemView.findViewById(R.id.surveyProperties);
            mViewContent = itemView.findViewById(R.id.list_item_survey_main_content);
            mActionContainer = itemView.findViewById(R.id.list_item_survey_action_container);
            mDeleteAction = itemView.findViewById(R.id.list_item_survey_action_delete);
            mSubmitAction = itemView.findViewById(R.id.list_item_survey_action_submit);
        }

        public void setSurvey(Survey survey) {
            this.mSurvey = survey;
            String surveyTitle = survey.identifier(AppUtil.getContext()) + "\n";
            String instrumentTitle = survey.getInstrument().getTitle() + "\n";
            String lastUpdated = DateFormat.getDateTimeInstance().format(survey
                    .getLastUpdated())
                    + "  ";
            String progress = survey.responses().size() + " " + getString(R.string.of) + " " +
                    survey.getInstrument().questions().size();

            SpannableString spannableString = new SpannableString(surveyTitle +
                    instrumentTitle +
                    lastUpdated + progress);
            // survey title
            spannableString.setSpan(new RelativeSizeSpan(1.2f), 0, surveyTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .primary_text)), 0, surveyTitle.length(), Spannable
                    .SPAN_EXCLUSIVE_EXCLUSIVE);
            // instrument title
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length(),
                    surveyTitle
                            .length() + instrumentTitle.length(), Spannable
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .secondary_text)), surveyTitle.length(), surveyTitle.length() +
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
                            instrumentTitle.length() + lastUpdated.length(), surveyTitle
                            .length() +
                            instrumentTitle.length() + lastUpdated.length() + progress.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .secondary_text)), surveyTitle.length() + instrumentTitle.length() +
                    lastUpdated.length(), surveyTitle.length() + instrumentTitle.length() +
                    lastUpdated.length() + progress.length(), Spannable
                    .SPAN_EXCLUSIVE_EXCLUSIVE);

            if (survey.readyToSend()) {
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                        .green)), surveyTitle.length() + instrumentTitle.length() + lastUpdated
                        .length(), surveyTitle.length() + instrumentTitle.length() + lastUpdated
                        .length() + progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mSubmitAction.setVisibility(View.VISIBLE);
            } else {
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                        .red)), surveyTitle.length() + instrumentTitle.length() + lastUpdated
                        .length(), surveyTitle.length() + instrumentTitle.length() + lastUpdated
                        .length() + progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mSubmitAction.setVisibility(View.GONE);
            }

            surveyTextView.setText(spannableString);
        }

    }

}
