package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.QuestionDeserializer;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class QuestionRepository {
    private static final String TAG = "QuestionRepository";
    private QuestionDao mQuestionDao;
    private LiveData<List<Question>> mAllQuestions;

    public QuestionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mQuestionDao = db.questionDao();
        mAllQuestions = mQuestionDao.getAllQuestions();
    }

    public void downloadQuestions() {
        DownloadQuestionsTask downloadInstrumentsTask = new DownloadQuestionsTask(mQuestionDao);
        downloadInstrumentsTask.setListener(new DownloadQuestionsTask.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished() {
                mAllQuestions = mQuestionDao.getAllQuestions();
            }
        });
        downloadInstrumentsTask.execute();
    }

    public LiveData<List<Question>> getmAllQuestions() {
        return mAllQuestions;
    }

    private static class DownloadQuestionsTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "DownloadQuestionsTask";
        private QuestionDao mQuestionDao;
        private AsyncTaskListener mListener;

        DownloadQuestionsTask(QuestionDao questionDao) {
            mQuestionDao = questionDao;
        }

        void setListener(AsyncTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            downloadQuestions();
            return null;
        }

        private void downloadQuestions() {
            String url = AppUtil.getFullApiUrl() + "questions" + AppUtil.getParams();
            final Request request = new Request.Builder().url(url).build();

            AppUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Question download exception: ", e);
                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseString = response.body().string();
                            if (BuildConfig.DEBUG) Log.i(TAG, "Questions: " + responseString);
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(Question.class, new QuestionDeserializer());
                            Gson gson = gsonBuilder.create();
                            Type type = new TypeToken<ArrayList<Question>>() {
                            }.getType();
                            List<Question> questions = gson.fromJson(responseString, type);
                            mQuestionDao.updateAll(questions);
                            mQuestionDao.insertAll(questions);
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) Log.e(TAG, "IOException: ", e);
                        }
                        response.close();
                    } else {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Question download not successful");
                        response.close();
                    }
                }
            });
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            mListener.onAsyncTaskFinished();
        }

        public interface AsyncTaskListener {
            void onAsyncTaskFinished();
        }

    }

}
