package org.adaptlab.chpir.android.survey.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.adaptlab.chpir.android.activerecordcloudsync.NotificationUtils;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.entities.DeviceSyncEntry;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.Uploadable;
import org.adaptlab.chpir.android.survey.relations.ProjectSurveyRelation;
import org.adaptlab.chpir.android.survey.repositories.ResponseRepository;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
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
    private static final String TAG = SubmitSurveyTask.class.getName();
    private SurveyRepository mSurveyRepository;
    private ResponseRepository mResponseRepository;

    @Override
    protected Void doInBackground(Void... params) {
        mSurveyRepository = new SurveyRepository(SurveyApp.getInstance());
        mResponseRepository = new ResponseRepository(SurveyApp.getInstance());
        List<ProjectSurveyRelation> projectSurveyRelations = new ArrayList<>();
        for (ProjectSurveyRelation relation : mSurveyRepository.getSurveyDao().projectSurveysSync(AppUtil.getProjectId())) {
            if (relation.survey.isQueued() && relation.responses.size() > 0) {
                projectSurveyRelations.add(relation);
            }
        }
        if (BuildConfig.DEBUG)
            Log.i(TAG, "Number of survey to submit: " + projectSurveyRelations.size());

        if (NotificationUtils.checkForNetworkErrors(SurveyApp.getInstance())) {
            for (ProjectSurveyRelation relation : projectSurveyRelations) {
                if (!relation.survey.isSent()) {
                    relation.survey.setIdentifier(relation.survey.identifier(SurveyApp.getInstance(), relation.responses));
                    mSurveyRepository.update(relation.survey);
                }
                sendData(relation.survey, "surveys");
                for (Response response : relation.responses) {
                    sendData(response, "responses");
                }
            }
            sendData(new DeviceSyncEntry(), "device_sync_entries");
        }
        return null;
    }

    private void sendData(final Uploadable element, String tableName) {
        String url = AppUtil.getFullApiUrl() + tableName + AppUtil.getParams();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, element.toJSON());
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
                    element.setSent(true);
                    if (element.getClass().getName().equals(Survey.class.getName())) {
                        mSurveyRepository.update((Survey) element);
                    } else if (element.getClass().getName().equals(Response.class.getName())) {
                        mResponseRepository.delete((Response) element);
                    }
                    response.close();
                } else {
                    if (BuildConfig.DEBUG) Log.i(TAG, "Not Successful");
                    response.close();
                }
            }
        });
    }

}
