package org.adaptlab.chpir.android.survey.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.OptionSetOptionDao;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.entities.BitmapEntity;
import org.adaptlab.chpir.android.survey.entities.OptionSetOption;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.repositories.Repository;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.NotificationUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private static byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            InputStream in = connection.getInputStream();
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
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
                            if (mEntity.getClass().getName().equals(OptionSetOption.class.getName())) {
                                downloadOptionImages();
                            }
                            if (mEntity.getClass().getName().equals(Question.class.getName())) {
                                downloadQuestionImages();
                            }
                            AppUtil.updateDownloadProgress();
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
        } else {
            AppUtil.updateDownloadProgress();
        }
        return null;
    }

    private void downloadOptionImages() {
        List<OptionSetOption> withImages = ((OptionSetOptionDao) mBaseDao).withImages();
        for (OptionSetOption optionSetOption : withImages) {
            if (optionSetOption.hasImage()) {
                String url = AppUtil.getFullApiUrl() + "images/" + optionSetOption.getOptionRemoteId() + AppUtil.getParams() + "&option_id=" + optionSetOption.getOptionRemoteId();
                getFile(url, optionSetOption);
            }
        }
    }

    private void downloadQuestionImages() {
        List<Question> withImages = ((QuestionDao) mBaseDao).withImages();
        for (Question question : withImages) {
            if (question.hasQuestionImage()) {
                String url = AppUtil.getFullApiUrl() + "images/" + question.getRemoteId() + AppUtil.getParams() + "&question_id=" + question.getRemoteId();
                getFile(url, question);
            }
        }
    }

    public void getFile(String url, BitmapEntity bitmapEntity) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Image url: " + url);
        String filename = UUID.randomUUID().toString() + ".png";
        FileOutputStream fileWriter = null;
        try {
            byte[] imageBytes = getUrlBytes(url);
            if (imageBytes != null) {
                fileWriter = SurveyApp.getInstance().openFileOutput(filename, Context.MODE_PRIVATE);
                fileWriter.write(imageBytes);
                bitmapEntity.setBitmapPath(filename);
                mBaseDao.update(bitmapEntity);
            }
            if (BuildConfig.DEBUG) Log.i(TAG, "Image saved in " + filename);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "IOException ", e);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Exception ", e);
            }
        }
    }

}
