package org.adaptlab.chpir.android.survey.tasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.repositories.Repository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.NotificationUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class EntityDownloadTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "EntityDownloadTask";
    private final BaseDao mBaseDao;
    private final BaseDao mTranslationDao;
    private final SurveyEntity mEntity;
    private final SurveyEntity mTranslationEntity;
    private final String mTableName;
    private final Gson mGson;

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
        if (NotificationUtils.checkForNetworkErrors(SurveyApp.getInstance())) {
            String url;
            if (mTableName.equals("projects")) {
                url = AppUtil.getProjectsEndPoint() + AppUtil.getParams();
            } else {
                url = AppUtil.getFullApiUrl() + mTableName + AppUtil.getParams();
            }
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
                            if (mEntity.getClass().getSimpleName().equals(Instrument.class.getSimpleName())) {
                                for (SurveyEntity<?> entity : entities) {
                                    downloadImages((Instrument) entity);
                                }
                            }
                            AppUtil.updateDownloadProgress();
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) Log.e(TAG, "Exception: ", e);
                        }
                    } else {
                        if (BuildConfig.DEBUG) Log.i(TAG, mBaseDao + " download not successful");
                    }
                    response.close();
                }
            });
        } else {
            AppUtil.updateDownloadProgress();
        }
        return null;
    }

    private void downloadImages(Instrument instrument) {
        if (BuildConfig.DEBUG) Log.i(TAG, "DOWNLOADING ZIP FILE!");
        String url = AppUtil.getFullApiUrl() + "instruments/" + instrument.getRemoteId() + "/images" + AppUtil.getParams();
        if (BuildConfig.DEBUG) Log.i(TAG, "URL: " + url);
        URLConnection urlConnection;
        try {
            URL finalUrl = new URL(url);
            urlConnection = finalUrl.openConnection();
            ZipInputStream zipInputStream = new ZipInputStream(urlConnection.getInputStream());
            for (ZipEntry zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream.getNextEntry()) {
                if (BuildConfig.DEBUG) Log.i(TAG, "Extracting: " + zipEntry.getName() + " ...");
                String path = SurveyApp.getInstance().getFilesDir().getAbsolutePath() + "/" + instrument.getRemoteId();
                File folder = new File(path);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                String innerFileName = path + "/" + zipEntry.getName();
                FileOutputStream outputStream = new FileOutputStream(innerFileName);
                final int BUFFER_SIZE = 2048;
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, BUFFER_SIZE);
                int count = 0;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    bufferedOutputStream.write(buffer, 0, count);
                }
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
        } catch (MalformedURLException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "MalformedURLException: ", e);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "IOException: ", e);
        }
    }

}
