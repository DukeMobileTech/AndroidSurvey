package org.adaptlab.chpir.android.survey.tasks;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.NetworkNotificationUtils;
import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.Instrument2Activity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.DeviceSyncEntry;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SubmitSurveyTask extends AsyncTask<Void, Integer, Boolean> {
    private static final String TAG = "SurveyViewPagerFragment";
    private static final String UPLOAD_CHANNEL = "UPLOAD_CHANNEL";
    private static final int UPLOAD_ID = 100;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private Context mContext;
    private List<Survey> mSurveys;
    private int mCount = 0;
    private boolean mRestart;

    public SubmitSurveyTask(Context context, boolean restart) {
        mContext = context;
        mRestart = restart;
        mSurveys = new ArrayList<>();
        for (Survey survey : Survey.getAllProjectSurveys(AppUtil.getProjectId())) {
            if (survey.isQueued() && survey.responses().size() > 0) {
                mSurveys.add(survey);
            }
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        for (Survey survey : mSurveys) {
            mCount++;
            if (survey == null) {
                return false;
            } else {
                if (NetworkNotificationUtils.checkForNetworkErrors(mContext)) {
                    if (survey.isPersistent()) {
                        DeviceSyncEntry deviceSyncEntry = new DeviceSyncEntry();
                        if (!survey.isSent()) {
                            survey.setSubmittedIdentifier(survey.identifier(mContext));
                        }
                        List<Response> responses = survey.responses();
                        sendData(survey, "surveys");
                        for (Response response : responses) {
                            sendData(response, "responses");
                            if (response.getResponsePhoto() != null) {
                                sendData(response.getResponsePhoto(), "response_images");
                            }
                        }
                        deviceSyncEntry.pushRemote();
                    }
                }
            }
            publishProgress(mCount);
        }
        return true;
    }

    private void sendData(final SendModel element, String tableName) {
        String url = ActiveRecordCloudSync.getEndPoint() + tableName + ActiveRecordCloudSync.getParams();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, element.toJSON().toString());
        final Request request = new okhttp3.Request.Builder().url(url).post(body).build();

        AppUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(Call call, final okhttp3.Response response) {
                if (response.isSuccessful()) {
                    if (BuildConfig.DEBUG) Log.i(TAG, "Successfully submitted: " + element);
                    element.setAsSent(mContext);
                    response.close();
                } else {
                    if (BuildConfig.DEBUG) Log.i(TAG, "Not Successful");
                    response.close();
                }
            }
        });
    }

    @Override
    protected void onPreExecute() {
        if (mSurveys.size() == 0) return;
        mBuilder = new NotificationCompat.Builder(mContext, UPLOAD_CHANNEL)
                .setSmallIcon(R.drawable.ic_cloud_upload_black_24dp)
                .setContentTitle(mContext.getString(R.string.uploading_surveys))
                .setContentText(mContext.getString(R.string.background_process_progress_message))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNotificationManager = mContext.getSystemService(NotificationManager.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        UPLOAD_CHANNEL, UPLOAD_CHANNEL, NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(channel);
            }
            mNotificationManager.notify(UPLOAD_ID, mBuilder.build());
        } else {
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(UPLOAD_ID, mBuilder.build());
        }
        mBuilder.setProgress(mSurveys.size() + 1, 0, false);
    }

    @Override
    protected void onPostExecute(Boolean status) {
        if (status) {
            String message = mContext.getString(R.string.submitted) + " " + mCount + " " + mContext.getString(R.string.surveys);
            if (mBuilder != null && mNotificationManager != null) {
                mBuilder.setContentText(message).setProgress(mSurveys.size(), mCount, false);
                mNotificationManager.notify(UPLOAD_ID, mBuilder.build());
            }
            if (mRestart) {
                mContext.startActivity(new Intent(mContext, Instrument2Activity.class));
                ((Activity) mContext).finish();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mBuilder != null && mNotificationManager != null) {
            mBuilder.setProgress(mSurveys.size(), values[0], false);
            mNotificationManager.notify(UPLOAD_ID, mBuilder.build());
        }
        super.onProgressUpdate(values[0]);
    }
}