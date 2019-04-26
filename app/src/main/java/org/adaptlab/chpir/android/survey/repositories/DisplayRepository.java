package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.DisplayDao;
import org.adaptlab.chpir.android.survey.daos.DisplayTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.DisplayTranslation;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.InstrumentTranslation;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class DisplayRepository implements Downloadable {
    private DisplayDao mDisplayDao;
    private DisplayTranslationDao mDisplayTranslationDao;
    private LiveData<List<Display>> mAllDisplays;
    private LiveData<List<DisplayTranslation>> mAllDisplayTranslations;

    public DisplayRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mDisplayDao = db.displayDao();
        mDisplayTranslationDao = db.displayTranslationDao();
        mAllDisplays = mDisplayDao.getAllDisplays();
        mAllDisplayTranslations = mDisplayTranslationDao.getAllDisplayTranslations();
    }

    public LiveData<List<Display>> getAllDisplays() {
        return mAllDisplays;
    }

    public LiveData<List<DisplayTranslation>> getAllDisplayTranslations() {
        return mAllDisplayTranslations;
    }

    public DisplayDao getDisplayDao() {
        return mDisplayDao;
    }

    public void download() {
        DownloadDisplaysTask task = new DownloadDisplaysTask(
                mDisplayDao, mDisplayTranslationDao);
        task.setListener(new DownloadDisplaysTask.AsyncTaskListener() {
            @Override
            public void onAsyncTaskFinished() {
                mAllDisplays = mDisplayDao.getAllDisplays();
                mAllDisplayTranslations = mDisplayTranslationDao.getAllDisplayTranslations();
            }
        });
        task.execute();
    }

    private static class DownloadDisplaysTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "DownloadDisplaysTask";
        private DisplayDao mDisplayDao;
        private DisplayTranslationDao mDisplayTranslationDao;
        private AsyncTaskListener mListener;

        DownloadDisplaysTask(DisplayDao displayDao, DisplayTranslationDao translationDao) {
            mDisplayDao = displayDao;
            mDisplayTranslationDao = translationDao;
        }

        void setListener(AsyncTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            downloadDisplays();
            return null;
        }

        private void downloadDisplays() {
            String url = AppUtil.getFullApiUrl() + "displays" + AppUtil.getParams();
            final Request request = new Request.Builder().url(url).build();

            AppUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Display download exception: ", e);
                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) {
                    if (response.isSuccessful()) {
                        try {
                            String responseString = response.body().string();
                            if (BuildConfig.DEBUG) Log.i(TAG, "Displays: " + responseString);
                            Gson gson = new Gson();
                            Type type = new TypeToken<ArrayList<Display>>() {
                            }.getType();
                            List<Display> displays = gson.fromJson(responseString, type);
                            mDisplayDao.updateAll(displays);
                            mDisplayDao.insertAll(displays);
                            List<DisplayTranslation> translations = new ArrayList<>();
                            for (Display display : displays) {
                                translations.addAll(display.getDisplayTranslations());
                            }
                            mDisplayTranslationDao.updateAll(translations);
                            mDisplayTranslationDao.insertAll(translations);
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) Log.e(TAG, "IOException: ", e);
                        }
                        response.close();
                    } else {
                        if (BuildConfig.DEBUG) Log.i(TAG, "Display download not successful");
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
