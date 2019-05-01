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
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class TranslatableEntityDownloadTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "TranslatableEntityDownloadTask";
    private BaseDao<? extends Entity> mBaseDao;
    private BaseDao<? extends Entity> mTranslationDao;
    private String mTableName;
    private Class<? extends Entity> mEntity;
    private Class<? extends Entity> mTranslationEntity;

    public TranslatableEntityDownloadTask(BaseDao<? extends Entity> dao, BaseDao<? extends Entity> translationDao,
                                   String tableName, Class<? extends Entity> entity, Class<? extends Entity> translationEntity) {
        mBaseDao = dao;
        mTranslationDao = translationDao;
        mTableName = tableName;
        mEntity = entity;
        mTranslationEntity = translationEntity;
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
                        Entity instance = mEntity.newInstance();
                        List<? extends Entity> entities = gson.fromJson(responseString, instance.getType());
                        instance.save(mBaseDao, entities);
                        List<Entity> translations = new ArrayList<>();
                        for (Entity entity : entities) {
                            translations.addAll(entity.getTranslations());
                        }
                        Entity translationInstance = mTranslationEntity.newInstance();
                        translationInstance.save(mTranslationDao, translations);
                    } catch (IOException | IllegalAccessException | InstantiationException e) {
                        if (BuildConfig.DEBUG) Log.e(TAG, "IOException: ", e);
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
