package org.adaptlab.chpir.android.survey.tasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.entities.Entity;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class EntityDownloadTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "EntityDownloadTask";
    private BaseDao<? extends Entity> mBaseDao;
    private String mTableName;
    private Class<? extends Entity> mEntity;

    public EntityDownloadTask(BaseDao<? extends Entity> dao, String tableName, Class<? extends Entity> entity) {
        mBaseDao = dao;
        mTableName = tableName;
        mEntity = entity;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String url = AppUtil.getFullApiUrl() + mTableName + AppUtil.getParams();
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
                        Gson gson = new Gson();
                        Entity entity = mEntity.newInstance();
                        List<? extends Entity> entities = gson.fromJson(responseString, entity.getType());
                        entity.save(mBaseDao, entities);
                    } catch (IOException | IllegalAccessException | InstantiationException e) {
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
