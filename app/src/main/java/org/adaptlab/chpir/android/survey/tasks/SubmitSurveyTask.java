package org.adaptlab.chpir.android.survey.tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.NotificationUtils;
import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
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

public class SubmitSurveyTask extends AsyncTask<Void, Integer, Void> {
    private static final String TAG = "SurveyViewPagerFragment";
    private Context mContext;
    private List<Survey> mSurveys;

    public SubmitSurveyTask(Context context) {
        mContext = context;
        mSurveys = new ArrayList<>();
        for (Survey survey : Survey.getAllProjectSurveys(AppUtil.getProjectId())) {
            if (survey.isQueued() && survey.responses().size() > 0) {
                mSurveys.add(survey);
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (NotificationUtils.checkForNetworkErrors(mContext)) {
            for (Survey survey : mSurveys) {
                if (survey.isPersistent()) {
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
                }
            }
            new DeviceSyncEntry().pushRemote();
        }
        return null;
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
        NotificationUtils.showNotification(mContext, R.drawable.ic_cloud_upload_black_24dp,
                R.string.uploading_surveys, mSurveys.size() + " " +
                        mContext.getString(R.string.survey_upload), Notification.DEFAULT_ALL,
                NotificationCompat.PRIORITY_DEFAULT, NotificationManager.IMPORTANCE_DEFAULT);
    }

}