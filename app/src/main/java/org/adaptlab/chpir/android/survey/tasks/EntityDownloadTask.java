package org.adaptlab.chpir.android.survey.tasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.repositories.Repository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class EntityDownloadTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "EntityDownloadTask";
    private BaseDao mBaseDao;
    private BaseDao mTranslationDao;
    private SurveyEntity mEntity;
    private SurveyEntity mTranslationEntity;
    private String mTableName;
    private Gson mGson;

    public EntityDownloadTask(Repository repository) {
        mBaseDao = repository.getDao();
        mTranslationDao = repository.getTranslationDao();
        mEntity = repository.getEntity();
        mTranslationEntity = repository.getTranslationEntity();
        mTableName = repository.getRemoteTableName();
        mGson = repository.getGson();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String url;
        if (mTableName.equals("projects")) {
            url = AppUtil.getProjectsEndPoint() + AppUtil.getParams();
        } else {
            url = AppUtil.getFullApiUrl() + mTableName + AppUtil.getParams();
        }
        if (BuildConfig.DEBUG) Log.i(TAG, "url: " + url);
        final Request request = new Request.Builder().url(url).build();

        AppUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Call call, IOException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, mBaseDao + " download exception: ", e);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call call, final okhttp3.Response response) {
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body().string();
                        if (BuildConfig.DEBUG) Log.i(TAG, mBaseDao + ": " + responseString);
                        List<? extends SurveyEntity> entities = mGson.fromJson(responseString, mEntity.getType());
                        mEntity.save(mBaseDao, entities);
                        if (mTranslationDao != null && mTranslationEntity != null) {
                            List<SurveyEntity> translations = new ArrayList<>();
                            for (SurveyEntity entity : entities) {
                                translations.addAll(entity.getTranslations());
                            }
                            mTranslationEntity.save(mTranslationDao, translations);
                        }
                        AppUtil.incrementRemoteDownloadCount();
                        AppUtil.setLoopsTask();
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) Log.e(TAG, "Exception: ", e);
                    }
                    response.close();
                } else {
                    if (BuildConfig.DEBUG) Log.i(TAG, mBaseDao + " download not successful");
                    response.close();
                }
            }
        });
        return null;
    }

}
