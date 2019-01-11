package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
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

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.NetworkNotificationUtils;
import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.Instrument2Activity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyActivity;
import org.adaptlab.chpir.android.survey.SurveyFragment;
import org.adaptlab.chpir.android.survey.models.DeviceSyncEntry;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.looper.ItemTouchHelperExtension;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SurveyViewPagerFragment extends Fragment {
    private static final String TAG = "SurveyViewPagerFragment";
    private static final String UPLOAD_CHANNEL = "UPLOAD_CHANNEL";
    private static final int UPLOAD_ID = 100;
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
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
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

    private static class SubmitSurveyTask extends AsyncTask<Void, Integer, Boolean> {
        SurveyAdapter surveyAdapter;
        Survey survey;
        SurveyViewHolder viewHolder;
        NotificationCompat.Builder builder;
        NotificationManager notificationManager;
        Context mContext;
        int successCount = 0;
        int nonSuccessCount = 0;
        int totalItems = 0;

        SubmitSurveyTask(SurveyAdapter adapter, SurveyViewHolder holder, Context context) {
            surveyAdapter = adapter;
            viewHolder = holder;
            survey = surveyAdapter.mSurveys.get(viewHolder.getAdapterPosition());
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (survey == null) {
                return false;
            } else {
                if (NetworkNotificationUtils.checkForNetworkErrors(mContext)) {
                    if (survey.isPersistent()) {
                        DeviceSyncEntry deviceSyncEntry = new DeviceSyncEntry();
                        if (!survey.isSent()) {
                            totalItems += 1;
                            survey.setSubmittedIdentifier(survey.identifier(mContext));
                        }
                        List<Response> responses = survey.responses();
                        if (survey.getCompletedResponseCount() == 0) {
                            survey.setCompletedResponseCount(responses.size());
                        }
                        sendData(survey, "surveys");
                        for (Response response : responses) {
                            totalItems += 1;
                            sendData(response, "responses");
                            if (response.getResponsePhoto() != null) {
                                totalItems += 1;
                                sendData(response.getResponsePhoto(), "response_images");
                            }
                        }
                        deviceSyncEntry.pushRemote();
                    }
                }
                while (successCount + nonSuccessCount < totalItems) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        if (BuildConfig.DEBUG) Log.e(TAG, "Exception: ", e);
                    }
                }
                return true;
            }
        }

        private void sendData(final SendModel element, String tableName) {
            String url = ActiveRecordCloudSync.getEndPoint() + tableName + ActiveRecordCloudSync.getParams();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, element.toJSON().toString());
            Request request = new okhttp3.Request.Builder().url(url).post(body).build();

            AppUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "onFailure: ", e);
                    // TODO: 11/16/18 Retry call??
                    nonSuccessCount += 1;
                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) {
                    if (response.isSuccessful()) {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Successfully submitted: " + element);
                        if (element instanceof Survey && element.isSent()) {
                            successCount -= 1;
                        }
                        element.setAsSent(mContext);
                        successCount += 1;
                        if (successCount % 10 == 0)
                            publishProgress(successCount);
                    } else {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Not Successful");
                        // TODO: 11/16/18 Retry call??
                        nonSuccessCount += 1;
                    }
                }
            });
        }

        @Override
        protected void onPreExecute() {
            builder = new NotificationCompat.Builder(mContext, UPLOAD_CHANNEL)
                    .setSmallIcon(R.drawable.ic_cloud_upload_black_24dp)
                    .setContentTitle(mContext.getString(R.string.uploading_survey) + " " + survey.identifier(mContext))
                    .setContentText(mContext.getString(R.string.background_process_progress_message))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager = mContext.getSystemService(NotificationManager.class);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(
                            UPLOAD_CHANNEL, UPLOAD_CHANNEL, NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(UPLOAD_ID, builder.build());
            } else {
                notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(UPLOAD_ID, builder.build());
            }
            builder.setProgress(survey.responses().size() + 1, 0, false);
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (status) {
                surveyAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                String message = mContext.getString(R.string.submitted) + " " +
                        (survey.getCompletedResponseCount() - survey.responses().size())
                        + " " + mContext.getString(R.string.of) + " " +
                        survey.getCompletedResponseCount();
                if (builder != null && notificationManager != null) {
                    builder.setContentText(message).setProgress(totalItems, successCount, false);
                    notificationManager.notify(UPLOAD_ID, builder.build());
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (builder != null && notificationManager != null) {
                builder.setProgress(totalItems, values[0], false);
                notificationManager.notify(UPLOAD_ID, builder.build());
            }
            super.onProgressUpdate(values[0]);
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
                    if (survey != null && survey.getInstrument().loaded() && !survey.isSent()) {
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
                                            int index = viewHolder.getAdapterPosition();
                                            if (index > -1 && index < mSurveyAdapter.mSurveys.size()) {
                                                new SubmitSurveyTask(mSurveyAdapter, viewHolder,
                                                        getActivity()).execute();
                                            }
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

    private class SurveyViewHolder extends RecyclerView.ViewHolder {
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

            String progress;
            if (survey.isSent()) {
                progress = getString(R.string.submitted) + " " + (survey.getCompletedResponseCount() - survey.responses().size())
                        + " " + getString(R.string.of) + " " + survey.getCompletedResponseCount();
            } else {
                progress = getString(R.string.progress) + " " + survey.responses().size() + " "
                        + getString(R.string.of) + " " + survey.getInstrument().questions().size();
            }
            SpannableString progressString = new SpannableString(progress);
            if (survey.isSent()) {
                if (survey.responses().size() > 0) {
                    mSubmitAction.setVisibility(View.VISIBLE);
                    progressString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                            .green)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    mSubmitAction.setVisibility(View.GONE);
                    progressString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                            .blue)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
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
